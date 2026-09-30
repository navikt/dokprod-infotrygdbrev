package no.nav.dokprod_infotrygdbrev.common.exception;

/**
 * Malformed NAV Kontor
 *
 */
public class NAVKontorFormatException extends BeanValidationException {
	public NAVKontorFormatException(String message) {
		super(message);
	}
}
