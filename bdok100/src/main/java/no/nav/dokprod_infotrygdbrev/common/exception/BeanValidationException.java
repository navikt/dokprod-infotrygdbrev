package no.nav.dokprod_infotrygdbrev.common.exception;

/**
 * Bean validation exception
 *
 */
public class BeanValidationException extends FailedRowException {
	public BeanValidationException(String message) {
		super(message);
	}
}
