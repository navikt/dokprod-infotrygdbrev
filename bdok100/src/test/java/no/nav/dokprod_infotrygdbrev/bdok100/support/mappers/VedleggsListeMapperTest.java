package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe.Vedlegg;

/**
 * Unit test for {@link VedleggsListeMapper}
 *
 */
public class VedleggsListeMapperTest {
	private static final String BREVKODE = "brevkode";
	private static final String VEDLEGG_1 = "vedlegg1";
	private static final String VEDLEGG_2 = "vedlegg2";

	@Test
	public void shouldMap() throws Exception {
		VedleggsListe vedleggsListe = VedleggsListeMapper.map(BREVKODE + "," + VEDLEGG_1 + "," + VEDLEGG_2);
		assertThat(vedleggsListe, is(notNullValue()));
		assertThat(vedleggsListe.getBrevkode(), is(BREVKODE));
		assertThat(vedleggsListe.getVedleggs(), contains(new Vedlegg(VEDLEGG_1), new Vedlegg(VEDLEGG_2)));
	}

	
}