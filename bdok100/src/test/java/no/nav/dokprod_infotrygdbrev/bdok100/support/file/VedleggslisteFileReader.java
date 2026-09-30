package no.nav.dokprod_infotrygdbrev.bdok100.support.file;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.VedleggsListeMapper;
import no.nav.dokprod_infotrygdbrev.common.file.ObjectFromFileReader;

/**
 * * Test helper for vedleggfiles
 *
 */
public class VedleggslisteFileReader extends ObjectFromFileReader<VedleggsListe> {

	@Override
	public VedleggsListe map(String string) {
		return VedleggsListeMapper.map(string);
	}
}
