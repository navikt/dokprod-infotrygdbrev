package no.nav.dokprod.infotrygdbrev.common.exception;

/**
 * Bean validation exception
 *
 */
public class BeanValidationException extends FailedRowException {
	public BeanValidationException(String message) {
		super(message);
	}
}
