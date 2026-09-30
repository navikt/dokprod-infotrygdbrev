package no.nav.dokprod_infotrygdbrev.bdok100.domain.code;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit test for Spraak
 *
 */
public class SpraakTest {

	@Test
	public void shouldMapLanguages() throws Exception {
		assertThat(Spraak.B.getSpraakKode(), is("NB"));
		assertThat(Spraak.N.getSpraakKode(), is("NN"));
		assertThat(Spraak.E.getSpraakKode(), is("EN"));
		assertThat(Spraak.U.getSpraakKode(), is("NB"));
	}
}