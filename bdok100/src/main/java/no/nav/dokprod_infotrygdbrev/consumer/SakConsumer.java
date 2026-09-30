package no.nav.dokprod_infotrygdbrev.consumer;

public interface SakConsumer {
    /**
     * Finn arkivsakId basert på sakskriterier.
     *
     * @param to Sakskriterier
     * @return ArkivsakId
     */
    String finnArkivsakId(FinnArkivsakIdTo to);

    /**
     * Opprett sak basert på innholdet av {@link OpprettArkivsakTo}.
     *
     * @param to Arkivsak info
     * @return ArkivsakId for saken som ble opprettet.
     */
    String opprettArkivsak(OpprettArkivsakTo to);
}
