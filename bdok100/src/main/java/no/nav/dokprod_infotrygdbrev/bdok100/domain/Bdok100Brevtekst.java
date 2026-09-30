package no.nav.dokprod_infotrygdbrev.bdok100.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class Bdok100Brevtekst implements Serializable {
    private int rekkefolge;
    private String innhold;

    @JsonCreator
    public Bdok100Brevtekst(@JsonProperty("rekkefolge") int rekkefolge, @JsonProperty("innhold") String innhold) {
        this.rekkefolge = rekkefolge;
        this.innhold = innhold;
    }
}
