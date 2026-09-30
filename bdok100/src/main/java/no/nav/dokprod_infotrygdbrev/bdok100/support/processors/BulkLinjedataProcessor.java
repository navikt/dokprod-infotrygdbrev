package no.nav.dokprod_infotrygdbrev.bdok100.support.processors;

import no.nav.brevogarkiv.batch.common.BatchCounter;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Errors;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Events;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.support.StrUtils;
import org.springframework.batch.infrastructure.item.ItemProcessor;

/**
 * Processor for setting fields for updating of raw linjedata
 *
 */
public class BulkLinjedataProcessor implements ItemProcessor<String, Bdok100ArbTbl> {

	private static final int ID_LEN = 16;
	private BatchCounter batchCounter;

	@Override
	public Bdok100ArbTbl process(String linjedata) throws Exception {
		Bdok100ArbTbl bdok100ArbTbl;

		if (linjedata.length() < ID_LEN + 1 || linjedata.substring(1, ID_LEN + 1).trim().length() < ID_LEN) {
			String uuid = StrUtils.createRandomId(ID_LEN);
			bdok100ArbTbl = Bdok100ArbTbl.builder()
					.idnr(uuid)
					.lineprintBrev(linjedata)
					.status(Bdok100Status.KAN_IKKE_BEHANDLES)
					.feilstatus(Bdok100Errors.MISSING_ARKIVID_LPF)
					.build();
		} else {
			bdok100ArbTbl = Bdok100ArbTbl.builder()
					.idnr(linjedata.substring(1, ID_LEN + 1))
					.lineprintBrev(linjedata)
					.status(Bdok100Status.UNDER_INNLESNING_LPF)
					.feilstatus(null)
					.build();
		}

		batchCounter.incrementEvent(Bdok100Events.LINJEDATA_COUNTER);
		return bdok100ArbTbl;
	}

	public void setBatchCounter(BatchCounter batchCounter) {
		this.batchCounter = batchCounter;
	}
}
