package no.nav.brevogarkiv.batch.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copied from Stelvio and changed to remove user of InfoLogger.
 * 
 * @author Ole Hjalmar Herje, BEKK
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class ProgressLoggerListener implements ProgressListener {
	private Logger logger;
	private EventReportFormatter formatter;
	
	public ProgressLoggerListener(String logname, EventReportFormatter formatter) {
		this.logger = LoggerFactory.getLogger(logname);
		this.formatter = formatter;
	}
	
	public void finished(BatchCounter counter) {
		logger.info("Batch ended with progress counter status:");
		logCounters(counter);
	}

	public void progressed(BatchCounter counter) {
		logger.info("Batch progress status:");
		logCounters(counter);
	}

	public void started(BatchCounter counter) {
		logger.info("Batch started with following progress counters active:");
		logCounters(counter);
	}

	private void logCounters(BatchCounter counter) {
		logger.info(formatter.format(counter.getEventReport()));
	}

}
