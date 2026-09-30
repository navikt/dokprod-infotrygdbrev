package no.nav.dokprod_infotrygdbrev.xml.validation;

/**
 * Exception to be thrown when XML validation fails
 *
 */
public class XmlValidationException extends RuntimeException {

	public XmlValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public XmlValidationException(String message) {
		super(message);
	}
}
