package no.nav.dokprod.infotrygdbrev.sftp;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokprod.infotrygdbrev.batch.Bdok100JobService;
import no.nav.dokprod.infotrygdbrev.bdok100.config.DokprodInfotrygdbrevProperties;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SftpBatchService {

	public static final String READY_FILE_NAME = "ready.txt";

	private final SftpRemoteFileTemplate remoteFileTemplate;
	private final DokprodInfotrygdbrevProperties.SftpProperties sftpProperties;
	private final Bdok100JobService jobService;

	public SftpBatchService(
			SftpRemoteFileTemplate remoteFileTemplate,
			DokprodInfotrygdbrevProperties dokprodInfotrygdbrevProperties,
			Bdok100JobService jobService
	) {
		this.remoteFileTemplate = remoteFileTemplate;
		this.sftpProperties = dokprodInfotrygdbrevProperties.sftp();
		this.jobService = jobService;
	}

	/// **denne har kanskje ikke noe formål**
	public List<String> listFiles() {
		return Arrays.stream(remoteFileTemplate.list(sftpProperties.remoteDirectory()))
				.filter(entry -> !entry.getAttributes().isDirectory())
				.map(SftpClient.DirEntry::getFilename)
				.sorted()
				.toList();
	}

	public void readFile(String fileName, OutputStream outputStream) {
		boolean read = remoteFileTemplate.get(remotePath(fileName), inputStream -> inputStream.transferTo(outputStream));
		if (!read) {
			throw new IllegalStateException("SFTP file not found: " + fileName);
		}
	}

	public Optional<JobExecution> startBatchIfReady(JobParameters jobParameters) throws JobExecutionException {
		if (!listFiles().contains(READY_FILE_NAME)) {
			return Optional.empty();
		}

		log.info("Found {} on SFTP server, starting BDOK100", READY_FILE_NAME);
		return Optional.of(jobService.start(jobParameters));
	}

	private String remotePath(String fileName) {
		String remoteDirectory = sftpProperties.remoteDirectory();
		return remoteDirectory.endsWith("/") ? remoteDirectory + fileName : remoteDirectory + "/" + fileName;
	}
}
