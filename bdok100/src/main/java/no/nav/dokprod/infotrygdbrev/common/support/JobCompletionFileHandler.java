package no.nav.dokprod.infotrygdbrev.common.support;

import no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper;
import no.nav.dokprod.infotrygdbrev.bdok100.config.Bdok100BatchParameters;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.listener.JobExecutionListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Set;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.START_TIME_KEY;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.JOURNALDATA_CSV;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.LINJEDATA_TXT;
import static no.nav.dokprod.infotrygdbrev.common.BDOKCommonBatchInputParameters.CLEAN_KEY;

public class JobCompletionFileHandler implements JobExecutionListener {

	private static final String WARNING = "WARNING";
	private static final String COMPLETED = "COMPLETED";
	private static final String STOPPED = "STOPPED";
	private static final String OUTPUT_DIRECTORY_NAME = "output";
	private static final String BEHANDLET_DIRECTORY_NAME = "behandlet";
	private static final String FAILED_DIRECTORY_NAME = "failed";

	private final PostRunFileTransfer fileTransfer;
	private final Bdok100BatchParameters batchParameters;

	public JobCompletionFileHandler(PostRunFileTransfer fileTransfer, Bdok100BatchParameters batchParameters) {
		this.fileTransfer = fileTransfer;
		this.batchParameters = batchParameters;
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStepExecutions().size() <= 1) {
			return;
		}
		try {
			if (isCleanOnly(jobExecution)) {
				return;
			}
			switch (jobExecution.getExitStatus().getExitCode()) {
				case COMPLETED:
				case WARNING:
					ok(jobExecution);
					break;
				case STOPPED:
					break;
				default:
					fail(jobExecution);
			}
		} catch (RuntimeException e) {
			jobExecution.setStatus(BatchStatus.FAILED);
			jobExecution.setExitStatus(ExitStatus.FAILED.addExitDescription(e));
			jobExecution.addFailureException(e);
			throw e;
		}
	}

	private void ok(JobExecution jobExecution) {
		finalizeFiles(jobExecution, BEHANDLET_DIRECTORY_NAME);
	}

	private void fail(JobExecution jobExecution) {
		finalizeFiles(jobExecution, FAILED_DIRECTORY_NAME);
	}

	private void finalizeFiles(JobExecution jobExecution, String targetDirectory) {
		Date startTime = (Date) jobExecution.getExecutionContext().get(START_TIME_KEY);
		String timestampedFolder = targetDirectory + "/" + jobExecution.getJobInstance().getJobName().toLowerCase()
			+ "-" + targetDirectory + "-" + FilenameHelper.formatDateToSeconds(startTime);

		moveRemoteInputFiles(timestampedFolder);
		uploadLocalOutputFiles();
		deleteLocalFiles();
	}

	private boolean isCleanOnly(JobExecution jobExecution) {
		JobParameter<Boolean> cleanOnlyParameter = (JobParameter<Boolean>) jobExecution.getJobParameters().getParameter(CLEAN_KEY);
		return cleanOnlyParameter != null && cleanOnlyParameter.value();
	}

	private void moveRemoteInputFiles(String timestampedFolder) {
		Set.of(JOURNALDATA_CSV, LINJEDATA_TXT).forEach(fileName -> fileTransfer.moveFileIfExists(fileName, timestampedFolder + "/" + fileName));
	}

	private void uploadLocalOutputFiles() {
		if (!Files.exists(batchParameters.outputFileLocation())) {
			return;
		}
		try (var outputFiles = Files.list(batchParameters.outputFileLocation())) {
			outputFiles
				.filter(Files::isRegularFile)
				.forEach(file -> fileTransfer.uploadFile(file, OUTPUT_DIRECTORY_NAME + "/" + file.getFileName()));
		} catch (Exception e) {
			throw new IllegalStateException("Failed to upload output files to SFTP", e);
		}
	}

	private void deleteLocalFiles() {
		try {
			for (String fileName : Set.of(JOURNALDATA_CSV, LINJEDATA_TXT)) {
				Files.deleteIfExists(batchParameters.inputFileLocation().resolve(fileName));
			}
			for (String fileName : Set.of(FilenameHelper.KONTOR_CSV, FilenameHelper.VEDLEGG_CSV)) {
				Files.deleteIfExists(batchParameters.staticInputFileLocation().resolve(fileName));
			}
			if (Files.exists(batchParameters.outputFileLocation())) {
				try (var outputFiles = Files.list(batchParameters.outputFileLocation())) {
					for (Path file : outputFiles.filter(Files::isRegularFile).toList()) {
						Files.delete(file);
					}
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to delete local BDOK100 files after SFTP upload", e);
		}
	}
}
