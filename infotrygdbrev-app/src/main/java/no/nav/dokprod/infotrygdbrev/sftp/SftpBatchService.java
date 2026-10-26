package no.nav.dokprod.infotrygdbrev.sftp;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokprod.infotrygdbrev.batch.Bdok100JobService;
import no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper;
import no.nav.dokprod.infotrygdbrev.bdok100.config.bdok100.Bdok100BatchParameters;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SftpBatchService {

	public static final String READY_FILE_NAME = "ready.txt";

	private final SftpFileService sftpFileService;
	private final SftpToLocalFileStagingService stagingService;
	private final Bdok100JobService jobService;
	private final Bdok100BatchParameters batchParameters;

	public SftpBatchService(
			SftpFileService sftpFileService,
			SftpToLocalFileStagingService stagingService,
			Bdok100JobService jobService,
			Bdok100BatchParameters batchParameters
	) {
		this.sftpFileService = sftpFileService;
		this.stagingService = stagingService;
		this.jobService = jobService;
		this.batchParameters = batchParameters;
	}

	public synchronized Optional<JobExecution> startBatchIfReady(JobParameters jobParameters) throws JobExecutionException {
		Set<String> remoteFiles = sftpFileService.listFiles();
		if (!remoteFiles.contains(READY_FILE_NAME)) {
			return Optional.empty();
		}

		stageFiles(remoteFiles);
		log.info("SFTP files are staged, starting BDOK100");
		return Optional.of(jobService.start(jobParameters));
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

		log.info("Staged {} SFTP files in local workspace {}", filesToStage.size(), batchParameters.inputFileLocation().getParent());
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
