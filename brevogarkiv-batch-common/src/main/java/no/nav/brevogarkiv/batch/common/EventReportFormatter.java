package no.nav.brevogarkiv.batch.common;

import java.util.Map;

/**
 * Copied from Stelvio.
 * 
 * @author Ole Hjalmar Herje, BEKK
 */
public interface EventReportFormatter {

	String format(Map<CounterEvent, ? extends EventCounter> eventReport);

}
