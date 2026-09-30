package no.nav.dokprod_infotrygdbrev.bdok100.support.processors;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Events.JOURNALDATA_AVVIK_COUNTER;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevogarkiv.batch.common.BatchCounter;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.validation.JournaldataValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.JournaldataMapper;
import no.nav.dokprod_infotrygdbrev.common.exception.JournaldataException;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Processor for journaldata mapping og validering
 *
 */
@Slf4j
public class JournaldataMapAndValidateProcessor implements ItemProcessor<Bdok100ArbTbl, Bdok100ArbTbl> {
	private JournaldataMapper journaldataMapper;
	private JournaldataValidator journaldataValidator;
	private BatchCounter batchCounter;

	@Override
	public Bdok100ArbTbl process(Bdok100ArbTbl item) throws Exception {
		if (item.getJournalforingsfil() == null) {
			failAvvik(item, "mangler journaldata");
			return item;
		}
		try {
			journaldataMapper.map(item);
			if (item.getStatus() == Bdok100Status.MAPPING_LPF) {
				// Dont set status or bother validating if it has already failed
				item.setStatus(Bdok100Status.MAPPING_JFF);
				try {
					validate(item);
				} catch (JournaldataException e) {
					item.failAvvik("Journaldata validation error: " + e.getMessage());
				}
			}
		} catch (Exception e) {
			failAvvik(item, "Journaldata mapping error: " + e.getMessage());
		}

		return item;
	}

	private void validate(Bdok100ArbTbl item) {
		journaldataValidator.validate(item);
	}

	private void failAvvik(Bdok100ArbTbl item, String message) {
		batchCounter.incrementEvent(JOURNALDATA_AVVIK_COUNTER);
		item.failAvvik(message);
	}

	@Autowired
	public void setJournaldataMapper(JournaldataMapper journaldataMapper) {
		this.journaldataMapper = journaldataMapper;
	}

	@Autowired
	public void setJournaldataValidator(JournaldataValidator journaldataValidator) {
		this.journaldataValidator = journaldataValidator;
	}

	@Autowired
	public void setBatchCounter(BatchCounter bdok100BatchCounter) {
		this.batchCounter = bdok100BatchCounter;
	}
}
