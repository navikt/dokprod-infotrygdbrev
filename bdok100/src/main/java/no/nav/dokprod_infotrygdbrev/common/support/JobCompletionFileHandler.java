package no.nav.dokprod_infotrygdbrev.common.support;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper;
import no.nav.dokprod_infotrygdbrev.common.BDOKCommonBatchInputParameters;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.listener.JobExecutionListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

/**
 * Moves files on job completion
 *
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

	@Override
	public void afterJob(JobExecution jobExecution) {
		getFolders(jobExecution);
		// If input validation failed we exit
		if (jobExecution.getStepExecutions().size() <= 1) {
			log.info("Input validation failed, not moving files");
			return;
		}

		String cleanOnly = jobExecution.getJobParameters().getString(BDOKCommonBatchInputParameters.CLEAN_KEY, "false");
		if (BooleanUtils.toBoolean(cleanOnly)) {
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

	private void getFolders(JobExecution jobExecution) {
		JobParameters jobParameters = jobExecution.getJobParameters();

		inputFolder = Paths.get(jobParameters.getString(CommonBatchInputParameters.INPUT_FILE_LOCATION_KEY));
		behandletFolder = Paths.get(jobParameters.getString(CommonBatchInputParameters.BEHANDLET_FILE_LOCATION_KEY));
		failedFolder = Paths.get(jobParameters.getString(CommonBatchInputParameters.FAILED_FILE_LOCATION_KEY));

		if (jobParameters.getString(CommonBatchInputParameters.WORKSPACE_FILE_LOCATION_KEY) != null) {
			workspaceFolder = Paths.get(jobParameters.getString(CommonBatchInputParameters.WORKSPACE_FILE_LOCATION_KEY));
		}

		startTime = (Date) jobExecution.getExecutionContext().get(CommonBatchInputParameters.START_TIME_KEY);
		jobName = jobExecution.getJobInstance().getJobName().toLowerCase();
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
