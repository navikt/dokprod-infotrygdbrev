package no.nav.dokprod.infotrygdbrev.bdok100.support.itest;

import jakarta.xml.bind.JAXBElement;
import no.nav.dokprod.infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod.infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod.infotrygdbrev.bdok100.support.mappers.ProduserIkkeRedigerbartDokumentXmlMapperTest;
import no.nav.dokprod.infotrygdbrev.common.BDOKCommonBatchInputParameters;
import no.nav.dokprod.infotrygdbrev.common.support.JobCompletionFileHandler;
import no.nav.dokprod.infotrygdbrev.util.NavExitStatus;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.NorskPostadresse;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.Person;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.ProduserIkkeRedigerbartDokument;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;

import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.START_TIME_KEY;
import static no.nav.dokprod.infotrygdbrev.bdok100.Bdok100Constants.BDOK100_OUTPUT_CHARSET;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.JOURNALDATA_CSV;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.LINJEDATA_TXT;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.formatDateToSeconds;
import static no.nav.dokprod.infotrygdbrev.bdok100.domain.Bdok100Status.BEHANDLET;
import static no.nav.dokprod.infotrygdbrev.bdok100.domain.Bdok100Status.FEILET_GSAK;
import static no.nav.dokprod.infotrygdbrev.bdok100.domain.Bdok100Status.KAN_IKKE_BEHANDLES;
import static no.nav.dokprod.infotrygdbrev.bdok100.support.mappers.ProduserIkkeRedigerbartDokumentXmlMapper.BDOK100_PREFIX;
import static no.nav.dokprod.infotrygdbrev.consumer.mock.SakConsumerMock.SAK_ID;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

public class Bdok100JobTest extends AbstractBdok100BatchTest {
	private static final String UUID_REGEX = "([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}){1}";

	@Autowired
	private JobCompletionFileHandler jobCompletionFileHandler;

	@Before
	public void setUp() throws Exception {
		reset(jobCompletionFileHandler);
		setupBdok100InputFiles();
	}

	@Test
	public void shouldStartContext() throws Exception {

	}

	@Test
	public void shouldRunWithSingleBrev() throws Exception {
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_single_ok.csv", JOURNALDATA_CSV);
		copyFileFromInputSourceFolderToInputFileFolder("linjedata_single_ok.crlf.txt", LINJEDATA_TXT);
		JobParameters parameters = getDefaultBdok100JobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getStatus(),is(BatchStatus.COMPLETED));
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		Object data = jmsBdok100Template.receiveAndConvert();
		assertTrue(((JAXBElement) data).getValue() instanceof ProduserIkkeRedigerbartDokument);
		ProduserIkkeRedigerbartDokument produserIkkeRedigerbartDokument = ((ProduserIkkeRedigerbartDokument) ((JAXBElement) data)
				.getValue());
		assertThat(produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon(), notNullValue());
		assertThat(produserIkkeRedigerbartDokument.getBrevdata(), notNullValue());

		assertThat(produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getBatchId(), is(BDOK100_PREFIX+new SimpleDateFormat("yyyyMMdd").format(LocalDate.now().toDate())));
		assertThat(produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getBestillingsId(), matchesPattern(UUID_REGEX));
		assertThat(produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getUstrukturertTittel(), is("Tittel innhold"));
		assertThat(produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getDokumenttypeId(), is("000045"));
		assertThat(produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getBestillendeFagsystemkode(), is("IT01"));

		assertThat(((Person)produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getBruker()).getNavn(), is("Unknown"));
		assertThat(((Person)produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getBruker()).getPersonidentifikator(), is("11111111111"));

		assertThat(((Person)produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getMottaker()).getNavn(), is("TEST TESTESEN"));
		assertThat(((Person)produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getMottaker()).getPersonidentifikator(), is("11111111111"));

		assertThat(produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getArkivsak().getSakstilhoerendeFagsystemkode(), is("FS22"));
		assertThat(produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getArkivsak().getJournalsakId(), is(SAK_ID));

