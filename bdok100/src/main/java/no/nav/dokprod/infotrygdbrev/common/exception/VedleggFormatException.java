package no.nav.dokprod.infotrygdbrev.common.exception;

/**
 * Malformed vedlegg
 *
 */
public class VedleggFormatException extends BeanValidationException {

	public VedleggFormatException(String message) {
		super(message);
	}
}