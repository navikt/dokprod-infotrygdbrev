package no.nav.dokprod_infotrygdbrev.bdok100.support.to;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * To holder for Vedlegg
 *
 */
@Component
public class VedleggsLister extends ToHolder<String, VedleggsListe> {

	private static final Logger log = LoggerFactory.getLogger(VedleggsLister.class);

	public void add(VedleggsListe vedleggsListe) {
		put(vedleggsListe.getBrevkode(), vedleggsListe);
	}

	/**
	 * Get Vedleggsliste from brevkode
	 *
	 * @param brevkode The brevkode for the mapping
	 * @return The Vedleggsliste if found, null otherwise
	 */
	public VedleggsListe getVedleggsListe(String brevkode) {
		return getValue(brevkode);
	}

}
