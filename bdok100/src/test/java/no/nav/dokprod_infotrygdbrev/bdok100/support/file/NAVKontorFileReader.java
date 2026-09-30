package no.nav.dokprod_infotrygdbrev.bdok100.support.file;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.NAVKontor;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.NAVKontorMapper;
import no.nav.dokprod_infotrygdbrev.common.file.ObjectFromFileReader;

/**
 * Test helper for NAV kontor files
 *
 */
public class NAVKontorFileReader extends ObjectFromFileReader<NAVKontor> {

	@Override
	public NAVKontor map(String string) {
		return NAVKontorMapper.map(string);
	}
}
