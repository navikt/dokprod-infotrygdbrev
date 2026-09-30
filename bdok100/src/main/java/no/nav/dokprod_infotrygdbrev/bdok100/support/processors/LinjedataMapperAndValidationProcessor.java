package no.nav.dokprod_infotrygdbrev.bdok100.support.processors;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Events.LINJEDATA_AVVIK_COUNTER;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Events.LINJEDATA_KONTROLLRAPPORT_COUNTER;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevogarkiv.batch.common.BatchCounter;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.validation.LinjedataValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.LinjedataMapper;
import no.nav.dokprod_infotrygdbrev.common.exception.AvviksfilException;
import no.nav.dokprod_infotrygdbrev.common.exception.KontrollRapportException;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Processor for linjedata mapping, mappes and validates
 *
 */
@Slf4j
public class LinjedataMapperAndValidationProcessor implements ItemProcessor<Bdok100ArbTbl, Bdok100ArbTbl> {
	private LinjedataMapper linjedataMapper;
	private LinjedataValidator linjedataValidator;
	private BatchCounter batchCounter;

	@Override
	public Bdok100ArbTbl process(Bdok100ArbTbl item) throws Exception {
		if (item.getLineprintBrev() == null) {
			failAvvik(item, "mangler linjedata");
			return item;
		}
		try {
			linjedataMapper.map(item);
			item.setStatus(Bdok100Status.MAPPING_LPF);
			validate(item);
		} catch (Exception e) {
			failAvvik(item, "Linjedata mapping error: " + e.getMessage());
		}

		return item;
	}

	private void validate(Bdok100ArbTbl item) {
		try {
			linjedataValidator.validate(item);
		} catch (AvviksfilException e) {
			failAvvik(item, "Linjedata validation error: " + e.getMessage());
		} catch (KontrollRapportException e) {
			batchCounter.incrementEvent(LINJEDATA_KONTROLLRAPPORT_COUNTER);
			item.failRapport("Linjedata validation error: " + e.getMessage());
		}
	}

	private void failAvvik(Bdok100ArbTbl item, String message) {
		batchCounter.incrementEvent(LINJEDATA_AVVIK_COUNTER);
		item.failAvvik(message);
	}

	@Autowired
	public void setLinjedataMapper(LinjedataMapper linjedataMapper) {
		this.linjedataMapper = linjedataMapper;
	}

	@Autowired
	public void setLinjedataValidator(LinjedataValidator linjedataValidator) {
		this.linjedataValidator = linjedataValidator;
	}

	@Autowired
	public void setBatchCounter(BatchCounter bdok100BatchCounter) {
		this.batchCounter = bdok100BatchCounter;
	}
}
