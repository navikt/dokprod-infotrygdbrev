package no.nav.brevogarkiv.batch.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.repeat.RepeatContext;
import org.springframework.batch.infrastructure.repeat.exception.ExceptionHandler;

/**
 * ExceptionHandler that logs exceptions. 
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class LoggingExceptionHandler implements ExceptionHandler {
	
	private Logger logger;

	/**
	 * Constructs a new LoggingExceptionHandler.
	 *
	 * @param logname The logname where exceptions will be logged
	 */
	public LoggingExceptionHandler(String logname) {
		this.logger = LoggerFactory.getLogger(logname);
	}

	/** 
	 * Logs the exception and re-throws the throwable.
	 * 
	 * {@inheritDoc} */
	public void handleException(RepeatContext context, Throwable throwable) throws Throwable {
		logger.error("Batch threw exception: ", throwable);
		throw throwable;	
	}

		

}

