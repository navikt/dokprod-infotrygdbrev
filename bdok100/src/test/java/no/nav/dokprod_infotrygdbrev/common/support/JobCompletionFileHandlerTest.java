package no.nav.dokprod_infotrygdbrev.common.support;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.BEHANDLET_FILE_LOCATION_KEY;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.FAILED_FILE_LOCATION_KEY;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.INPUT_FILE_LOCATION_KEY;
import static no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper.formatDateToSeconds;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.brevogarkiv.batch.common.provider.launch.util.NavExitStatus;
import no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.step.StepExecution;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

/**
 * Unit test for {@link JobCompletionFileHandler}
 *
 */
public class JobCompletionFileHandlerTest {

	private static final String JOB_NAME = "TESTJOB";
	private static final Date START_TIME = new Date();
	private static final String BEHANDLET_SUBFOLDER = JOB_NAME.toLowerCase() + "-behandlet-" + formatDateToSeconds(START_TIME);
	private static final String FAILED_SUBFOLDER = JOB_NAME.toLowerCase() + "-failed-" + formatDateToSeconds(START_TIME);

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private JobCompletionFileHandler jobCompletionFileHandler = new JobCompletionFileHandler();
	private JobExecution jobExecution;

	private Path in;
	private Path behandlet;
	private Path failed;

	@Before
	public void setUp() throws Exception {
		in = temporaryFolder.newFolder("ok").toPath();
		Files.createFile(in.resolve(FilenameHelper.JOURNALDATA_CSV));
		Files.createFile(in.resolve(FilenameHelper.LINJEDATA_TXT));

		behandlet = temporaryFolder.newFolder("behandlet").toPath();
		failed = temporaryFolder.newFolder("failed").toPath();
		JobParametersBuilder parametersBuilder = new JobParametersBuilder()
				.addString(INPUT_FILE_LOCATION_KEY, separatorsToUnix(in.toString()))
				.addString(BEHANDLET_FILE_LOCATION_KEY, separatorsToUnix(behandlet.toString()))
				.addString(FAILED_FILE_LOCATION_KEY, separatorsToUnix(failed.toString()));

		jobExecution = new JobExecution(1L, new JobInstance(1L, JOB_NAME), parametersBuilder.toJobParameters());
		jobExecution.getExecutionContext().put(CommonBatchInputParameters.START_TIME_KEY, START_TIME);
	}

	@Test
	public void shouldMoveFilesToBehandletOnCompleted() throws Exception {
		addSteps();
		jobExecution.setExitStatus(ExitStatus.COMPLETED);
		jobCompletionFileHandler.afterJob(jobExecution);

		assertTrue(Files.exists(behandlet.resolve(BEHANDLET_SUBFOLDER).resolve(FilenameHelper.JOURNALDATA_CSV)));
		assertTrue(Files.exists(behandlet.resolve(BEHANDLET_SUBFOLDER).resolve(FilenameHelper.LINJEDATA_TXT)));
		assertTrue(ArrayUtils.isEmpty(failed.toFile().list()));
	}

	@Test
	public void shouldMoveFilesToBehandletOnWarning() throws Exception {
		addSteps();
		jobExecution.setExitStatus(NavExitStatus.WARNING);
		jobCompletionFileHandler.afterJob(jobExecution);

		assertTrue(Files.exists(behandlet.resolve(BEHANDLET_SUBFOLDER).resolve(FilenameHelper.JOURNALDATA_CSV)));
		assertTrue(Files.exists(behandlet.resolve(BEHANDLET_SUBFOLDER).resolve(FilenameHelper.LINJEDATA_TXT)));
		assertTrue(ArrayUtils.isEmpty(failed.toFile().list()));
	}

	@Test
	public void shouldNotMoveIfOnlyOneStepExecuted() throws Exception {
		jobExecution.setExitStatus(ExitStatus.COMPLETED);
		jobExecution.addStepExecutions(Lists.newArrayList(new StepExecution(0L, "validationStep", jobExecution)));
		jobCompletionFileHandler.afterJob(jobExecution);

		assertTrue(Files.exists(in.resolve(FilenameHelper.JOURNALDATA_CSV)));
		assertTrue(Files.exists(in.resolve(FilenameHelper.LINJEDATA_TXT)));
		assertTrue(ArrayUtils.isEmpty(failed.toFile().list()));
		assertTrue(ArrayUtils.isEmpty(behandlet.toFile().list()));
	}

	@Test
	public void shouldNotMoveIfStopped() throws Exception {
		addSteps();
		jobExecution.setExitStatus(ExitStatus.STOPPED);
		jobCompletionFileHandler.afterJob(jobExecution);

		assertTrue(Files.exists(in.resolve(FilenameHelper.JOURNALDATA_CSV)));
		assertTrue(Files.exists(in.resolve(FilenameHelper.LINJEDATA_TXT)));
		assertTrue(ArrayUtils.isEmpty(failed.toFile().list()));
		assertTrue(ArrayUtils.isEmpty(behandlet.toFile().list()));
	}

	@Test
	public void shouldMoveFilesToFailedOtherwiseError() throws Exception {
		addSteps();
		jobExecution.setExitStatus(NavExitStatus.ERROR);
		jobCompletionFileHandler.afterJob(jobExecution);

		assertTrue(Files.exists(failed.resolve(FAILED_SUBFOLDER).resolve(FilenameHelper.JOURNALDATA_CSV)));
		assertTrue(Files.exists(failed.resolve(FAILED_SUBFOLDER).resolve(FilenameHelper.LINJEDATA_TXT)));
		assertTrue(ArrayUtils.isEmpty(behandlet.toFile().list()));
	}

	@Test
	public void shouldMoveFilesToFailedOtherwiseFailed() throws Exception {
		addSteps();
		jobExecution.setExitStatus(ExitStatus.FAILED);
		jobCompletionFileHandler.afterJob(jobExecution);

		assertTrue(Files.exists(failed.resolve(FAILED_SUBFOLDER).resolve(FilenameHelper.JOURNALDATA_CSV)));
		assertTrue(Files.exists(failed.resolve(FAILED_SUBFOLDER).resolve(FilenameHelper.LINJEDATA_TXT)));
		assertTrue(ArrayUtils.isEmpty(behandlet.toFile().list()));
	}

	private void addSteps() {
		jobExecution.addStepExecutions(Lists.newArrayList(
				new StepExecution(0L, "validationStep", jobExecution),
				new StepExecution(1L, "readStep", jobExecution)));
	}
}