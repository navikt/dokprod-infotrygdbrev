package no.nav.dokprod_infotrygdbrev.serializer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDateTime;
import org.junit.Test;

/**
 * Unit test for LocalDateTimeConverter
 *
 */
public class LocalDateTimeConverterTest {

	public static final String TIME = "2015-04-12T12:00:00.000+02:00";
	private LocalDateTimeConverter converter = new LocalDateTimeConverter();

	@Test
	public void shouldConvert() throws Exception {
		LocalDateTime dateTime = (LocalDateTime) converter.fromString(TIME);
		String string = converter.toString(dateTime);

		assertThat(string, is(TIME));
	}

	@Test
	public void shouldBeAbleToConvertLocalDateTime() throws Exception {
		assertTrue(converter.canConvert(LocalDateTime.class));
	}
}
