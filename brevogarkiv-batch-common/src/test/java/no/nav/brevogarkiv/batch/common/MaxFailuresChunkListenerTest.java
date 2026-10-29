package no.nav.brevogarkiv.batch.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.test.MetaDataInstanceFactory;

public class MaxFailuresChunkListenerTest {

	@Test
	public void shouldThrowWhenMaxFailuresIsExceeded() throws Exception {
		MaxFailuresChunkListener listener = new MaxFailuresChunkListener(1);
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		jobExecution.getExecutionContext().putLong(MaxFailuresChunkListener.NO_OF_FAILURES_KEY, 2);

		ChunkContext context = createChunkContext(jobExecution);

		MaxFailuresReachedException exception = assertThrows(MaxFailuresReachedException.class,
				() -> listener.afterChunk(context));
		assertEquals("Number of failures (2) reached the maxFailures (1) threshold.", exception.getMessage());
	}

	@Test
	public void shouldThrowWhenMaxFailuresIsEqual() throws Exception {
		MaxFailuresChunkListener listener = new MaxFailuresChunkListener(1);
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		jobExecution.getExecutionContext().putLong(MaxFailuresChunkListener.NO_OF_FAILURES_KEY, 1);

		ChunkContext context = createChunkContext(jobExecution);

		MaxFailuresReachedException exception = assertThrows(MaxFailuresReachedException.class,
				() -> listener.afterChunk(context));
		assertEquals("Number of failures (1) reached the maxFailures (1) threshold.", exception.getMessage());
	}

	@Test
	public void shouldNotThrowWhenMaxFailuresIsBelow() throws Exception {
		MaxFailuresChunkListener listener = new MaxFailuresChunkListener(1);
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		jobExecution.getExecutionContext().putLong(MaxFailuresChunkListener.NO_OF_FAILURES_KEY, 0);

		ChunkContext context = createChunkContext(jobExecution);

		listener.afterChunk(context);
	}

	private ChunkContext createChunkContext(JobExecution jobExecution) {
		return new ChunkContext(new StepContext(new StepExecution("Step", jobExecution)));
	}
}
