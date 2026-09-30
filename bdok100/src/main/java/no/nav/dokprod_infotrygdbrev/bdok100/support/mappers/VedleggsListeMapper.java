package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe;

/**
 * Mapper for Vedleggsliste CSV
 *
 */
public class VedleggsListeMapper {

	public static VedleggsListe map(String vedleggLine) {
		String[] codes = vedleggLine.split(",");
		VedleggsListe liste = new VedleggsListe();

		if (codes.length > 0) {
			liste.setBrevkode(codes[0]);
		}
		if (codes.length > 1) {
			liste.addVedlegg(codes[1]);
		}
		if (codes.length > 2) {
			liste.addVedlegg(codes[2]);
		}

		return liste;
	}
}
