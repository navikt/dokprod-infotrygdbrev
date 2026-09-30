package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.Journaldata;
import no.nav.dokprod_infotrygdbrev.consumer.FinnEllerOpprettSakTo;

/**
 * Mapper for Bdokk100 call to gsak
 *
 */
public class Bdok100GsakMapper {

	public FinnEllerOpprettSakTo map(Journaldata journaldata) {
		return FinnEllerOpprettSakTo.builder()
				.fagomraade(journaldata.getDokumentTilhorendefagomraadekode())
				.fagsystem(journaldata.getBestillendeFagsystemkode())
				.fagsystemSakId(journaldata.getSaksNummer())
				.brukerId(journaldata.getGjelderID())
				.brukerType(FinnEllerOpprettSakTo.BrukerType.valueOf(journaldata.getGjelderType().name()))
				.build();
	}
}
