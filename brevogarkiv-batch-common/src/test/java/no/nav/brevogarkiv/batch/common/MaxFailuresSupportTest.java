package no.nav.brevogarkiv.batch.common;

import static no.nav.brevogarkiv.batch.common.MaxFailuresChunkListener.NO_OF_FAILURES_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;

/**
 * Unit tests for MaxFailuresSupport
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class MaxFailuresSupportTest {


	private MaxFailuresSupport maxFailuresSupport;

	private StepExecution stepExecution;

	@BeforeEach
	public void setUp() {
		maxFailuresSupport = mock(MaxFailuresSupport.class, Mockito.CALLS_REAL_METHODS);
		stepExecution = MetaDataInstanceFactory.createStepExecution();
		maxFailuresSupport.beforeStep(stepExecution);
	}

	@Test
	public void shouldIncrement() throws Exception {
		assertEquals(0L, getNumberOfFailures());

		maxFailuresSupport.incrementNumberOfFailures();

		assertEquals(1L, getNumberOfFailures());
	}

	@Test
	public void shouldIncrementBy2() throws Exception {
		assertEquals(0L, getNumberOfFailures());

		maxFailuresSupport.incrementNumberOfFailures(2);

		assertEquals(2L, getNumberOfFailures());
	}

	private long getNumberOfFailures() {
		return stepExecution.getJobExecution().getExecutionContext().getLong(NO_OF_FAILURES_KEY);
	}
}
