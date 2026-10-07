package no.nav.brevogarkiv.batch.common;

import static no.nav.brevogarkiv.batch.common.MaxFailuresChunkListener.NO_OF_FAILURES_KEY;

import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.infrastructure.item.ExecutionContext;

/**
 * Support class that increments the default failure counter.
 * <p/>
 * Must be extended by a step component (reader, processor, writer).
 * <p/>
 * If the class is extended by a Tasklet or a component part of a composite step component, the component also needs to
 * be registered as a step listener (necessary because the @BeforeStep method not is invoked in these situations).
 * <p>
 * See https://jira.spring.io/browse/BATCH-2169 for detailed information.
 *
 * @author Roar Bjurstrøm, Visma Consulting
 */
public abstract class MaxFailuresSupport {

	private ExecutionContext jobExecutionContext;

	@BeforeStep
	public void beforeStep(StepExecution stepExecution) {
		jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();

		if (jobExecutionContext.get(NO_OF_FAILURES_KEY) == null) {
			jobExecutionContext.putLong(NO_OF_FAILURES_KEY, 0);
		}
	}

	/**
	 * Increments number of failures by one
	 */
	protected void incrementNumberOfFailures() {
		updateNumberOfFailuresBy(1);
	}

	/**
	 * Increments number of failures by count
	 *
	 * @param addNumberOfFailures The count of which to add to the number of failures
	 */
	protected void incrementNumberOfFailures(long addNumberOfFailures) {
		updateNumberOfFailuresBy(addNumberOfFailures);
	}

	private void updateNumberOfFailuresBy(long count) {
		long numberOfFailures = jobExecutionContext.getLong(NO_OF_FAILURES_KEY);
		jobExecutionContext.putLong(NO_OF_FAILURES_KEY, numberOfFailures + count);
	}
}
