package no.nav.dokprod_infotrygdbrev.consumer;

public class SakFunctionalException extends RuntimeException {
    public SakFunctionalException(String message) {
        super(message);
    }

    public SakFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
