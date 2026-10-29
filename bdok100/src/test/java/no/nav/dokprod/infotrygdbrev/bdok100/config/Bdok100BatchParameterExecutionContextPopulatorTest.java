package no.nav.dokprod.infotrygdbrev.bdok100.config;

import org.junit.Test;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.infrastructure.item.ExecutionContext;

import java.nio.file.Path;
import java.util.Date;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.START_TIME_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Bdok100BatchParameterExecutionContextPopulatorTest {

	private final Bdok100BatchParameterExecutionContextPopulator populator =
			new Bdok100BatchParameterExecutionContextPopulator(new Bdok100BatchParameters(
					Path.of("input"),
					Path.of("static"),
					Path.of("output"),
					Path.of("failed"),
					Path.of("behandlet"),
					2,
					10));

	@Test
	public void shouldCopyStartTimeFromJobParametersToExecutionContext() {
		Date startTime = new Date(123456789L);
		ExecutionContext executionContext = new ExecutionContext();
		JobExecution jobExecution = jobExecution(
				new JobParametersBuilder()
						.addDate(START_TIME_KEY, startTime)
						.toJobParameters(),
				executionContext);

		populator.beforeJob(jobExecution);

		assertEquals(startTime, executionContext.get(START_TIME_KEY));
	}

	@Test
	public void shouldFailWhenStartTimeJobParameterIsMissing() {
		JobExecution jobExecution = jobExecution(new JobParameters(), new ExecutionContext());

		NullPointerException exception = assertThrows(
				NullPointerException.class,
				() -> populator.beforeJob(jobExecution));

		assertEquals("Missing required job parameter: " + START_TIME_KEY, exception.getMessage());
	}

	private JobExecution jobExecution(JobParameters jobParameters, ExecutionContext executionContext) {
		JobExecution jobExecution = mock(JobExecution.class);
		when(jobExecution.getJobParameters()).thenReturn(jobParameters);
		when(jobExecution.getExecutionContext()).thenReturn(executionContext);
		return jobExecution;
	}
}
