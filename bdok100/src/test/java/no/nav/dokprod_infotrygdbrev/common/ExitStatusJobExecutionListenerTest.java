package no.nav.dokprod_infotrygdbrev.common;

import static no.nav.brevogarkiv.batch.common.MaxFailuresChunkListener.NO_OF_FAILURES_KEY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import no.nav.dokprod_infotrygdbrev.util.NavExitStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;

/**
 * Unit tests for ExitStatusJobExecutionListener
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ExitStatusJobExecutionListenerTest {

	private JobExecution jobExecution;

	private ExitStatusJobExecutionListener exitStatusJobExecutionListener = new ExitStatusJobExecutionListener();

	@Before
	public void setUp() throws Exception {
		jobExecution = MetaDataInstanceFactory.createJobExecution();
	}

	@Test
	public void shouldSetExitStatusWarningIfJobHasExceptions() throws Exception {
		jobExecution.addFailureException(new RuntimeException());
		jobExecution.setExitStatus(ExitStatus.COMPLETED);

		exitStatusJobExecutionListener.afterJob(jobExecution);

		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.WARNING));
	}

	@Test
	public void shouldSetExitStatusWarningIfAnyStepHasExceptions() throws Exception {
		jobExecution.setExitStatus(ExitStatus.COMPLETED);
		jobExecution.addStepExecution(new StepExecution("step", jobExecution));
		Iterables.getOnlyElement(jobExecution.getStepExecutions()).addFailureException(new RuntimeException());

		exitStatusJobExecutionListener.afterJob(jobExecution);

		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.WARNING));
	}

	@Test
	public void shouldSetNoFailuresToZeroOnRestart() throws Exception {
		exitStatusJobExecutionListener.beforeJob(jobExecution);
		assertThat(jobExecution.getExecutionContext().getLong(NO_OF_FAILURES_KEY), is(0L));
	}

	@Test
	public void shouldSetWarnIfStatusCheckIsWarn() throws Exception {
		jobExecution.setExitStatus(ExitStatus.COMPLETED);
		StatusChecker statusChecker = mock(StatusChecker.class);
		when(statusChecker.isWarning(jobExecution)).thenReturn(true);
		when(statusChecker.isError(jobExecution)).thenReturn(false);
		exitStatusJobExecutionListener.setStatusChecker(statusChecker);

		exitStatusJobExecutionListener.afterJob(jobExecution);
		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.WARNING));
	}

	@Test
	public void shouldSetWarnIfStatusCheckIsError() throws Exception {
		jobExecution.setExitStatus(ExitStatus.COMPLETED);
		StatusChecker statusChecker = mock(StatusChecker.class);
		when(statusChecker.isWarning(jobExecution)).thenReturn(false);
		when(statusChecker.isError(jobExecution)).thenReturn(true);
		exitStatusJobExecutionListener.setStatusChecker(statusChecker);

		exitStatusJobExecutionListener.afterJob(jobExecution);
		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.ERROR));
	}
}