		assertThat(produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getDokumenttilhoerendeFagomraadekode(), is("FOS"));
		assertThat(produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getJournalfoerendeEnhet(), is("0106"));

		assertThat(produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getSaksbehandlernavn(), is("Saksbehandler saksbehandlersen"));
		assertThat(((NorskPostadresse)produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje1(), is("C/O TESTINE TESTESEN"));
		assertThat(((NorskPostadresse)produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje2(), is("Testeveien 18"));
		assertThat(((NorskPostadresse)produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje3(), is("Postboks 1"));
		assertThat(((NorskPostadresse)produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getAdresse()).getPostnummer(), is("1111"));
		assertThat(((NorskPostadresse)produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getAdresse()).getLand(), is("NO"));
		assertThat(((NorskPostadresse)produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon().getAdresse()).getPoststed(), is("Test"));

		List<String> lines = kontrollrapportLines(jobExecution);
		assertThat(lines.get(0), is("Antall dokumenter i linjeprintfil: 1"));
		assertThat(lines.get(1), is("Antall rader i journalføringsfil: 1"));
		assertThat(lines.get(2), is("Antall brev i dokumentbestilling: 1"));
	}

	@Test
	public void shouldHandleBrevWithBrevtypeAN() throws Exception {
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_for_brev_med_brevtype_AN.txt", JOURNALDATA_CSV);
		copyFileFromInputSourceFolderToInputFileFolder("linjedata_med_brevtype_AN.txt", LINJEDATA_TXT);
		JobParameters parameters = getDefaultBdok100JobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		validateMessageInQueue();

		List<String> lines = kontrollrapportLines(jobExecution);
		assertThat(lines.get(0), is("Antall dokumenter i linjeprintfil: 2"));
		assertThat(lines.get(1), is("Antall rader i journalføringsfil: 2"));
		assertThat(lines.get(2), is("Antall brev i dokumentbestilling: 1"));
	}

	@Test
	public void shouldEndWithWarningWhenAddressError() throws Exception {
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_ugyldig_adresse.csv", JOURNALDATA_CSV);
		copyFileFromInputSourceFolderToInputFileFolder("linjedata_ugyldig_adresse.txt", LINJEDATA_TXT);
		JobParameters parameters = getDefaultBdok100JobParameters();
		JobExecution jobExecution = launchJob(parameters);

		validateMessageInQueue();

		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.WARNING));
		List<String> lines = kontrollrapportLines(jobExecution);
		assertThat(lines.get(0), is("Antall dokumenter i linjeprintfil: 2"));
		assertThat(lines.get(1), is("Antall rader i journalføringsfil: 2"));
		assertThat(lines.get(2), is("Antall brev i dokumentbestilling: 1"));
		assertThat(lines.get(3), is("Antall rader feilet: 1"));
	}

