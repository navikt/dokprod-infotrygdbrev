package no.nav.dokprod_infotrygdbrev.bdok100.domain;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link VedleggsListe}
 *
 */
public class VedleggsListeTest {

	private VedleggsListe validVedleggsliste;
	private VedleggsListe invalidVedleggsliste;

	@Before
	public void setUp() throws Exception {
		validVedleggsliste = new VedleggsListe();
		validVedleggsliste.setBrevkode("SP99");
		validVedleggsliste.addVedlegg("NAV_21-00.00");
		validVedleggsliste.addVedlegg("NAV_21-12.05");

		invalidVedleggsliste = new VedleggsListe();
		invalidVedleggsliste.setBrevkode("SP99");
		invalidVedleggsliste.addVedlegg("NAV_21-00.00");
		invalidVedleggsliste.addVedlegg("NAV_21-12.05");
	}

	@Test
	public void getBrevkode() throws Exception {
		assertThat(validVedleggsliste.getBrevkode(), is("SP99"));
	}

	@Test
	public void setBrevkode() throws Exception {
		assertThat(validVedleggsliste.getBrevkode(), is("SP99"));
		validVedleggsliste.setBrevkode("SP97");
		assertThat(validVedleggsliste.getBrevkode(), is("SP97"));
	}

}