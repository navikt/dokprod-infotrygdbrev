package no.nav.dokprod_infotrygdbrev.serializer;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for LocalDateTimeConverter
 *
 */
public class LocalDateTimeConverterTest {

	public static final String TIME = "2015-04-12T12:00:00.000+02:00";
	public static final String TIME_NO_ZONE = TIME.substring(0, 23);

	private LocalDateTimeConverter converter = new LocalDateTimeConverter();

	@Test
	public void shouldConvert() throws Exception {
		LocalDateTime dateTime = (LocalDateTime) converter.fromString(TIME);
		String string = converter.toString(dateTime);

		assertThat(string).startsWith(TIME_NO_ZONE);
	}

	@Test
	public void shouldBeAbleToConvertLocalDateTime() throws Exception {
		assertTrue(converter.canConvert(LocalDateTime.class));
	}
}