	@Test
	public void shouldEndWithWarningWhenOneSpaceBetweenAdressAndLetterText() throws Exception {
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_for_lite_mellomrom.csv", JOURNALDATA_CSV);
		copyFileFromInputSourceFolderToInputFileFolder("linjedata_for_lite_mellomrom.crlf.txt", LINJEDATA_TXT);
		JobParameters parameters = getDefaultBdok100JobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.WARNING));
		List<String> lines = kontrollrapportLines(jobExecution);
		assertThat(lines.get(0), is("Antall dokumenter i linjeprintfil: 4"));
		assertThat(lines.get(1), is("Antall rader i journalføringsfil: 4"));
		assertThat(lines.get(2), is("Antall brev i dokumentbestilling: 3"));
		assertThat(lines.get(3), is("Antall rader feilet: 1"));
	}

	// Denne ignorer Helfo brev, selv om journaldata mangler
	@Test
	public void shouldIgnoreHelfoWithoutJournaldata() throws Exception {
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_single_ok.csv", JOURNALDATA_CSV);
		copyFileFromInputSourceFolderToInputFileFolder("linjedata_uten_gyldig_brevdata.txt", LINJEDATA_TXT);
		JobParameters parameters = getDefaultBdok100JobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		List<String> lines = kontrollrapportLines(jobExecution);
		assertThat(lines.get(0), is("Antall dokumenter i linjeprintfil: 2"));
		assertThat(lines.get(1), is("Antall rader i journalføringsfil: 1"));
		assertThat(lines.get(2), is("Antall brev i dokumentbestilling: 1"));
		assertThat(lines.get(3), is("Antall rader feilet: 0"));
		assertThat(lines.get(5), is("Antall brev der NAV-nr = starter med '23': 1"));
	}

	// Denne ignorer Helfo brevet når journaldata finnes
	@Test
	public void shouldIgnoreHelfoWithJournaldata() throws Exception {
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_both_ok.csv", JOURNALDATA_CSV);
		copyFileFromInputSourceFolderToInputFileFolder("linjedata_uten_gyldig_brevdata.txt", LINJEDATA_TXT);
		JobParameters parameters = getDefaultBdok100JobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		List<String> lines = kontrollrapportLines(jobExecution);
		assertThat(lines.get(0), is("Antall dokumenter i linjeprintfil: 2"));
		assertThat(lines.get(1), is("Antall rader i journalføringsfil: 2"));
		assertThat(lines.get(2), is("Antall brev i dokumentbestilling: 1"));
		assertThat(lines.get(3), is("Antall rader feilet: 0"));
		assertThat(lines.get(5), is("Antall brev der NAV-nr = starter med '23': 1"));
	}

	@Test
	public void shouldEndCompleteIfEverythingOk() throws Exception {
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_single_ok.csv", JOURNALDATA_CSV);
		copyFileFromInputSourceFolderToInputFileFolder("linjedata_single_ok.crlf.txt", LINJEDATA_TXT);
		JobParameters parameters = getDefaultBdok100JobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		validateMessageInQueue();

		List<String> lines = kontrollrapportLines(jobExecution);
		assertThat(lines.get(0), is("Antall dokumenter i linjeprintfil: 1"));
		assertThat(lines.get(1), is("Antall rader i journalføringsfil: 1"));
		assertThat(lines.get(2), is("Antall brev i dokumentbestilling: 1"));
	}

	@Test
	public void shouldRunOkAndNotValidateTKnr2() throws Exception {
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_single_ok.csv", JOURNALDATA_CSV);
		copyFileFromInputSourceFolderToInputFileFolder("linjedata_single_ok_should_not_validate_TKnr2.crlf.txt", LINJEDATA_TXT);
		JobParameters parameters = getDefaultBdok100JobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		validateMessageInQueue();

		List<String> lines = kontrollrapportLines(jobExecution);
		assertThat(lines.get(0), is("Antall dokumenter i linjeprintfil: 1"));
		assertThat(lines.get(1), is("Antall rader i journalføringsfil: 1"));
		assertThat(lines.get(2), is("Antall brev i dokumentbestilling: 1"));
	}

	@Test
	public void shouldEndCompleteWithoutFooter() throws Exception {
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_single_ok_uten_footer.csv", JOURNALDATA_CSV);
		copyFileFromInputSourceFolderToInputFileFolder("linjedata_single_ok_uten_footer.crlf.txt", LINJEDATA_TXT);
		JobParameters parameters = getDefaultBdok100JobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		validateMessageInQueue();

		List<String> lines = kontrollrapportLines(jobExecution);
		assertThat(lines.get(0), is("Antall dokumenter i linjeprintfil: 1"));
		assertThat(lines.get(1), is("Antall rader i journalføringsfil: 1"));
		assertThat(lines.get(2), is("Antall brev i dokumentbestilling: 1"));
	}

	@Test
	public void shouldStartBatchAndEndWithWarningIfAnyAvvik() throws Exception {
		JobParameters parameters = getDefaultBdok100JobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.WARNING));

		assertThat(workspace.output().toFile().list().length, is(0));
		assertThat(databaseOutputService.filesForJob(jobExecution.getJobInstance().getId()).size(), is(3));
		assertThat(workspace.failed().toFile().list().length, is(0));
		validateMessageInQueue();

		// The SFTP handler is replaced by a no-op in this batch-only test.
		assertThat(workspace.behandlet().toFile().list().length, is(0));
		assertThat(workspace.input().toFile().list().length, is(3));

		assertThat(bdok100Repo.countByStatus(FEILET_GSAK), is(0L));
		assertThat(bdok100Repo.countByStatus(BEHANDLET), is(0L));
		assertThat(bdok100Repo.countByStatus(KAN_IKKE_BEHANDLES), is(0L));
	}

	@Test
	public void shouldExitErrorIfGsakFailures() throws Exception {
		Files.delete(workspace.input().resolve(JOURNALDATA_CSV));
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_failgsaktwice.csv", JOURNALDATA_CSV);
		JobExecution jobExecution = launchJob(getDefaultBdok100JobParameters());

		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.ERROR));
		assertThat(bdok100Repo.countByStatus(FEILET_GSAK), is(2L));
	}

	@Test
	public void shouldFailAndKeepTableOfFailedEntries() throws Exception {
		Files.delete(workspace.input().resolve(JOURNALDATA_CSV));
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_failgsaktwice.csv", JOURNALDATA_CSV);
		maxFailuresChunkListener.setMaxFailures(1);
		JobParameters parameters = getDefaultBdok100JobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus().getExitCode(), is(ExitStatus.FAILED.getExitCode()));
		assertThat(bdok100Repo.countByStatus(FEILET_GSAK), is(2L));

		assertThat(workspace.output().toFile().list().length, is(0));
		assertThat(workspace.behandlet().toFile().list().length, is(0));
		validateMessageInQueue();

		assertThat(workspace.failed().toFile().list().length, is(0));
		assertThat(workspace.input().toFile().list().length, is(3));
	}

	@Test
	public void shouldStartBatchAndKeepWorkTable() throws Exception {
		AtomicReference<String> exitStatusAtFinalization = new AtomicReference<>();
		doAnswer(invocation -> {
			JobExecution execution = invocation.getArgument(0);
			exitStatusAtFinalization.set(execution.getExitStatus().getExitCode());
			return null;
		}).when(jobCompletionFileHandler).afterJob(org.mockito.ArgumentMatchers.any(JobExecution.class));
		JobParameters parameters = parameterBuilder().addString(BDOKCommonBatchInputParameters.SKIP_CLEAN_KEY, "true")
				.toJobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.ERROR));
		assertThat(exitStatusAtFinalization.get(), is(NavExitStatus.ERROR.getExitCode()));

		List<Bdok100ArbTbl> all = bdok100Repo.findAll();

		assertThat(bdok100Repo.findByStatus(Bdok100Status.KAN_IKKE_BEHANDLES), hasSize(10));
		assertThat(bdok100Repo.findByStatus(BEHANDLET), hasSize(3));
		assertThat(all, hasSize(13));
		validateMessageInQueue();

		Bdok100ArbTbl behandlet = bdok100Repo.findById("ODIQ210102100021").get();
		assertThat(behandlet.getSaksID(), is(SAK_ID));
		assertThat(behandlet.getFeilstatus(), nullValue());
		assertThat(behandlet.getStatus(), is(BEHANDLET));

		Bdok100ArbTbl manglerLinjedata = bdok100Repo.findById("ODIQ013010900021").get();
		assertThat(manglerLinjedata.getFeilstatus(), is("mangler linjedata"));
		assertThat(manglerLinjedata.getStatus(), is(Bdok100Status.KAN_IKKE_BEHANDLES));

		Bdok100ArbTbl manglerJournaldata = bdok100Repo.findById("ODIQ113010900019").get();
		assertThat(manglerJournaldata.getFeilstatus(), is("mangler journaldata"));
		assertThat(manglerLinjedata.getStatus(), is(Bdok100Status.KAN_IKKE_BEHANDLES));
	}

	@Test
	public void shouldRunCleanOnly() throws Exception {
		bdok100Repo.save(ProduserIkkeRedigerbartDokumentXmlMapperTest.createArbeidstabellRow());

		JobParameters parameters = parameterBuilder().addJobParameter(BDOKCommonBatchInputParameters.CLEAN_KEY, true, Boolean.class)
				.toJobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		assertThat(bdok100Repo.count(), is(0L));
		assertThat(workspace.output().toFile().list().length, is(0));
	}

	@Test
	public void shouldRestartAndCompleteIfGsakReachedMaxFail() throws Exception {
		Files.delete(workspace.input().resolve(JOURNALDATA_CSV));
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_failgsaktwice.csv", JOURNALDATA_CSV);
		maxFailuresChunkListener.setMaxFailures(2);
		JobParameters parameters = getDefaultBdok100JobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(ExitStatus.FAILED));
		List<Bdok100ArbTbl> all = bdok100Repo.findAll();
		assertThat(bdok100Repo.findByStatus(FEILET_GSAK), hasSize(2));
		for (Bdok100ArbTbl bdok100ArbTbl : all) {
			if (bdok100ArbTbl.getJournaldata() != null) {
				bdok100ArbTbl.getJournaldata().setSaksNummer("1261216");
			}
			bdok100Repo.save(bdok100ArbTbl);
		}
		jobExecution = launchJob(parameters);
		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.WARNING));
		assertThat(bdok100Repo.count(), is(0L));
		assertThat(bdok100Repo.countByStatus(FEILET_GSAK), is(0L));
	}

	@Test
	public void shouldRestartAndCompleteIfGsakDidntReachMaxFail() throws Exception {
		Files.delete(workspace.input().resolve(JOURNALDATA_CSV));
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_failgsaktwice.csv", JOURNALDATA_CSV);
		JobParameters parameters = getDefaultBdok100JobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.ERROR));
		List<Bdok100ArbTbl> all = bdok100Repo.findAll();
		assertThat(bdok100Repo.findByStatus(FEILET_GSAK), hasSize(2));
		for (Bdok100ArbTbl bdok100ArbTbl : all) {
			if (bdok100ArbTbl.getJournaldata() != null) {
				bdok100ArbTbl.getJournaldata().setSaksNummer("1261216");
			}
			bdok100Repo.save(bdok100ArbTbl);
		}
		jobExecution = launchJob(parameters);
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		assertThat(bdok100Repo.count(), is(0L));
		assertThat(bdok100Repo.countByStatus(FEILET_GSAK), is(0L));

		assertThat(workspace.output().toFile().list().length, is(0));
		assertThat(databaseOutputService.filesForJob(jobExecution.getJobInstance().getId()).size(), is(3));
	}

	@Test
	public void shouldEndWarnIfEmptyInput() throws Exception {
		Files.write(workspace.input().resolve(JOURNALDATA_CSV), new byte[]{});
		Files.write(workspace.input().resolve(LINJEDATA_TXT), new byte[]{});
		JobExecution jobExecution = launchJob(getDefaultBdok100JobParameters());
		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.WARNING));
	}

	private List<String> kontrollrapportLines(JobExecution jobExecution) {
		ArgumentCaptor<byte[]> content = ArgumentCaptor.forClass(byte[].class);
		verify(remoteReportWriter, atLeastOnce()).writeFile(
			argThat(path -> path.startsWith("output/BDOK100_kontrollrapport_"
				+ formatDateToSeconds((Date) jobExecution.getExecutionContext().get(START_TIME_KEY)))), content.capture());
		return new String(content.getValue(), BDOK100_OUTPUT_CHARSET).lines().toList();
	}

	private void validateMessageInQueue() {
		while(getJmsMessageCount()>0) {
			Object data = jmsBdok100Template.receiveAndConvert();
			assertTrue(((JAXBElement) data).getValue() instanceof ProduserIkkeRedigerbartDokument);
			ProduserIkkeRedigerbartDokument produserIkkeRedigerbartDokument = ((ProduserIkkeRedigerbartDokument) ((JAXBElement) data)
					.getValue());
			assertThat(produserIkkeRedigerbartDokument.getDokumentbestillingsinformasjon(), notNullValue());
			assertThat(produserIkkeRedigerbartDokument.getBrevdata(), notNullValue());
		}
	}
}
