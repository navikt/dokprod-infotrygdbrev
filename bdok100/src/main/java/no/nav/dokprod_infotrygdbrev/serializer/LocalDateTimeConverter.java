package no.nav.dokprod_infotrygdbrev.serializer;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

/**
 * Xstream LocalDateTime Converter
 *
 */
public class LocalDateTimeConverter implements SingleValueConverter {
	@Override
	public String toString(Object obj) {
		return ((LocalDateTime) obj).toDateTime().toString();
	}

	@Override
	public Object fromString(String str) {
		return DateTime.parse(str).toLocalDateTime();
	}

	@Override
	public boolean canConvert(Class type) {
		return type.equals(LocalDateTime.class);
	}
}
