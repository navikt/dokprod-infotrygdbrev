package no.nav.dokprod_infotrygdbrev.bdok100;

import no.nav.brevogarkiv.batch.common.CounterEvent;

/**
 * Logged events for the BDOK100 batch
 *
 */
public final class Bdok100Events {

	public static final CounterEvent JOURNALDATA_COUNTER =
			CounterEvent.createCounterEvent(Bdok100Events.class,
					"journaldata",
					"Rows read journaldata",
					CounterEvent.EventType.FUNCTIONAL);

	public static final CounterEvent LINJEDATA_COUNTER =
			CounterEvent.createCounterEvent(Bdok100Events.class,
					"linjedata",
					"Rows read linjedata",
					CounterEvent.EventType.FUNCTIONAL);

	public static final CounterEvent SAK_COUNTER =
			CounterEvent.createCounterEvent(Bdok100Events.class,
					"sak",
					"finnsak og eller opprettsak",
					CounterEvent.EventType.TECHNICAL);

	public static final CounterEvent LINJEDATA_KONTROLLRAPPORT_COUNTER =
			CounterEvent.createCounterEvent(Bdok100Events.class,
					"linjedata_kontrollrapport",
					"Linjedata inneholder feil som kun skal rapporteres",
					CounterEvent.EventType.FUNCTIONAL);

	public static final CounterEvent LINJEDATA_AVVIK_COUNTER =
			CounterEvent.createCounterEvent(Bdok100Events.class,
					"linjedata_avvik",
					"Linjedata avvik",
					CounterEvent.EventType.FUNCTIONAL);

	public static final CounterEvent JOURNALDATA_AVVIK_COUNTER =
			CounterEvent.createCounterEvent(Bdok100Events.class,
					"journaldata_avvik",
					"Journaldata avvik",
					CounterEvent.EventType.FUNCTIONAL);

	public static final CounterEvent NAVKONTOR_LOADED =
			CounterEvent.createCounterEvent(Bdok100Events.class,
					"navKontor", "Antall NAV-kontor lest", CounterEvent.EventType.FUNCTIONAL);

	public static final CounterEvent NAVKONTOR_LOAD_FAILED =
			CounterEvent.createCounterEvent(Bdok100Events.class,
					"navKontor_avvik", "Antall ugyldige NAV-kontor", CounterEvent.EventType.FUNCTIONAL);

	public static final CounterEvent VEDLEGGSLISTE_LOADED =
			CounterEvent.createCounterEvent(Bdok100Events.class,
					"vedlegg", "Antall Vedleggslister lest", CounterEvent.EventType.FUNCTIONAL);

	private Bdok100Events() {
	}
}
