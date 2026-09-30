package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import com.google.common.collect.Lists;
import jakarta.xml.bind.JAXBElement;
import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.brevogarkiv.batch.common.provider.launch.util.NavExitStatus;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.ProduserIkkeRedigerbartDokumentXmlMapperTest;
import no.nav.dokprod_infotrygdbrev.common.BDOKCommonBatchInputParameters;
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
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.START_TIME_KEY;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.BDOK100_OUTPUT_CHARSET;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.FILE_DATE_FORMAT_MS;
import static no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper.JOURNALDATA_CSV;
import static no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper.LINJEDATA_TXT;
import static no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper.getKontrollrapport;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status.BEHANDLET;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status.FEILET_GSAK;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status.KAN_IKKE_BEHANDLES;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.ProduserIkkeRedigerbartDokumentXmlMapper.BDOK100_PREFIX;
import static no.nav.dokprod_infotrygdbrev.consumer.mock.SakConsumerMock.SAK_ID;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for the BDOK100 Job
 *
 */
public class Bdok100JobTest extends AbstractBdok100BatchTest {
	private static final String UUID_REGEX = "([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}){1}";

	@Before
	public void setUp() throws Exception {
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

		Path kontrollrapport = findKontrollrapport(jobExecution, true);
		List<String> lines = Files.readAllLines(kontrollrapport, BDOK100_OUTPUT_CHARSET);
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

		Path kontrollrapport = findKontrollrapport(jobExecution, true);
		List<String> lines = Files.readAllLines(kontrollrapport, BDOK100_OUTPUT_CHARSET);
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
		Path kontrollrapport = findKontrollrapport(jobExecution, true);
		List<String> lines = Files.readAllLines(kontrollrapport, BDOK100_OUTPUT_CHARSET);
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
		Path kontrollrapport = findKontrollrapport(jobExecution, true);
		List<String> lines = Files.readAllLines(kontrollrapport, BDOK100_OUTPUT_CHARSET);
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
		Path kontrollrapport = findKontrollrapport(jobExecution, true);
		List<String> lines = Files.readAllLines(kontrollrapport, BDOK100_OUTPUT_CHARSET);
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
		Path kontrollrapport = findKontrollrapport(jobExecution, true);
		List<String> lines = Files.readAllLines(kontrollrapport, BDOK100_OUTPUT_CHARSET);
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

		Path kontrollrapport = findKontrollrapport(jobExecution, true);
		List<String> lines = Files.readAllLines(kontrollrapport, BDOK100_OUTPUT_CHARSET);
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

		Path kontrollrapport = findKontrollrapport(jobExecution, true);
		List<String> lines = Files.readAllLines(kontrollrapport, BDOK100_OUTPUT_CHARSET);
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

		Path kontrollrapport = findKontrollrapport(jobExecution, true);
		List<String> lines = Files.readAllLines(kontrollrapport, BDOK100_OUTPUT_CHARSET);
		assertThat(lines.get(0), is("Antall dokumenter i linjeprintfil: 1"));
		assertThat(lines.get(1), is("Antall rader i journalføringsfil: 1"));
		assertThat(lines.get(2), is("Antall brev i dokumentbestilling: 1"));
	}

