package no.nav.dokprod.infotrygdbrev.consumer.mock;


import no.nav.dokprod.infotrygdbrev.consumer.pdl.PdlConsumer;

public class PdlConsumerMock implements PdlConsumer {
    @Override
    public String hentAktoerIdForIdent(String ident) {
        return "10000000000000";
    }
}
