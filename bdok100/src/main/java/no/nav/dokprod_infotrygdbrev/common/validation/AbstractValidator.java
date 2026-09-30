package no.nav.dokprod_infotrygdbrev.common.validation;

import no.nav.dokprod_infotrygdbrev.common.exception.BeanValidationException;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Abstract class for validator, replaces standard bean validation as beanvalidation will prevent hibernate from persisting
 *
 */
public class AbstractValidator<T> {
	private final Class<T> type;

	public AbstractValidator(Class<T> type) {
		this.type = type;
	}

	protected static final Pattern DIGS = Pattern.compile("\\d+");
	protected static final Pattern DIG_4 = Pattern.compile("\\d{4}");
	protected static final Pattern DIG_11 = Pattern.compile("\\d{11}");
	protected static final Pattern DIG_2 = Pattern.compile("\\d{2}");
	protected static final Pattern WORD = Pattern.compile("\\w");
	protected static final Pattern WORD_2 = Pattern.compile("\\w{2}");
	protected static final Pattern WORD_4 = Pattern.compile("\\w{4}");

	public void notNull(Object field, String fieldName) {
		if (field == null) {
			throw new BeanValidationException(String.format("%s cannot be null", getFieldName(fieldName)));
		}
	}

	public void hasText(String field, String fieldName) {
		if (StringUtils.isBlank(field)) {
			throw new BeanValidationException(String.format("%s cannot be empty or missing", getFieldName(fieldName)));
		}
	}

	public void pattern(String field, Pattern pattern, String fieldName) {
		if (field != null && !pattern.matcher(field).matches()) {
			throw new BeanValidationException(String.format("%s <%s> does not match %s", getFieldName(fieldName), field, pattern));
		}
	}

	public void notNullpattern(String field, Pattern pattern, String fieldName) {
		notNull(field, fieldName);
		pattern(field, pattern, fieldName);
	}

	public void isNumeric(String field, String fieldName) {
		notNullpattern(field, DIGS, fieldName);
	}

	private String getFieldName(String fieldName) {
		return type.getSimpleName() + "." + fieldName;
	}
}
