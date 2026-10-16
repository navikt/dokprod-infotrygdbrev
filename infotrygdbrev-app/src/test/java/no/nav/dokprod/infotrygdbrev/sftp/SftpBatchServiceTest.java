package no.nav.dokprod.infotrygdbrev.sftp;

import no.nav.dokprod.infotrygdbrev.batch.Bdok100JobService;
import no.nav.dokprod.infotrygdbrev.bdok100.config.DokprodInfotrygdbrevProperties;
import org.apache.sshd.sftp.client.SftpClient;
import org.junit.Test;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

import java.time.Duration;
import java.util.Optional;

import static no.nav.dokprod.infotrygdbrev.sftp.SftpBatchService.READY_FILE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class SftpBatchServiceTest {

	public static final String REMOTE_DIRECTORY = "/input";

	private final SftpRemoteFileTemplate remoteFileTemplate = mock(SftpRemoteFileTemplate.class);
	private final Bdok100JobService jobService = mock(Bdok100JobService.class);
	private final DokprodInfotrygdbrevProperties properties = new DokprodInfotrygdbrevProperties(null, null, new DokprodInfotrygdbrevProperties.SftpProperties(
		"localhost",
		22,
		"user",
		"private-key",
		"",
		"known-hosts",
		REMOTE_DIRECTORY,
		Duration.ofSeconds(30)
	));
	private final SftpBatchService service = new SftpBatchService(remoteFileTemplate, properties, jobService);

	@Test
	public void shouldNotStartBatchWhenReadyFileIsMissing() throws Exception {
		when(remoteFileTemplate.list(REMOTE_DIRECTORY)).thenReturn(new SftpClient.DirEntry[0]);

		Optional<JobExecution> execution = service.startBatchIfReady(new JobParameters());

		assertTrue(execution.isEmpty());
		verifyNoInteractions(jobService);
	}

	@Test
	public void shouldStartBatchWhenReadyFileExists() throws Exception {
		SftpClient.DirEntry readyFile = file(READY_FILE_NAME);
		JobParameters jobParameters = new JobParameters();
		JobExecution expectedExecution = mock(JobExecution.class);
		when(remoteFileTemplate.list(REMOTE_DIRECTORY)).thenReturn(new SftpClient.DirEntry[]{readyFile});
		when(jobService.start(jobParameters)).thenReturn(expectedExecution);

		Optional<JobExecution> execution = service.startBatchIfReady(jobParameters);

		assertEquals(Optional.of(expectedExecution), execution);
		verify(jobService).start(jobParameters);
	}

	private SftpClient.DirEntry file(String name) {
		SftpClient.DirEntry entry = mock(SftpClient.DirEntry.class);
		SftpClient.Attributes attributes = mock(SftpClient.Attributes.class);
		when(entry.getFilename()).thenReturn(name);
		when(entry.getAttributes()).thenReturn(attributes);
		when(attributes.isDirectory()).thenReturn(false);
		return entry;
	}
}
