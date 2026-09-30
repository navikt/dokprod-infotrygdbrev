package no.nav.dokprod_infotrygdbrev.consumer;

import no.nav.dokprod_infotrygdbrev.consumer.pdl.PdlConsumer;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

@Component
public class SakService {
    private final SakConsumer sakConsumer;
    private final PdlConsumer pdlConsumer;
    private final FinnArkivsakIdToMapper finnArkivsakIdToMapper;
    private final OpprettArkivsakToMapper opprettArkivsakToMapper;

    @Autowired
    public SakService(SakConsumer sakConsumer, PdlConsumer pdlConsumer) {
        this.sakConsumer = sakConsumer;
        this.pdlConsumer = pdlConsumer;
        this.finnArkivsakIdToMapper = new FinnArkivsakIdToMapper();
        this.opprettArkivsakToMapper = new OpprettArkivsakToMapper();
    }

    /**
     * Finn arkivsakId. Hvis den ikke finnes, opprettet en ny arkivsak og returner arkivsakId til den nye arkivsaken.
     *
     * @param to Arkivsak info
     * @return ArkivsakId for saken som ble funnet eller opprettet.
     */
    public String finnEllerOpprettArkivsak(FinnEllerOpprettSakTo to) {
        if(FinnEllerOpprettSakTo.BrukerType.PERSON.equals(to.getBrukerType())) {
            return finnEllerOpprettSakPerson(to);
        } else if(FinnEllerOpprettSakTo.BrukerType.ORGANISASJON.equals(to.getBrukerType())) {
            return finnEllerOpprettSakOrganisasjon(to);
        } else {
            throw new SakFunctionalException("Brukertype må være PERSON eller ORGANISASJON. " + to);
        }
    }

    private String finnEllerOpprettSakPerson(FinnEllerOpprettSakTo to) {
        final String aktoerId = pdlConsumer.hentAktoerIdForIdent(to.getBrukerId());
        final String sakId = sakConsumer.finnArkivsakId(finnArkivsakIdToMapper.mapPerson(to, aktoerId));
        if (sakId == null) {
            return sakConsumer.opprettArkivsak(opprettArkivsakToMapper.mapPerson(to, aktoerId));
        }
        return sakId;
    }

    private String finnEllerOpprettSakOrganisasjon(FinnEllerOpprettSakTo to) {
        final String sakId = sakConsumer.finnArkivsakId(finnArkivsakIdToMapper.mapOrganisasjon(to));
        if (sakId == null) {
            return sakConsumer.opprettArkivsak(opprettArkivsakToMapper.mapOrganisasjon(to));
        }
        return sakId;
    }
}
