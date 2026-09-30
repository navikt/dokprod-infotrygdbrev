package no.nav.dokprod_infotrygdbrev.consumer;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
public class OpprettArkivsakTo {
    @ToString.Exclude
    private final String aktoerId;
    private final String orgnr;
    private final String applikasjon;
    private final String tema;
    private final String fagsakNr;
}
