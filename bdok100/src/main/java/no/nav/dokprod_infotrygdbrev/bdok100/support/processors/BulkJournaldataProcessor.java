package no.nav.dokprod_infotrygdbrev.bdok100.support.processors;

import no.nav.brevogarkiv.batch.common.BatchCounter;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Errors;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Events;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.UpdateArbTbl;
import no.nav.dokprod_infotrygdbrev.support.StrUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.infrastructure.item.ItemProcessor;

/**
 * Processor for setting fields for updating of raw journaldata
 *
 */
public class BulkJournaldataProcessor implements ItemProcessor<String, UpdateArbTbl> {

	private static final int ID_LEN = 16;
	private BatchCounter batchCounter;

	@Override
	public UpdateArbTbl process(String journaldata) throws Exception {
		String onDemandId = journaldata.split(",")[0];
		UpdateArbTbl updateArbTbl;

		if (StringUtils.isEmpty(onDemandId) || onDemandId.length() != ID_LEN) {
			String uuid = StrUtils.createRandomId(ID_LEN);
			updateArbTbl = UpdateArbTbl.builder()
					.onDemandId(uuid)
					.journalforingsfil(journaldata)
					.updateStatus(Bdok100Status.UNDER_INNLESNING_LPF.name())
					.status(Bdok100Status.KAN_IKKE_BEHANDLES.name())
					.feilStatus(Bdok100Errors.MISSING_ONDEMANDID_JFF)
					.build();
		} else {
			updateArbTbl = UpdateArbTbl.builder()
					.onDemandId(onDemandId)
					.journalforingsfil(journaldata)
					.updateStatus(Bdok100Status.UNDER_INNLESNING_LPF.name())
					.status(Bdok100Status.UNDER_INNLESNING_JFF.name())
					.feilStatus(null)
					.build();
		}

		batchCounter.incrementEvent(Bdok100Events.JOURNALDATA_COUNTER);
		return updateArbTbl;
	}

	public void setBatchCounter(BatchCounter batchCounter) {
		this.batchCounter = batchCounter;
	}
}

