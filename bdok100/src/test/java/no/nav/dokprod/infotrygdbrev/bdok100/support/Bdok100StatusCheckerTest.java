package no.nav.dokprod.infotrygdbrev.bdok100.support;

import static no.nav.dokprod.infotrygdbrev.common.BDOKCommonBatchInputParameters.CLEAN_KEY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.dokprod.infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod.infotrygdbrev.common.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

public class Bdok100StatusCheckerTest {

	private JpaRepository repository = mock(JpaRepository.class);

	private Bdok100StatusChecker checker = new Bdok100StatusChecker();
	private JobExecution jobExecution = new JobExecution(1L, new JobInstance(1L, "test"), new JobParameters());

	private TestUtils.MockAppender appender = TestUtils.getMockedAppender(Bdok100StatusChecker.class);

	@Before
	public void setUp() throws Exception {
		setUpExecutionContext();
		checker.setRepo(repository);
	}

	private void setUpExecutionContext() {
		jobExecution.getExecutionContext().putLong(Bdok100Constants.BEHANDLET_COUNT, 1L);
		jobExecution.getExecutionContext().put(Bdok100Constants.IS_WARNING_EXIT, false);
	}

	@Test
	public void shouldSayAllOk() throws Exception {
		assertFalse(checker.isWarning(jobExecution));
		assertFalse(checker.isError(jobExecution));
		appender.verifyZeroInteractions();
	}

	@Test
	public void shouldReportWarningIfWarnings() throws Exception {
		jobExecution.getExecutionContext().put(Bdok100Constants.IS_WARNING_EXIT, true);
		assertTrue(checker.isWarning(jobExecution));
		assertFalse(checker.isError(jobExecution));
		appender.verifyZeroInteractions();
	}

	@Test
	public void shouldReportWarningIfBehandletNone() throws Exception {
		jobExecution.getExecutionContext().putLong(Bdok100Constants.BEHANDLET_COUNT, 0L);
		assertTrue(checker.isWarning(jobExecution));
		assertFalse(checker.isError(jobExecution));
		appender.verify("BDOK100 avsluttet med WARNING siden ingen rader ble funnet i input, Journaldata/ Linjeprintfil er tom");
	}

	@Test
	public void shouldNotReportWarningIfBehandletNoneOnCleanRun() throws Exception {
		JobParameters jobParameters = new JobParametersBuilder()
				.addString(CLEAN_KEY, "true")
				.toJobParameters();
		jobExecution = new JobExecution(1L, new JobInstance(1L, "test"), jobParameters);
		setUpExecutionContext();
		jobExecution.getExecutionContext().putLong(Bdok100Constants.BEHANDLET_COUNT, 0L);
		assertFalse(checker.isWarning(jobExecution));
		assertFalse(checker.isError(jobExecution));
		appender.verifyZeroInteractions();
	}

	@Test
	public void shouldReportErrorIfArbTblNotEmpty() throws Exception {
		when(repository.count()).thenReturn(1L);
		assertFalse(checker.isWarning(jobExecution));
		assertTrue(checker.isError(jobExecution));
		appender.verifyZeroInteractions();
	}
}