package no.nav.dokprod_infotrygdbrev.common.exception;

/**
 * Parent Exception for all Failed row exceptions
 *
 */
public abstract class FailedRowException extends RuntimeException {

	public FailedRowException(String message) {
		super(message);
	}

	public FailedRowException(String message, Throwable cause) {
		super(message, cause);
	}
}
