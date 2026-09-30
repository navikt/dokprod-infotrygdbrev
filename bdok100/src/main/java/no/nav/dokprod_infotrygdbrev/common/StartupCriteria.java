package no.nav.dokprod_infotrygdbrev.common;


import org.springframework.batch.infrastructure.item.ExecutionContext;

/**
 * Interface for custom checking of Batch startup.
 * <p/>
 * Used by BatchStartupValidationTasklet
 *
 */
public interface StartupCriteria {

	/**
	 * Called by Startup tasklet
	 */
	void check(ExecutionContext jobExecutionContext) throws Exception;
}
