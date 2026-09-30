package no.nav.dokprod_infotrygdbrev.support;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit tets for {@link StrUtils}
 *
 */
public class StrUtilsTest {
	@Test
	public void shouldCraeteCorrectLengthStrings() throws Exception {
		for (int i = 1; i < 30; i++) {
			String randomId = StrUtils.createRandomId(i);
			assertThat(randomId.length(), is(i));
		}
	}

}