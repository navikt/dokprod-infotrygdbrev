package no.nav.dokprod.infotrygdbrev.bdok100.support;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.START_TIME_KEY;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.getKontrollrapport;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokprod.infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod.infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod.infotrygdbrev.bdok100.repo.Bdok100Repo;
import no.nav.dokprod.infotrygdbrev.bdok100.repo.CountPair;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import java.util.Date;

@Slf4j
public class KontrollrapportTasklet implements Tasklet {

	private final Bdok100Repo repo;
	private final RemoteReportWriter remoteReportWriter;

	public KontrollrapportTasklet(Bdok100Repo repo, RemoteReportWriter remoteReportWriter) {
		this.repo = repo;
		this.remoteReportWriter = remoteReportWriter;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		Date startTime = (Date) chunkContext.getStepContext().getJobExecutionContext().get(START_TIME_KEY);
		String content = writeKontrollrapport(startTime);

		log.info("\r\nKontrollrapport:" +
				"\r\n---------------------------------------------\r\n" +
				content +
				"\r\n---------------------------------------------");
		return RepeatStatus.FINISHED;
	}

	private String writeKontrollrapport(Date startTime) {
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
		String content = sb.toString();
		remoteReportWriter.writeFile(getKontrollrapport(startTime),
			content.getBytes(Bdok100Constants.BDOK100_OUTPUT_CHARSET));
		return content;
	}
}
