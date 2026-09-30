package no.nav.dokprod_infotrygdbrev.common.support;

import static no.nav.dokprod_infotrygdbrev.common.BDOKCommonBatchInputParameters.CLEAN_KEY;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;


/**
 * Unit test for {@link CleanableOnlyEmptyDecider}
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CleanableOnlyEmptyDeciderTest {

	@Mock
	private JpaRepository jpaRepositoryMock;
	@Mock
	private JobExplorer jobExplorerMock;
	@InjectMocks
	private CleanableOnlyEmptyDecider cleanableOnlyEmptyDecider;

	@Test
	public void shouldDecideStartOnFirstRunWithEmptyWorktable() throws Exception {
		when(jpaRepositoryMock.count()).thenReturn(0L);
		FlowExecutionStatus decide = cleanableOnlyEmptyDecider.decide(new JobExecution(1L, new JobInstance(1L, "test"), new JobParameters()), null);

		assertThat(decide, is(CleanableOnlyEmptyDecider.START));
	}

	@Test
	public void shouldThrowExceptionIfStartAndNotEmptyWorkTable() throws Exception {
		when(jpaRepositoryMock.count()).thenReturn(1L);

		FlowExecutionStatus decide = cleanableOnlyEmptyDecider.decide(new JobExecution(1L, new JobInstance(1L, "test"), new JobParameters()), null);

		assertThat(decide, is(FlowExecutionStatus.FAILED));
	}

	@Test
	public void shouldAllowRestartWithElementsInWorktable() throws Exception {
		mockRestart();
		JobExecution execution = new JobExecution(1L, new JobInstance(3L, "RestartJob"), new JobParameters());

		FlowExecutionStatus decide = cleanableOnlyEmptyDecider.decide(execution, null);

		assertThat(decide, is(CleanableOnlyEmptyDecider.START));
	}

	@Test
	public void shouldDecideCleanIfParameterClean() throws Exception {
		FlowExecutionStatus decide = cleanableOnlyEmptyDecider.decide(new JobExecution(1L, new JobInstance(1L, "test"), new JobParameters(Set.of(new JobParameter<>(CLEAN_KEY, "true", String.class)))), null);

		assertThat(decide, is(CleanableOnlyEmptyDecider.CLEAN));
	}

	private void mockRestart() {
		when(jobExplorerMock.getJobExecutions(any(JobInstance.class))).thenReturn(Arrays.asList(new JobExecution(1L, new JobInstance(1L, "test"), new JobParameters()), new JobExecution(2L, null, null)));
	}

}