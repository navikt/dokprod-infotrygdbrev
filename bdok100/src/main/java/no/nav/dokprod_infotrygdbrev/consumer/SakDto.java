package no.nav.dokprod_infotrygdbrev.consumer;

import lombok.Data;
import lombok.ToString;

@Data
public class SakDto {
    private String id;
    private String tema;
    private String applikasjon;
    @ToString.Exclude
    private String aktoerId;
    private String orgnr;
    private String fagsakNr;
}
