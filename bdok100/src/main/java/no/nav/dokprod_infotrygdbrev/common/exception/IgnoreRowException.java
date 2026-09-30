package no.nav.dokprod_infotrygdbrev.common.exception;

/**
 * Exception for rows to ignore
 *
 */
public class IgnoreRowException extends FailedRowException {
	public IgnoreRowException(String message) {
		super(message);
	}

	public IgnoreRowException(String message, Throwable cause) {
		super(message, cause);
	}
}
