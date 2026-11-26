package no.nav.dokprod.infotrygdbrev.bdok100.support.itest;

import no.nav.dokprod.infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod.infotrygdbrev.bdok100.support.DatabaseOutputService.AvvikType;
import org.junit.Test;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

import static no.nav.dokprod.infotrygdbrev.bdok100.Bdok100Constants.BDOK100_INPUT_CHARSET;
import static no.nav.dokprod.infotrygdbrev.bdok100.Bdok100Constants.CRLF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DatabaseOutputServiceTest extends AbstractBdok100BatchTest {

	@Test
	public void shouldReadChunksInOrderAcrossPagesAndKeepJobsSeparate() throws Exception {
		long firstJob = 987654321L;
		long secondJob = 987654322L;
		try {
			var writer = databaseOutputService.writer(firstJob, AvvikType.LINJEPRINTFIL, Bdok100ArbTbl::getLineprintBrev);
			writer.open(new ExecutionContext());
			List<Bdok100ArbTbl> rows = new ArrayList<>();
			for (int index = 0; index < 205; index++) {
				rows.add(Bdok100ArbTbl.builder().lineprintBrev("Linje " + index + " ø").build());
			}
			writer.write(new Chunk<>(rows.subList(0, 100)));
			writer.write(new Chunk<>(rows.subList(100, 205)));
			writer.close();

			var emptyWriter = databaseOutputService.writer(secondJob, AvvikType.JOURNALFORINGSFIL, Bdok100ArbTbl::getJournalforingsfil);
			emptyWriter.open(new ExecutionContext());
			emptyWriter.close();

			assertEquals(List.of(AvvikType.LINJEPRINTFIL), databaseOutputService.filesForJob(firstJob));
			assertEquals(List.of(AvvikType.JOURNALFORINGSFIL), databaseOutputService.filesForJob(secondJob));
			StringBuilder expected = new StringBuilder();
			for (int index = 0; index < 205; index++) {
				expected.append("Linje ").append(index).append(" ø").append(CRLF);
			}
			try (var stream = databaseOutputService.openStream(firstJob, AvvikType.LINJEPRINTFIL)) {
				assertEquals(expected.toString(), new String(stream.readAllBytes(), BDOK100_INPUT_CHARSET));
			}
			try (var stream = databaseOutputService.openStream(secondJob, AvvikType.JOURNALFORINGSFIL)) {
				assertEquals(0, stream.readAllBytes().length);
			}

			databaseOutputService.deleteForJob(firstJob);
			assertTrue(databaseOutputService.filesForJob(firstJob).isEmpty());
			assertEquals(List.of(AvvikType.JOURNALFORINGSFIL), databaseOutputService.filesForJob(secondJob));
		} finally {
			databaseOutputService.deleteForJob(firstJob);
			databaseOutputService.deleteForJob(secondJob);
		}
	}

	@Test
	public void shouldReplaceOnlyRerunFileAndSkipNullLines() throws Exception {
		long job = 987654323L;
		try {
			var lpf = databaseOutputService.writer(job, AvvikType.LINJEPRINTFIL, Bdok100ArbTbl::getLineprintBrev);
			var jff = databaseOutputService.writer(job, AvvikType.JOURNALFORINGSFIL, Bdok100ArbTbl::getJournalforingsfil);
			lpf.open(new ExecutionContext());
			jff.open(new ExecutionContext());
			lpf.write(new Chunk<>(List.of(Bdok100ArbTbl.builder().lineprintBrev("old").build())));
			jff.write(new Chunk<>(List.of(Bdok100ArbTbl.builder().journalforingsfil("kept").build())));

			lpf.open(new ExecutionContext());
			lpf.write(new Chunk<>(List.of(
				Bdok100ArbTbl.builder().lineprintBrev(null).build(),
				Bdok100ArbTbl.builder().lineprintBrev("new").build())));

			try (var stream = databaseOutputService.openStream(job, AvvikType.LINJEPRINTFIL)) {
				assertEquals("new" + CRLF, new String(stream.readAllBytes(), BDOK100_INPUT_CHARSET));
			}
			try (var stream = databaseOutputService.openStream(job, AvvikType.JOURNALFORINGSFIL)) {
				assertEquals("kept" + CRLF, new String(stream.readAllBytes(), BDOK100_INPUT_CHARSET));
			}
		} finally {
			databaseOutputService.deleteForJob(job);
		}
	}
}
