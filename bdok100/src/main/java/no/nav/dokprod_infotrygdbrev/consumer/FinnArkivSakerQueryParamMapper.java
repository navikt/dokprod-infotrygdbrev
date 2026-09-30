package no.nav.dokprod_infotrygdbrev.consumer;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class FinnArkivSakerQueryParamMapper {
    MultiValueMap<String, String> map(FinnArkivsakIdTo to) {
        final LinkedMultiValueMap<String, String> queryParam = new LinkedMultiValueMap<>();
        queryParam.add("fagsakNr", to.getFagsakNr());
        queryParam.add("applikasjon", to.getApplikasjon());
        queryParam.add("tema", to.getTema());
        if(to.getOrgnr() != null) {
            queryParam.add("orgnr", to.getOrgnr());
        } else if(to.getAktoerId() != null) {
            queryParam.add("aktoerId", to.getAktoerId());
        } else {
            throw new SakFunctionalException("Orgnr eller aktoerId ikke angitt i request. " + to);
        }
        return queryParam;
    }
}
