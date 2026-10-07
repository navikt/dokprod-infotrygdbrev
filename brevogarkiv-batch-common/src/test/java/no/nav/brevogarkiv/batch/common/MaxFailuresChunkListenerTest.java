package no.nav.brevogarkiv.batch.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.test.MetaDataInstanceFactory;

/**
 * Unit test for {@link MaxFailuresChunkListener}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@ExtendWith(MockitoExtension.class)
public class MaxFailuresChunkListenerTest {

	@InjectMocks
	private MaxFailuresChunkListener listener;

	@Test
	public void shouldThrowWhenMaxFailuresIsExceeded() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		jobExecution.getExecutionContext().putLong(MaxFailuresChunkListener.NO_OF_FAILURES_KEY, 2);
		jobExecution.getExecutionContext().putLong(CommonBatchInputParameters.MAX_FAILURES, 1);

		ChunkContext context = createChunkContext(jobExecution);

		MaxFailuresReachedException exception = assertThrows(MaxFailuresReachedException.class,
				() -> listener.afterChunk(context));
		assertEquals("Number of failures (2) reached the maxFailures (1) threshold.", exception.getMessage());
	}

	@Test
	public void shouldThrowWhenMaxFailuresIsEqual() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		jobExecution.getExecutionContext().putLong(MaxFailuresChunkListener.NO_OF_FAILURES_KEY, 1);
		jobExecution.getExecutionContext().putLong(CommonBatchInputParameters.MAX_FAILURES, 1);

		ChunkContext context = createChunkContext(jobExecution);

		MaxFailuresReachedException exception = assertThrows(MaxFailuresReachedException.class,
				() -> listener.afterChunk(context));
		assertEquals("Number of failures (1) reached the maxFailures (1) threshold.", exception.getMessage());
	}

	@Test
	public void shouldNotThrowWhenMaxFailuresIsBelow() throws Exception {
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		jobExecution.getExecutionContext().putLong(MaxFailuresChunkListener.NO_OF_FAILURES_KEY, 0);
		jobExecution.getExecutionContext().putLong(CommonBatchInputParameters.MAX_FAILURES, 1);

		ChunkContext context = createChunkContext(jobExecution);

		listener.afterChunk(context);
	}

	private ChunkContext createChunkContext(JobExecution jobExecution) {
		return new ChunkContext(new StepContext(new StepExecution("Step", jobExecution)));
	}
}
