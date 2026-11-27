package no.nav.dokprod.infotrygdbrev.common.support;

import no.nav.dokprod.infotrygdbrev.bdok100.config.Bdok100BatchParameters;
import no.nav.dokprod.infotrygdbrev.bdok100.support.LocalFileService;
import no.nav.dokprod.infotrygdbrev.bdok100.support.DatabaseOutputService;
import no.nav.dokprod.infotrygdbrev.bdok100.support.DatabaseOutputService.AvvikType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.START_TIME_KEY;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.JOURNALDATA_CSV;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.LINJEDATA_TXT;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.KONTOR_CSV;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.VEDLEGG_CSV;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.formatDateToSeconds;
import static no.nav.dokprod.infotrygdbrev.common.BDOKCommonBatchInputParameters.CLEAN_KEY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class JobCompletionFileHandlerTest {

	private final PostRunFileTransfer fileTransfer = mock(PostRunFileTransfer.class);
	private final DatabaseOutputService databaseOutputService = mock(DatabaseOutputService.class);
	private Bdok100BatchParameters batchParameters;
	private JobCompletionFileHandler handler;

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
		handler = new JobCompletionFileHandler(fileTransfer, new LocalFileService(batchParameters), databaseOutputService);
	}

	@Test
	public void shouldMoveAndCleanLocalFilesWhenCompleted() throws Exception {
		var output = batchParameters.outputFileLocation();
		Files.createDirectories(output);
		Files.writeString(output.resolve("result.txt"), "data");
		Files.createDirectories(batchParameters.inputFileLocation());
		Files.writeString(batchParameters.inputFileLocation().resolve(JOURNALDATA_CSV), "journal");
		Files.writeString(batchParameters.inputFileLocation().resolve(LINJEDATA_TXT), "linje");
		Files.createDirectories(batchParameters.staticInputFileLocation());
		Files.writeString(batchParameters.staticInputFileLocation().resolve(KONTOR_CSV), "kontor");
		Files.writeString(batchParameters.staticInputFileLocation().resolve(VEDLEGG_CSV), "vedlegg");
		JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "BDOK100"), new JobParameters());
		jobExecution.setExitStatus(ExitStatus.COMPLETED);
		ExecutionContext context = jobExecution.getExecutionContext();
		context.put(START_TIME_KEY, new Date(0));
		addProcessedSteps(jobExecution);

		handler.afterJob(jobExecution);

		String archive = "behandlet/bdok100-behandlet-" + formatDateToSeconds(new Date(0));
		verify(fileTransfer).moveFileIfExists(JOURNALDATA_CSV, archive + "/" + JOURNALDATA_CSV);
		verify(fileTransfer).moveFileIfExists(LINJEDATA_TXT, archive + "/" + LINJEDATA_TXT);
		verify(fileTransfer, never()).uploadContent(any(InputStream.class), any(String.class));
		verify(databaseOutputService).deleteForJob(1L);
		assertFalse(Files.exists(output.resolve("result.txt")));
		assertFalse(Files.exists(batchParameters.inputFileLocation().resolve(JOURNALDATA_CSV)));
		assertFalse(Files.exists(batchParameters.inputFileLocation().resolve(LINJEDATA_TXT)));
		assertFalse(Files.exists(batchParameters.staticInputFileLocation().resolve(KONTOR_CSV)));
		assertFalse(Files.exists(batchParameters.staticInputFileLocation().resolve(VEDLEGG_CSV)));
	}

	@Test
	public void shouldKeepLocalFilesIfUploadFails() throws Exception {
		Path output = batchParameters.outputFileLocation();
		Files.createDirectories(output);
		Path result = output.resolve("result.txt");
		Files.writeString(result, "data");
		JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "BDOK100"), new JobParameters());
		jobExecution.setExitStatus(ExitStatus.COMPLETED);
		jobExecution.getExecutionContext().put(START_TIME_KEY, new Date(0));
		addProcessedSteps(jobExecution);
		when(databaseOutputService.filesForJob(1L)).thenReturn(List.of(AvvikType.LINJEPRINTFIL));
		when(databaseOutputService.openStream(1L, AvvikType.LINJEPRINTFIL)).thenReturn(new ByteArrayInputStream(new byte[0]));
		doThrow(new IllegalStateException("upload failed")).when(fileTransfer)
			.uploadContent(any(InputStream.class), any(String.class));

		assertThrows(IllegalStateException.class, () -> handler.afterJob(jobExecution));

		assertTrue(Files.exists(result));
		assertTrue(jobExecution.getStatus() == BatchStatus.FAILED);
		assertTrue(jobExecution.getExitStatus().getExitCode().equals(ExitStatus.FAILED.getExitCode()));
		assertFalse(jobExecution.getFailureExceptions().isEmpty());
		verify(databaseOutputService, never()).deleteForJob(1L);
	}

	@Test
	public void shouldStreamDatabaseFilesAndDeleteOnlyAfterSuccessfulUpload() {
		JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "BDOK100"), new JobParameters());
		jobExecution.setExitStatus(ExitStatus.COMPLETED);
		jobExecution.getExecutionContext().put(START_TIME_KEY, new Date(0));
		addProcessedSteps(jobExecution);
		when(databaseOutputService.filesForJob(1L)).thenReturn(List.of(AvvikType.LINJEPRINTFIL, AvvikType.REPORT));
		when(databaseOutputService.openStream(1L, AvvikType.LINJEPRINTFIL)).thenReturn(new ByteArrayInputStream(new byte[0]));
		when(databaseOutputService.openStream(1L, AvvikType.REPORT)).thenReturn(new ByteArrayInputStream(new byte[0]));

		handler.afterJob(jobExecution);

		var order = inOrder(fileTransfer, databaseOutputService);
		order.verify(fileTransfer).uploadContent(any(InputStream.class), eq(AvvikType.LINJEPRINTFIL.filename(new Date(0))));
		order.verify(fileTransfer).uploadContent(any(InputStream.class), eq(AvvikType.REPORT.filename(new Date(0))));
		order.verify(databaseOutputService).deleteForJob(1L);
	}

	@Test
	public void shouldRetainDatabaseRowsWhenSftpUploadFails() {
		JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "BDOK100"), new JobParameters());
		jobExecution.setExitStatus(ExitStatus.COMPLETED);
		jobExecution.getExecutionContext().put(START_TIME_KEY, new Date(0));
		addProcessedSteps(jobExecution);
		when(databaseOutputService.filesForJob(1L)).thenReturn(List.of(AvvikType.LINJEPRINTFIL));
		when(databaseOutputService.openStream(1L, AvvikType.LINJEPRINTFIL)).thenReturn(new ByteArrayInputStream(new byte[0]));
		doThrow(new IllegalStateException("SFTP unavailable")).when(fileTransfer)
			.uploadContent(any(InputStream.class), any(String.class));

		assertThrows(IllegalStateException.class, () -> handler.afterJob(jobExecution));

		verify(databaseOutputService, never()).deleteForJob(1L);
		assertTrue(jobExecution.getStatus() == BatchStatus.FAILED);
	}

	@Test
	public void shouldUseBehandletOnWarning() {
		JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "BDOK100"), new JobParameters());
		jobExecution.setExitStatus(new ExitStatus("WARNING"));
		jobExecution.getExecutionContext().put(START_TIME_KEY, new Date(0));
		addProcessedSteps(jobExecution);

		handler.afterJob(jobExecution);

		verify(fileTransfer).moveFileIfExists(eq(JOURNALDATA_CSV), contains("behandlet/"));
	}

	@Test
	public void shouldUseJobNameInArchiveDirectory() {
		JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "OTHERJOB"), new JobParameters());
		jobExecution.setExitStatus(ExitStatus.COMPLETED);
		jobExecution.getExecutionContext().put(START_TIME_KEY, new Date(0));
		addProcessedSteps(jobExecution);

		handler.afterJob(jobExecution);

		verify(fileTransfer).moveFileIfExists(JOURNALDATA_CSV,
			"behandlet/otherjob-behandlet-" + formatDateToSeconds(new Date(0)) + "/" + JOURNALDATA_CSV);
	}

	@Test
	public void shouldHandleFailedJob() throws Exception {
		Path input = batchParameters.inputFileLocation();
		Files.createDirectories(input);
		Files.writeString(input.resolve(JOURNALDATA_CSV), "data");
		Path output = batchParameters.outputFileLocation();
		Files.createDirectories(output);
		Files.writeString(output.resolve("error.txt"), "error");
		JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "BDOK100"), new JobParameters());
		jobExecution.setStatus(BatchStatus.FAILED);
		jobExecution.setExitStatus(ExitStatus.FAILED);
		jobExecution.getExecutionContext().put(START_TIME_KEY, new Date(0));
		addProcessedSteps(jobExecution);
		when(databaseOutputService.filesForJob(1L)).thenReturn(List.of(AvvikType.REPORT));
		when(databaseOutputService.openStream(1L, AvvikType.REPORT)).thenReturn(new ByteArrayInputStream(new byte[0]));

		handler.afterJob(jobExecution);

		verify(fileTransfer).moveFileIfExists(JOURNALDATA_CSV, "failed/bdok100-failed-" + formatDateToSeconds(new Date(0)) + "/" + JOURNALDATA_CSV);
		verify(fileTransfer).uploadContent(any(InputStream.class), eq(AvvikType.REPORT.filename(new Date(0))));
		verify(databaseOutputService, never()).deleteForJob(1L);
		assertFalse(Files.exists(input.resolve(JOURNALDATA_CSV)));
		assertFalse(Files.exists(output.resolve("error.txt")));
	}

	@Test
	public void shouldNotFinalizeAfterInputValidationFailure() throws Exception {
		Path input = batchParameters.inputFileLocation();
		Files.createDirectories(input);
		Files.writeString(input.resolve(JOURNALDATA_CSV), "data");
		JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "BDOK100"), new JobParameters());
		jobExecution.setStatus(BatchStatus.FAILED);
		jobExecution.setExitStatus(ExitStatus.FAILED);
		jobExecution.addStepExecutions(List.of(new StepExecution(1L, "inputValidationStep", jobExecution)));

		handler.afterJob(jobExecution);

		assertTrue(Files.exists(input.resolve(JOURNALDATA_CSV)));
		verify(fileTransfer, never()).moveFileIfExists(any(String.class), any(String.class));
	}

	@Test
	public void shouldNotFinalizeStoppedJob() {
		JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "BDOK100"), new JobParameters());
		jobExecution.setStatus(BatchStatus.STOPPED);
		jobExecution.setExitStatus(ExitStatus.STOPPED);
		addProcessedSteps(jobExecution);

		handler.afterJob(jobExecution);

		verify(fileTransfer, never()).moveFileIfExists(any(String.class), any(String.class));
	}

	@Test
	public void shouldDoNothingForCleanOnly() {
		JobParameters params = new JobParameters(Set.of(new JobParameter<>(CLEAN_KEY, true, Boolean.class)));
		JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "BDOK100"), params);
		jobExecution.setExitStatus(ExitStatus.COMPLETED);
		jobExecution.getExecutionContext().put(START_TIME_KEY, new Date(0));
		addProcessedSteps(jobExecution);

		handler.afterJob(jobExecution);

		verify(fileTransfer, never()).moveFileIfExists(any(String.class), any(String.class));
		verify(fileTransfer, never()).uploadContent(any(InputStream.class), any(String.class));
	}

	private void addProcessedSteps(JobExecution jobExecution) {
		jobExecution.addStepExecutions(List.of(
			new StepExecution(1L, "inputValidationStep", jobExecution),
			new StepExecution(2L, "xmlDokumentbestillingWriterStep", jobExecution)));
	}
}
