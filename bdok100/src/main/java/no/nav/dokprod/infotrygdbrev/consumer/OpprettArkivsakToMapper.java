package no.nav.dokprod.infotrygdbrev.consumer;

import no.nav.dokprod.infotrygdbrev.bdok100.kodeverk.BestillendeFagsystemCode;

class OpprettArkivsakToMapper {

    OpprettArkivsakTo mapPerson(final FinnEllerOpprettSakTo to, final String aktoerId) {
        return mapBase(to).aktoerId(aktoerId).build();
    }

    OpprettArkivsakTo mapOrganisasjon(final FinnEllerOpprettSakTo to) {
        return mapBase(to).orgnr(to.getBrukerId()).build();
    }

    private OpprettArkivsakTo.OpprettArkivsakToBuilder mapBase(FinnEllerOpprettSakTo to) {
        return OpprettArkivsakTo.builder()
                .tema(to.getFagomraade() == null ? null : to.getFagomraade().name())
                .fagsakNr(to.getFagsystemSakId())
                .applikasjon(to.getFagsystem() == null ? BestillendeFagsystemCode.IT01.name() : to.getFagsystem().name());
    }
}
