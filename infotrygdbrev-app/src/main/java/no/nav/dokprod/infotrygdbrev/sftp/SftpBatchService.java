package no.nav.dokprod.infotrygdbrev.sftp;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokprod.infotrygdbrev.batch.Bdok100JobService;
import no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper;
import no.nav.dokprod.infotrygdbrev.bdok100.config.Bdok100BatchParameters;
import no.nav.dokprod.infotrygdbrev.bdok100.config.DokprodInfotrygdbrevProperties;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.START_TIME_KEY;
import static no.nav.dokprod.infotrygdbrev.common.BDOKCommonBatchInputParameters.CLEAN_KEY;

@Slf4j
@Service
public class SftpBatchService {

	public static final String READY_FILE_NAME = "ready.txt";
	private static final String STARTED_FILE_PREFIX = "started-";
	private static final String MARKER_FILE_SUFFIX = ".txt";

	private final SftpFileService sftpFileService;
	private final SftpToLocalFileStagingService stagingService;
	private final Bdok100JobService jobService;
	private final Bdok100BatchParameters batchParameters;
	private final DokprodInfotrygdbrevProperties dokprodInfotrygdbrevProperties;

	public SftpBatchService(
			SftpFileService sftpFileService,
			SftpToLocalFileStagingService stagingService,
			DokprodInfotrygdbrevProperties dokprodInfotrygdbrevProperties,
			Bdok100JobService jobService,
			Bdok100BatchParameters batchParameters
	) {
		this.sftpFileService = sftpFileService;
		this.stagingService = stagingService;
		this.jobService = jobService;
		this.batchParameters = batchParameters;
		this.dokprodInfotrygdbrevProperties = dokprodInfotrygdbrevProperties;
	}

	@Scheduled(cron = "${dokprod.infotrygdbrev.batch.cron}")
	public Optional<JobExecution> startBatchIfReady() throws JobExecutionException {
		Set<String> remoteFiles = sftpFileService.listFiles();
		log.debug("bdok100 found files. files: {}", remoteFiles);
		if (!remoteFiles.contains(READY_FILE_NAME)) {
			if (!remoteFiles.isEmpty()) {
				log.debug("bdok100 found files, but no ready-marker. files: {}", remoteFiles);
				log.debug("bdok100 lager en ready.txt sjæl yolo");
				sftpFileService.createEmptyFile(READY_FILE_NAME);
			} else {
				Set<String> remoteIncludingDirs = sftpFileService.listFiles();
				log.debug("bdok100 found no files on sftp: ls: {}", remoteIncludingDirs);
			}
			return Optional.empty();
		}
		if (dokprodInfotrygdbrevProperties.resetStartedJobs()) {
			remoteFiles.stream()
				.filter(fileName -> fileName.startsWith(STARTED_FILE_PREFIX))
				.peek(filename -> log.info("bdok100 Deleting previous batch claim {}", filename))
				.forEach(sftpFileService::deleteFile);
			log.info("bdok100 resetStartedJobs=true, will not process any files");
		} else {
			var previousStartedMarker = remoteFiles.stream().filter(fileName -> fileName.startsWith(STARTED_FILE_PREFIX)).findAny();
			if (previousStartedMarker.isPresent()) {
				log.error("bdok100 aborted! A previous job has already claimed this round, marker: {}", previousStartedMarker.get());
				return Optional.empty();
			}
		}

		stageFiles(remoteFiles);
		Date startTime = new Date();
		String startedFileName = startedFileName(startTime);
		sftpFileService.createEmptyFile(startedFileName);
		log.info("bdok100 starting, files staged, claimed round with marker: {}", startedFileName);
		JobExecution jobExecution = jobService.start(
				new JobParametersBuilder()
						.addDate(START_TIME_KEY, startTime)
						.addJobParameter(CLEAN_KEY, dokprodInfotrygdbrevProperties.resetStartedJobs(), Boolean.class)
						.toJobParameters());
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			sftpFileService.deleteFile(startedFileName);
			log.info("bdok100 complete, removed marker {}", startedFileName);
		} else {
			log.error("bdok100 Something went wrong, batch status={}. Not removing batch-claiming marker: {}", jobExecution.getStatus(), startedFileName);
		}
		return Optional.of(jobExecution);
	}

	private String startedFileName(Date startTime) {
		return STARTED_FILE_PREFIX + FilenameHelper.formatDateToSeconds(startTime) + MARKER_FILE_SUFFIX;
	}

	private void stageFiles(Set<String> remoteFiles) {
		Map<String, Path> filesToStage = batchfilesToLocalPaths();
		Set<String> missingFiles = filesToStage.keySet().stream()
				.filter(fileName -> !remoteFiles.contains(fileName))
				.collect(Collectors.toSet());
		if (!missingFiles.isEmpty()) {
			throw new IllegalStateException("SFTP ready marker found, but required files are missing: " + missingFiles);
		}

		stagingService.prepareLocalWorkspace();
		filesToStage.forEach(stagingService::stageFile);

		log.info("bdok100 Staged {} SFTP files in local workspace {}", filesToStage.size(), batchParameters.inputFileLocation().getParent());
	}

	private Map<String, Path> batchfilesToLocalPaths() {
		return Map.of(
			FilenameHelper.JOURNALDATA_CSV, batchParameters.inputFileLocation().resolve(FilenameHelper.JOURNALDATA_CSV),
			FilenameHelper.LINJEDATA_TXT, batchParameters.inputFileLocation().resolve(FilenameHelper.LINJEDATA_TXT),
			FilenameHelper.KONTOR_CSV, batchParameters.staticInputFileLocation().resolve(FilenameHelper.KONTOR_CSV),
			FilenameHelper.VEDLEGG_CSV, batchParameters.staticInputFileLocation().resolve(FilenameHelper.VEDLEGG_CSV)
		);
	}
}
