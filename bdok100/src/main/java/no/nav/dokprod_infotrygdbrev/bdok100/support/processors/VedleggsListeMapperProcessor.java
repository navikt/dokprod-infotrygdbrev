package no.nav.dokprod_infotrygdbrev.bdok100.support.processors;

import no.nav.brevogarkiv.batch.common.BatchCounter;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Events;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.validation.VedleggsListeValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.VedleggsListeMapper;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.VedleggsLister;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Processor for loading vedlegg BDOK100
 *
 */
public class VedleggsListeMapperProcessor implements ItemProcessor<String, VedleggsListe>, InitializingBean {

	@Autowired
	private VedleggsLister vedleggsLister;

	@Autowired
	private BatchCounter bdok100BatchCounter;

	@Autowired
	private VedleggsListeValidator validator;

	@Override
	public VedleggsListe process(String vedleggsListe) throws Exception {
		VedleggsListe liste = VedleggsListeMapper.map(vedleggsListe);
		validator.validate(liste);
		vedleggsLister.add(liste);
		bdok100BatchCounter.incrementEvent(Bdok100Events.VEDLEGGSLISTE_LOADED);
		return liste;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}

}
