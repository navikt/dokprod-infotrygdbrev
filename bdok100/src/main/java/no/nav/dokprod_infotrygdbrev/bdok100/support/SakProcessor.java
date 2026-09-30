package no.nav.dokprod_infotrygdbrev.bdok100.support;

import com.google.common.collect.Maps;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import no.nav.brevogarkiv.batch.common.BatchCounter;
import no.nav.brevogarkiv.batch.common.MaxFailuresSupport;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Journaldata;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.Bdok100GsakMapper;
import no.nav.dokprod_infotrygdbrev.consumer.FinnEllerOpprettSakTo;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Events.SAK_COUNTER;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status.FEILET_GSAK;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status.TIL_BEHANDLING;

/**
 * Calls Gsak and sets statuses
 *
 */
@Setter
@Slf4j
public class SakProcessor extends MaxFailuresSupport implements ItemWriter<Bdok100ArbTbl> {

	private AsyncSakConsumer sakConsumer;
	private Bdok100GsakMapper bdok100GsakMapper;
	private BatchCounter bdok100BatchCounter;

	@Override
	public void write(Chunk<? extends Bdok100ArbTbl> items) throws Exception {
		Map<Bdok100ArbTbl, Future<String>> futures = Maps.newHashMap();
		bdok100BatchCounter.start(SAK_COUNTER);

		for (Bdok100ArbTbl item : items) {
			Journaldata journaldata = item.getJournaldata();
			FinnEllerOpprettSakTo opprettSakTo;
			try {
				opprettSakTo = bdok100GsakMapper.map(journaldata);
			} catch (Exception e) {
				item.failAvvik("Sak feilet mapping: " + e.getMessage());
				continue;
			}
			futures.put(item, sakConsumer.finnEllerOpprettSakWithCache(opprettSakTo));
		}

		for (Map.Entry<Bdok100ArbTbl, Future<String>> entry : futures.entrySet()) {
			process(entry.getKey(), entry.getValue());
		}
		bdok100BatchCounter.stop(SAK_COUNTER, futures.size());
	}

	public Bdok100ArbTbl process(Bdok100ArbTbl item, Future<String> future) throws Exception {
		try {
			String sakId = future.get();
			item.setSaksID(sakId);
			item.setStatus(TIL_BEHANDLING);
		} catch (Exception e) {
			Throwable cause = e instanceof ExecutionException ? e.getCause() : e;
			incrementNumberOfFailures();
			fail(item, cause);
		}
		return item;
	}

	private void fail(Bdok100ArbTbl item, Throwable cause) {
		log.warn("Sak feilet for ArkivID=" + item.getIdnr(), cause);
		item.setStatus(FEILET_GSAK);
		item.setFeilstatus("Sak feilet " + cause.getMessage());
	}
}
