package no.nav.dokprod.infotrygdbrev.common.exception;

/**
 * Exception for rows that should be handled in avvik
 *
 */
public class AvviksfilException extends FailedRowException {
	public AvviksfilException(String message) {
		super(message);
	}
}
