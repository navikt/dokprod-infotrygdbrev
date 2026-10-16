package no.nav.dokprod.infotrygdbrev.consumer;

public class SakFunctionalException extends RuntimeException {
    public SakFunctionalException(String message) {
        super(message);
    }

    public SakFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
