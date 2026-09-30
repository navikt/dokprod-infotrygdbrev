package no.nav.dokprod_infotrygdbrev.consumer.mock;

import no.nav.dokprod_infotrygdbrev.consumer.FinnArkivsakIdTo;
import no.nav.dokprod_infotrygdbrev.consumer.OpprettArkivsakTo;
import no.nav.dokprod_infotrygdbrev.consumer.SakConsumer;
import no.nav.dokprod_infotrygdbrev.consumer.SakFunctionalException;

public class SakConsumerMock implements SakConsumer {
    public static final String SAK_ID = "96759347";
    public static final String SAK_ID_OPPRETTET = "96759199";
    public static final String ERROR_ID = "98765";

    @Override
    public String finnArkivsakId(FinnArkivsakIdTo to) {
        if (to.getFagsakNr().equals(ERROR_ID)) {
            throw new SakFunctionalException("sakerror");
        }
        return SAK_ID;
    }

    @Override
    public String opprettArkivsak(OpprettArkivsakTo to) {
        return SAK_ID_OPPRETTET;
    }
}
