package no.nav.dokprod_infotrygdbrev.consumer.pdl;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class PdlResponseTo {
    private PdlHentIdenterTo data;
    private List<PdlErrorTo> errors;

    @Data
    public static class PdlHentIdenterTo {
        private PdlIdenterTo hentIdenter;
    }

    @Data
    public static class PdlIdenterTo {
        private List<PdlIdentTo> identer;
    }

    @Data
    public static class PdlIdentTo {
        @ToString.Exclude
        private String ident;
        private boolean historisk;
        private PdlGruppe gruppe;
    }

    @Data
    public static class PdlErrorTo {
        private String message;
        private PdlErrorExtensionTo extensions;
    }

    @Data
    public static class PdlErrorExtensionTo {
        private String code;
        private String classification;
    }

    public enum PdlGruppe {
        FOLKEREGISTERIDENT, AKTORID, NPID;
    }
}
