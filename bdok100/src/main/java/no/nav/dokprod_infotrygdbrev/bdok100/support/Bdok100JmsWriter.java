package no.nav.dokprod_infotrygdbrev.bdok100.support;

import lombok.extern.slf4j.Slf4j;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.ProduserIkkeRedigerbartDokument;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.jms.JmsItemWriter;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsOperations;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class Bdok100JmsWriter extends JmsItemWriter<ProduserIkkeRedigerbartDokument> {

	public Bdok100JmsWriter(JmsOperations jmsOperations) {
		super(jmsOperations);
	}

	@Override
	public void write(Chunk<? extends ProduserIkkeRedigerbartDokument> items) throws Exception {
		try {
			List<String> originalBestillingsIds = extractOldBestillingsIds(items);
			updateBestillingsIdToUuid(items);
			super.write(items);
			// Vi gjør dette som hotfix slik at statusoppdatering i arbeidstabell blir riktig. (se commit)
			writeOriginalBestillingsIdBack(items, originalBestillingsIds);
		} catch (JmsException e) {
			log.error(String.format("Kunne ikke skrive brevbestillinger på kø til QDOK001. Feilmelding=%s", e.getMessage()));
			throw e;
		}
		log.info(String.format("Har lagt til %s brevbestillinger på kø til QDOK001", items.size()));
	}

	private List<String> extractOldBestillingsIds(Chunk<? extends ProduserIkkeRedigerbartDokument> items) {
		return items.getItems()
			.stream()
			.map(item -> item.getDokumentbestillingsinformasjon().getBestillingsId())
			.toList();
	}

	private void updateBestillingsIdToUuid(Chunk<? extends ProduserIkkeRedigerbartDokument> items) {
		items.forEach(o -> {
			ProduserIkkeRedigerbartDokument obj = (ProduserIkkeRedigerbartDokument) o;
			String newBestillingsId = UUID.randomUUID().toString();
			String oldBestillingsId = obj.getDokumentbestillingsinformasjon().getBestillingsId();
			log.info("Sender dokument med bestillingsId={} til qdok001. originalId={}", newBestillingsId, oldBestillingsId);
			obj.getDokumentbestillingsinformasjon().setBestillingsId(newBestillingsId);
		});
	}

	private void writeOriginalBestillingsIdBack(Chunk<? extends ProduserIkkeRedigerbartDokument> items, List<String> originalBestillingsIds) {
		int index = 0;
		for(Object obj : items) {
			((ProduserIkkeRedigerbartDokument) obj).getDokumentbestillingsinformasjon().setBestillingsId(originalBestillingsIds.get(index));
			index++;
		}
	}
}
