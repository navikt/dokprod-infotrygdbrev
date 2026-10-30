package no.nav.dokprod.infotrygdbrev.common.support;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper;
import no.nav.dokprod.infotrygdbrev.bdok100.config.Bdok100BatchParameters;
import no.nav.dokprod.infotrygdbrev.common.BDOKCommonBatchInputParameters;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.listener.JobExecutionListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

/**
 * Moves files on job completion
 */
@Slf4j
public class JobCompletionFileHandler implements JobExecutionListener {

	private static final String WARNING = "WARNING";
	private static final String COMPLETED = "COMPLETED";
	private static final String STOPPED = "STOPPED";

	protected Path inputFolder;
	protected Path behandletFolder;
	protected Path failedFolder;
	protected Path workspaceFolder;

	protected Date startTime;
	private String jobName;

	public JobCompletionFileHandler(Bdok100BatchParameters bdok100BatchParameters) {
		inputFolder = bdok100BatchParameters.inputFileLocation();
		behandletFolder = bdok100BatchParameters.behandletFileLocation();
		failedFolder = bdok100BatchParameters.failedFileLocation();
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		startTime = (Date) jobExecution.getExecutionContext().get(CommonBatchInputParameters.START_TIME_KEY);
		jobName = jobExecution.getJobInstance().getJobName().toLowerCase();
		// If input validation failed we exit
		if (jobExecution.getStepExecutions().size() <= 1) {
			log.info("Input validation failed, not moving files");
			return;
		}

		JobParameter<Boolean> cleanKeyParameter = (JobParameter<Boolean>) jobExecution.getJobParameters().getParameter(BDOKCommonBatchInputParameters.CLEAN_KEY);
		Boolean cleanOnly = cleanKeyParameter != null && cleanKeyParameter.value();
		if (cleanOnly) {
			return;
		}
		switch (jobExecution.getExitStatus().getExitCode()) {
			case COMPLETED:
			case WARNING:
				ok();
				break;
			case STOPPED:
				// Do nothing with files
				break;
			default:
				fail();
		}
	}

	protected void ok() {
		log.info("Job completed ok, moving files to " + behandletFolder.toString());
		move(inputFolder, behandletFolder, "behandlet");
	}

	protected void fail() {
		log.info("Job failed, moving files to " + failedFolder.toString());
		move(inputFolder, failedFolder, "failed");
	}

	protected int move(Path fromFolder, Path toFolder, String targetName) {
		Path timeStampedFolder = toFolder.resolve(getJobName() + "-" + targetName + "-" + FilenameHelper.formatDateToSeconds(startTime));
		int counter = 0;
		try (val files = Files.newDirectoryStream(fromFolder)) {
			Files.createDirectory(timeStampedFolder);
			for (Path file : files) {
				if (!Files.isDirectory(file)) {
					counter++;
					Files.move(file, timeStampedFolder.resolve(file.getFileName().toString()));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Error moving inputFiles to " + targetName, e);
		}
		return counter;
	}

	private String getJobName() {
		return jobName;
	}

}
