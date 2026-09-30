package no.nav.dokprod_infotrygdbrev.bdok100.support;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.OUTPUT_FILE_LOCATION_KEY;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.START_TIME_KEY;
import static no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper.getKontrollrapport;

import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.CountPair;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import org.springframework.beans.factory.annotation.Autowired;
import java.nio.file.Paths;
import java.util.Date;

/**
 * Generates the kontrollrapport for BDOK100
 *
 */
@Slf4j
public class KontrollrapportTasklet implements Tasklet {

	@Autowired
	private Bdok100Repo repo;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("Antall dokumenter i linjeprintfil: ").append(repo.countByLineprintBrevIsNotNull());
		sb.append("\r\nAntall rader i journalføringsfil: ").append(repo.countByJournalforingsfilIsNotNull());
		sb.append("\r\nAntall brev i dokumentbestilling: ").append(repo.countByStatus(Bdok100Status.BEHANDLET));
		sb.append("\r\nAntall rader feilet: ").append(repo.countByStatus(Bdok100Status.KAN_IKKE_BEHANDLES));
		sb.append("\r\nAntall rader feilet teknisk: ").append(repo.countByStatus(Bdok100Status.FEILET_GSAK));
		sb.append("\r\nAntall brev der NAV-nr = starter med '23': ").append(repo.countByTknr1StartingWith23());
		sb.append("\r\nAntall brev der NAV-nr er i mellom (2399,2820),(2822,2999),(3100,3399),(3500,3799),(3900,4199),(4300,4400),(4500,4600) eller (5999,10000): ").append(repo.countInvalidTkNr1());
		sb.append("\r\nAntall brev der brevnavn starter på ZH2,ZE2,Z10,Z20,Z30,Z40,ZH3 eller ZE3: ").append(repo.countLocalBrevkode());
		sb.append("\r\nAntall brev der Infotrygd_BrevkodePage = FLXXX_YY: ").append(repo.countByInfotrygdBrevkodePageEqualToFLXXXYY());
		sb.append("\r\nAntall brev der brevtype <> OK: ").append(repo.countByBrevtypeNotOk());

		sb.append("\r\nAntall brev gruppert på brevkode (inneholder også ignorerte brev):");
		for (CountPair countPair : repo.countByLinjedataBrevkode()) {
			sb.append("\r\nbrevkode=").append(countPair.getName()).append(": ").append(countPair.getCount());
		}
		Files.write(sb.toString(), Paths.get(getKontrollrapportPath(chunkContext)).toFile(), Bdok100Constants.BDOK100_OUTPUT_CHARSET);

		log.info("\r\nKontrollrapport:" +
				"\r\n---------------------------------------------\r\n" +
				sb.toString() +
				"\r\n---------------------------------------------");
		return RepeatStatus.FINISHED;
	}

	private String getKontrollrapportPath(ChunkContext chunkContext) {
		String outputLocation = (String) chunkContext.getStepContext().getJobExecutionContext().get(OUTPUT_FILE_LOCATION_KEY);
		Date startTime = (Date) chunkContext.getStepContext().getJobExecutionContext().get(START_TIME_KEY);
		return outputLocation + "/" + getKontrollrapport(startTime);
	}

}
