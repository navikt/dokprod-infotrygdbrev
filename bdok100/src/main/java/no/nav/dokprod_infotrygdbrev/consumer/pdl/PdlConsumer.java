package no.nav.dokprod_infotrygdbrev.consumer.pdl;

/**
 * https://navikt.github.io/pdl
 *
 */
public interface PdlConsumer {
    /**
     * Henter aktørId for ident
     *
     * https://navikt.github.io/pdl/#_hentidenter
     *
     * @param ident AktørId eller folkeregisterident
     * @return Aktørid for ident
     */
    String hentAktoerIdForIdent(final String ident);
}
