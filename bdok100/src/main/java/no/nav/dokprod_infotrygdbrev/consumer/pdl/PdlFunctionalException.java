package no.nav.dokprod_infotrygdbrev.consumer.pdl;

public class PdlFunctionalException extends RuntimeException {
    public PdlFunctionalException(String message) {
        super(message);
    }

    public PdlFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
