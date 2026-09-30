package no.nav.dokprod_infotrygdbrev.serializer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDate;
import org.junit.Test;

/**
 * Unit test for LocalDateConverter
 *
 */
public class LocalDateConverterTest {

	public static final String TIME = "2015-04-12";
	private LocalDateConverter converter = new LocalDateConverter();

	@Test
	public void shouldConvert() throws Exception {
		LocalDate dateTime = (LocalDate) converter.fromString(TIME);
		String string = converter.toString(dateTime);

		assertThat(string, is(TIME));
	}

	@Test
	public void shouldBeAbleToConvertLocalDateTime() throws Exception {
		assertTrue(converter.canConvert(LocalDate.class));
	}
}
