package no.nav.dokprod.infotrygdbrev.common.exception;

/**
 * Journaldata validation failure
 *
 */
public class JournaldataException extends FailedRowException {
	public JournaldataException(String message) {
		super(message);
	}
}
