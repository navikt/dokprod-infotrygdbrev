package no.nav.brevogarkiv.batch.common;

import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;

import java.util.Map;

/**
 * ChunkListener that throws exception if NO_OF_FAILURES_KEY exceeds MAX_FAILURES.
 */
public class MaxFailuresChunkListener implements ChunkListener<Object, Object> {

	public static final String NO_OF_FAILURES_KEY = "noOfFailures";

	private static final String EXCEPTION_MESSAGE_FORMAT = "Number of failures (%d) reached the maxFailures (%d) threshold.";

	@Override
	public void beforeChunk(ChunkContext context) {

	}

	@Override
	public void afterChunk(ChunkContext context) {
		Map<String, Object> jobExecutionContext = context.getStepContext().getJobExecutionContext();
		Long maxFailures = (Long) jobExecutionContext.get(CommonBatchInputParameters.MAX_FAILURES);

		if (jobExecutionContext.get(NO_OF_FAILURES_KEY) == null) {
			jobExecutionContext.put(NO_OF_FAILURES_KEY, 0L);
		}

		throwExceptionIfThresholdHasBeenReached((Long) jobExecutionContext.get(NO_OF_FAILURES_KEY), maxFailures);
	}

	private void throwExceptionIfThresholdHasBeenReached(long numberOfFailures, long maxFailures) {
		if (isThresholdReached(numberOfFailures, maxFailures)) {
			throw new MaxFailuresReachedException(String.format(EXCEPTION_MESSAGE_FORMAT, numberOfFailures, maxFailures));
		}
	}

	private boolean isThresholdReached(long numberOfFailures, long maxFailures) {
		return numberOfFailures >= maxFailures;
	}

	@Override
	public void afterChunkError(ChunkContext context) {

	}
}
