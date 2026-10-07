package no.nav.brevogarkiv.batch.common;

import no.nav.brevogarkiv.batch.common.CounterEvent.EventType;

/**
 * Copied from Stelvio
 * 
 * Contains common batch events, see {@link CounterEvent}.
 * 
 * @author Ole Hjalmar Herje, BEKK
 *
 */
public final class CommonBatchEvents {
	
	private CommonBatchEvents() {
	}

	/**	Batch run */
	public static final CounterEvent JOB_EVENT = 
		CounterEvent.createCounterEvent(CommonBatchEvents.class, "job", "Kjører batch.", EventType.TECHNICAL);

}