	@Test
	public void shouldStartBatchMoveToBehandletAndEndWithWarningIfAnyAvvik() throws Exception {
		JobParameters parameters = getDefaultBdok100JobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.WARNING));

		assertThat(tmpOutFileFolder.toFile().list().length, is(4));
		assertThat(tmpOutFileFolder.resolveSibling("failed").toFile().list().length, is(0));
		validateMessageInQueue();

		// files in generated folder with timestamp in name
		Path behandletFolder = tmpOutFileFolder.resolveSibling("behandlet");
		String[] behandlets = behandletFolder.toFile().list();
		assertThat(behandlets.length, is(1));
		assertThat(behandletFolder.resolve(behandlets[0]).toFile().list().length, is(2));

		// contains "static" folder
		assertThat(tmpInputFileFolder.toFile().list().length, is(1));

		assertThat(bdok100Repo.countByStatus(FEILET_GSAK), is(0L));
		assertThat(bdok100Repo.countByStatus(BEHANDLET), is(0L));
		assertThat(bdok100Repo.countByStatus(KAN_IKKE_BEHANDLES), is(0L));
	}

	@Test
	public void shouldExitErrorIfGsakFailures() throws Exception {
		Files.delete(tmpInputFileFolder.resolve(JOURNALDATA_CSV));
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_failgsaktwice.csv", JOURNALDATA_CSV);
		JobExecution jobExecution = launchJob(getDefaultBdok100JobParameters());

		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.ERROR));
		assertThat(bdok100Repo.countByStatus(FEILET_GSAK), is(2L));
	}

	@Test
	public void shouldMoveToFailedFolderOnFailureAndNotEmptyTableOfFailedEntries() throws Exception {
		Files.delete(tmpInputFileFolder.resolve(JOURNALDATA_CSV));
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_failgsaktwice.csv", JOURNALDATA_CSV);
		JobParameters parameters = parameterBuilder().addString(CommonBatchInputParameters.MAX_FAILURES, "1").toJobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus().getExitCode(), is(ExitStatus.FAILED.getExitCode()));
		assertThat(bdok100Repo.countByStatus(FEILET_GSAK), is(2L));

		assertThat(tmpOutFileFolder.toFile().list().length, is(0));
		assertThat(tmpOutFileFolder.resolveSibling("behandlet").toFile().list().length, is(0));
		validateMessageInQueue();

		// files in generated folder with timestamp in name
		Path failedFolder = tmpOutFileFolder.resolveSibling("failed");
		String[] faileds = failedFolder.toFile().list();
		assertThat(faileds.length, is(1));
		assertThat(failedFolder.resolve(faileds[0]).toFile().list().length, is(2));

		// contains "static" folder
		assertThat(tmpInputFileFolder.toFile().list().length, is(1));
	}

	@Test
	public void shouldStartBatchAndKeepWorkTable() throws Exception {
		JobParameters parameters = parameterBuilder().addString(BDOKCommonBatchInputParameters.SKIP_CLEAN_KEY, "true")
				.toJobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.ERROR));

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

		JobParameters parameters = parameterBuilder().addString(BDOKCommonBatchInputParameters.CLEAN_KEY, "true")
				.toJobParameters();
		JobExecution jobExecution = launchJob(parameters);

		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		assertThat(bdok100Repo.count(), is(0L));
		assertThat(tmpOutFileFolder.toFile().list().length, is(0));
	}

	@Test
	public void shouldRestartAndCompleteIfGsakReachedMaxFail() throws Exception {
		Files.delete(tmpInputFileFolder.resolve(JOURNALDATA_CSV));
		copyFileFromInputSourceFolderToInputFileFolder("journaldata_failgsaktwice.csv", JOURNALDATA_CSV);
		JobParameters parameters = parameterBuilder().addString(CommonBatchInputParameters.MAX_FAILURES, "2").toJobParameters();
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
		Path failedFolder = tmpOutFileFolder.resolveSibling("failed");
		failedFolder = failedFolder.resolve(failedFolder.toFile().list()[0]);
		Files.move(failedFolder.resolve(JOURNALDATA_CSV), tmpInputFileFolder.resolve(JOURNALDATA_CSV));
		Files.move(failedFolder.resolve(LINJEDATA_TXT), tmpInputFileFolder.resolve(LINJEDATA_TXT));

		jobExecution = launchJob(parameters);
		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.WARNING));
		assertThat(bdok100Repo.count(), is(0L));
		assertThat(bdok100Repo.countByStatus(FEILET_GSAK), is(0L));
	}

	@Test
	public void shouldRestartAndCompleteIfGsakDidntReachMaxFail() throws Exception {
		Files.delete(tmpInputFileFolder.resolve(JOURNALDATA_CSV));
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
		Path failedFolder = tmpOutFileFolder.resolveSibling("behandlet");
		failedFolder = failedFolder.resolve(failedFolder.toFile().list()[0]);
		Files.move(failedFolder.resolve(JOURNALDATA_CSV), tmpInputFileFolder.resolve(JOURNALDATA_CSV));
		Files.move(failedFolder.resolve(LINJEDATA_TXT), tmpInputFileFolder.resolve(LINJEDATA_TXT));
		Files.delete(failedFolder);

		jobExecution = launchJob(parameters);
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		assertThat(bdok100Repo.count(), is(0L));
		assertThat(bdok100Repo.countByStatus(FEILET_GSAK), is(0L));

		// avvik_lpf, avvik_jff, avvikrapport, kontrollrapport_1, kontrollrapport_2, xml_1, xml_2
		assertThat(tmpOutFileFolder.toFile().list().length, is(5));
	}

	@Test
	public void shouldEndWarnIfEmptyInput() throws Exception {
		Files.write(tmpInputFileFolder.resolve(JOURNALDATA_CSV), new byte[]{});
		Files.write(tmpInputFileFolder.resolve(LINJEDATA_TXT), new byte[]{});
		JobExecution jobExecution = launchJob(getDefaultBdok100JobParameters());
		assertThat(jobExecution.getExitStatus(), is(NavExitStatus.WARNING));
	}

	private Path findKontrollrapport(JobExecution jobExecution, boolean first) {
		String kontrollrapportName = getKontrollrapport((Date) jobExecution.getExecutionContext().get(START_TIME_KEY));
		ArrayList<String> files = Lists.newArrayList(tmpOutFileFolder.toFile().list());
		Collections.sort(files);
		if (!first) {
			Collections.reverse(files);
		}
		for (String filename : files) {
			if (filename.startsWith(kontrollrapportName.substring(0, kontrollrapportName.length() - (FILE_DATE_FORMAT_MS + ".txt")
					.length()))) {
				kontrollrapportName = filename;
				break;
			}
		}
		return tmpOutFileFolder.resolve(kontrollrapportName);
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
