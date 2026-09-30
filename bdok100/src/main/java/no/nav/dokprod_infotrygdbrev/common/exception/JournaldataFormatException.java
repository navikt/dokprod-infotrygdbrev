package no.nav.dokprod_infotrygdbrev.common.exception;

/**
 * Journaldata malformed
 *
 */
public class JournaldataFormatException extends FailedRowException {
	public JournaldataFormatException(String message) {
		super(message);
	}
}
