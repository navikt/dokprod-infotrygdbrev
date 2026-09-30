package no.nav.dokprod_infotrygdbrev.bdok100.support.processors;

import no.nav.brevogarkiv.batch.common.BatchCounter;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Events;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.NAVKontor;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.validation.NAVKontorValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.NAVKontorMapper;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod_infotrygdbrev.common.exception.NAVKontorFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Processor for loading Nav Kontor in BDOK100
 *
 */
public class NAVKontorMapperProcessor implements ItemProcessor<String, NAVKontor>, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(NAVKontorMapperProcessor.class);

	@Autowired
	private NAVKontors kontors;

	@Autowired
	private BatchCounter bdok100BatchCounter;

	@Autowired
	private NAVKontorValidator validator;

	@Override
	public NAVKontor process(String navKontor) throws Exception {
		NAVKontor kontor = NAVKontorMapper.map(navKontor);

		try {
			if (kontor == null) {
				throw new NAVKontorFormatException("Cannot map string to NAVKontor: \'" + navKontor + "\'");
			}
			validator.validate(kontor);
			kontors.add(kontor);
			bdok100BatchCounter.incrementEvent(Bdok100Events.NAVKONTOR_LOADED);
		} catch (NAVKontorFormatException e) {
			// Invalid kontor entries are just filtered. No exceptions will be raised.
			LOGGER.warn("Invalid NAVKontor line. Will be ignored: \'" + navKontor + "\'. Error message: \'" + e.getMessage() + "\'.");
			bdok100BatchCounter.incrementEvent(Bdok100Events.NAVKONTOR_LOAD_FAILED);
			return null;
		}
		return kontor;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}

}
