package no.nav.dokprod_infotrygdbrev.consumer.mock;


import no.nav.dokprod_infotrygdbrev.consumer.sts.StsConsumer;
import no.nav.dokprod_infotrygdbrev.consumer.sts.StsResponse;

public class StsConsumerMock implements StsConsumer {
    @Override
    public StsResponse getStsToken() {
        return StsResponse.builder()
                .access_token("dummytoken")
                .build();
    }
}
