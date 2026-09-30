package no.nav.dokprod_infotrygdbrev.consumer;


import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.BestillendeFagsystemCode;

class FinnArkivsakIdToMapper {

    FinnArkivsakIdTo mapPerson(final FinnEllerOpprettSakTo to, final String aktoerId) {
        return mapBase(to).aktoerId(aktoerId).build();
    }

    FinnArkivsakIdTo mapOrganisasjon(final FinnEllerOpprettSakTo to) {
        return mapBase(to).orgnr(to.getBrukerId()).build();
    }

    private FinnArkivsakIdTo.FinnArkivsakIdToBuilder mapBase(FinnEllerOpprettSakTo to) {
        return FinnArkivsakIdTo.builder()
                .tema(to.getFagomraade() == null ? null : to.getFagomraade().name())
                .fagsakNr(to.getFagsystemSakId())
                .applikasjon(to.getFagsystem() == null ? BestillendeFagsystemCode.IT01.name() : to.getFagsystem().name());
    }
}
