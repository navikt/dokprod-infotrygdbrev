package no.nav.dokprod.infotrygdbrev.sftp;

import no.nav.dokprod.infotrygdbrev.batch.Bdok100JobService;
import no.nav.dokprod.infotrygdbrev.bdok100.config.Bdok100BatchParameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;

import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.START_TIME_KEY;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.JOURNALDATA_CSV;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.KONTOR_CSV;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.LINJEDATA_TXT;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.VEDLEGG_CSV;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.formatDateToSeconds;
import static no.nav.dokprod.infotrygdbrev.sftp.SftpBatchService.READY_FILE_NAME;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class SftpBatchServiceTest {

	private final SftpFileService sftpFileService = mock(SftpFileService.class);
	private final SftpToLocalFileStagingService stagingService = mock(SftpToLocalFileStagingService.class);
	private final Bdok100JobService jobService = mock(Bdok100JobService.class);
	private Bdok100BatchParameters batchParameters;
	private SftpBatchService service;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setUp() {
		Path root = temporaryFolder.getRoot().toPath();
		batchParameters = new Bdok100BatchParameters(
				root.resolve("input"),
				root.resolve("static"),
				root.resolve("output"),
				root.resolve("failed"),
				root.resolve("behandlet"),
				2,
				10);
		service = new SftpBatchService(sftpFileService, stagingService, jobService, batchParameters);
	}

	@Test
	public void shouldNotStartBatchWhenReadyFileIsMissing() throws Exception {
		when(sftpFileService.listFiles()).thenReturn(Set.of(JOURNALDATA_CSV));

		Optional<JobExecution> execution = service.startBatchIfReady();

		assertTrue(execution.isEmpty());
		verifyNoInteractions(stagingService, jobService);
	}

	@Test
	public void shouldNotStartBatchWhenStartedMarkerExists() throws Exception {
		when(sftpFileService.listFiles()).thenReturn(Set.of(
				READY_FILE_NAME,
				"started-2026-10-27_14-30-00.txt"));

		Optional<JobExecution> execution = service.startBatchIfReady();

		assertTrue(execution.isEmpty());
		verifyNoInteractions(stagingService, jobService);
		verify(sftpFileService, never()).createEmptyFile(any(String.class));
	}

	@Test
	public void shouldStageFilesAndStartBatchWhenReadyFileExists() throws Exception {
		JobExecution expectedExecution = mock(JobExecution.class);
		ArgumentCaptor<JobParameters> jobParametersCaptor = ArgumentCaptor.forClass(JobParameters.class);
		when(sftpFileService.listFiles()).thenReturn(requiredRemoteFiles());
		when(jobService.start(any(JobParameters.class))).thenReturn(expectedExecution);
		when(expectedExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);

		Optional<JobExecution> execution = service.startBatchIfReady();

		assertEquals(Optional.of(expectedExecution), execution);
		verify(stagingService).prepareLocalWorkspace();
		verify(stagingService).stageFile(JOURNALDATA_CSV, batchParameters.inputFileLocation().resolve(JOURNALDATA_CSV));
		verify(stagingService).stageFile(LINJEDATA_TXT, batchParameters.inputFileLocation().resolve(LINJEDATA_TXT));
		verify(stagingService).stageFile(KONTOR_CSV, batchParameters.staticInputFileLocation().resolve(KONTOR_CSV));
		verify(stagingService).stageFile(VEDLEGG_CSV, batchParameters.staticInputFileLocation().resolve(VEDLEGG_CSV));
		verify(jobService).start(jobParametersCaptor.capture());
		Date startTime = jobParametersCaptor.getValue().getDate(START_TIME_KEY);
		assertNotNull(startTime);
		String startedFileName = "started-" + formatDateToSeconds(startTime) + ".txt";
		verify(sftpFileService).createEmptyFile(startedFileName);
		verify(sftpFileService).deleteFile(startedFileName);
	}

	@Test
	public void shouldNotStageOrStartBatchWhenRequiredFileIsMissing() {
		when(sftpFileService.listFiles()).thenReturn(Set.of(
				READY_FILE_NAME,
				JOURNALDATA_CSV,
				LINJEDATA_TXT,
				KONTOR_CSV));

		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				() -> service.startBatchIfReady());

		assertThat(exception.getMessage(), containsString(VEDLEGG_CSV));
		verifyNoInteractions(stagingService, jobService);
	}

	@Test
	public void shouldNotStartBatchWhenStagingFails() {
		when(sftpFileService.listFiles()).thenReturn(requiredRemoteFiles());
		doThrow(new IllegalStateException("staging failed"))
				.when(stagingService)
				.stageFile(JOURNALDATA_CSV, batchParameters.inputFileLocation().resolve(JOURNALDATA_CSV));

		assertThrows(
				IllegalStateException.class,
				() -> service.startBatchIfReady());

		verifyNoInteractions(jobService);
	}

	@Test
	public void shouldNotStartBatchWhenStartedMarkerCannotBeCreated() {
		when(sftpFileService.listFiles()).thenReturn(requiredRemoteFiles());
		doThrow(new IllegalStateException("write failed"))
				.when(sftpFileService)
				.createEmptyFile(any(String.class));

		assertThrows(
				IllegalStateException.class,
				() -> service.startBatchIfReady());

		verifyNoInteractions(jobService);
	}

	@Test
	public void shouldKeepStartedMarkerWhenBatchFails() throws Exception {
		JobExecution failedExecution = mock(JobExecution.class);
		when(sftpFileService.listFiles()).thenReturn(requiredRemoteFiles());
		when(jobService.start(any(JobParameters.class))).thenReturn(failedExecution);
		when(failedExecution.getStatus()).thenReturn(BatchStatus.FAILED);

		Optional<JobExecution> execution = service.startBatchIfReady();

		assertEquals(Optional.of(failedExecution), execution);
		verify(sftpFileService, never()).deleteFile(any(String.class));
	}

	private Set<String> requiredRemoteFiles() {
		return Set.of(
				READY_FILE_NAME,
				JOURNALDATA_CSV,
				LINJEDATA_TXT,
				KONTOR_CSV,
				VEDLEGG_CSV);
	}
}
