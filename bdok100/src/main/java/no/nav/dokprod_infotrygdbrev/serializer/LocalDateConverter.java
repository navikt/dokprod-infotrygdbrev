package no.nav.dokprod_infotrygdbrev.serializer;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * Xstream LocalDate Converter
 *
 */
public class LocalDateConverter implements SingleValueConverter {
	@Override
	public String toString(Object obj) {
		return obj.toString();
	}

	@Override
	public Object fromString(String str) {
		return DateTime.parse(str).toLocalDate();
	}

	@Override
	public boolean canConvert(Class type) {
		return type.equals(LocalDate.class);
	}
}
