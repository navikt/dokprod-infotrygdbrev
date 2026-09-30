package no.nav.dokprod_infotrygdbrev.bdok100.config.config.bdok100;

import no.nav.brevogarkiv.batch.common.ExecutionContextWorkUnitCompletionPolicy;
import no.nav.brevogarkiv.batch.common.LogContextListener;
import no.nav.brevogarkiv.batch.common.ProgressNotifier;
import org.springframework.batch.infrastructure.repeat.exception.ExceptionHandler;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Common functionality for steps in BDOK100
 *
 */
public abstract class AbstractBdok100StepConfig {
	@Autowired
	protected ExecutionContextWorkUnitCompletionPolicy workUnitCompletionPolicy;
	@Autowired
	protected LogContextListener logContextListener;
	@Autowired
	protected ExceptionHandler bdok100ExceptionHandler;
	@Autowired
	protected ProgressNotifier bdok100BatchCounterLogger;
}
