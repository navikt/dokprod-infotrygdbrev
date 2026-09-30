package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.LINJEDATA_MED_TOPPTEKST;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.readLinjedataFile;
import static no.nav.dokprod_infotrygdbrev.support.StrUtils.createRandomId;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;

import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Brevtype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Spraak;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;

/**
 * Step test for createKontrollrapportStep
 *
 */
public class CreateKontrollrapportStepTest extends AbstractBdok100StepTest {

	private static final Date START_TIME = LocalDate.parse("2016-01-01").toDate();
	private static final String FILENAME_START = "BDOK100_kontrollrapport_2016-01-01_00-00-00";

	@Autowired
	private Bdok100Repo repo;

	@Before
	public void setUp() throws Exception {
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow(createRandomId(16))
				.lineprintBrev(readLinjedataFile(LINJEDATA_MED_TOPPTEKST))
				.status(Bdok100Status.BEHANDLET)
				.build());
		saveArbTblWithStatus(Bdok100Status.BEHANDLET);
		saveArbTblWithStatus(Bdok100Status.KAN_IKKE_BEHANDLES);
		saveArbTblWithStatus(Bdok100Status.UNDER_INNLESNING_JFF);
		saveArbTblWithStatus(Bdok100Status.KAN_IKKE_BEHANDLES);
		saveArbTblWithStatus(Bdok100Status.FEILET_GSAK);
		saveArbTblWithStatus(Bdok100Status.FEILET_GSAK);
		saveArbTblWithStatus(Bdok100Status.FEILET_GSAK);
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow(createRandomId(16))
				.lineprintBrev("linjeprint")
				.linjedata(Bdok100TestdataUtil.createLinjedata()
						.infotrygdBrevkodePage("FLXXX_YY")
						.brevtype(Brevtype.OK)
						.tknr1("2321")
						.build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow(createRandomId(16))
				.lineprintBrev("linjeprint")
				.linjedata(Bdok100TestdataUtil.createLinjedata()
						.infotrygdBrevkodePage("FLXXX_ZZ")
						.brevtype(Brevtype.BP)
						.tknr1("2321")
						.build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow(createRandomId(16))
				.lineprintBrev("linjeprint")
				.linjedata(Bdok100TestdataUtil.createLinjedata().brevnavn("A001").spraak(Spraak.B).build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow(createRandomId(16))
				.lineprintBrev("linjeprint")
				.linjedata(Bdok100TestdataUtil.createLinjedata().brevnavn("A001").spraak(Spraak.B).build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow(createRandomId(16))
				.lineprintBrev("linjeprint")
				.linjedata(Bdok100TestdataUtil.createLinjedata().brevnavn("B001").spraak(Spraak.B).build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow(createRandomId(16))
				.lineprintBrev("linjeprint")
				.linjedata(Bdok100TestdataUtil.createLinjedata().brevtype(Brevtype.OK).build())
				.build());

		createArbTblWithTkNr1("2399");
		createArbTblWithTkNr1("2400");
		createArbTblWithTkNr1("4400");

		createArbTblWithTkNr1("4499");
		createArbTblWithTkNr1("4500");
		createArbTblWithTkNr1("4699");
		createArbTblWithTkNr1("4700");

		createArbTblWithTkNr1("4899");
		createArbTblWithTkNr1("4900");
		createArbTblWithTkNr1("4901");
		createArbTblWithTkNr1("9999");

		createArbTblWithBrevnavnStartingWith("ZH2");
		createArbTblWithBrevnavnStartingWith("ZE2");
		createArbTblWithBrevnavnStartingWith("Z10");
		createArbTblWithBrevnavnStartingWith("Z10");
		createArbTblWithBrevnavnStartingWith("Z10");
		createArbTblWithBrevnavnStartingWith("Z20");
		createArbTblWithBrevnavnStartingWith("Z30");
		createArbTblWithBrevnavnStartingWith("Z30");
		createArbTblWithBrevnavnStartingWith("Z40");
		createArbTblWithBrevnavnStartingWith("ZH3");
		createArbTblWithBrevnavnStartingWith("ZE3");
		createArbTblWithBrevnavnStartingWith("ZE3");
		createArbTblWithBrevnavnStartingWith("ZE3");
		createArbTblWithBrevnavnStartingWith("ZE3");
	}

	@Test
	public void shouldCreateKontrollrapport() throws Exception {
		ExecutionContext jobExecutionContext = getJobExecutionContext();
		jobExecutionContext.put(CommonBatchInputParameters.START_TIME_KEY, START_TIME);

		JobExecution jobExecution = launchStep("createKontrollrapportStep", getDefaultBdok100JobParameters(), jobExecutionContext);
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		String[] list = tmpOutFileFolder.toFile().list();
		String filename = list != null ? list[0] : "";
		assertThat(filename, startsWith(FILENAME_START));
		String outputFileAsString = getOutputFileAsString(filename, Bdok100Constants.BDOK100_OUTPUT_CHARSET);
		assertThat(outputFileAsString, containsString("Antall dokumenter i linjeprintfil: 7"));
		assertThat(outputFileAsString, containsString("Antall rader i journalføringsfil: 0"));
		assertThat(outputFileAsString, containsString("Antall brev i dokumentbestilling: 2"));
		assertThat(outputFileAsString, containsString("Antall rader feilet: 2"));
		assertThat(outputFileAsString, containsString("Antall rader feilet teknisk: 3"));
		assertThat(outputFileAsString, containsString("Antall brev der NAV-nr = starter med '23': 3"));
		assertThat(outputFileAsString, containsString("Antall brev der brevnavn starter på ZH2,ZE2,Z10,Z20,Z30,Z40,ZH3 eller ZE3: 14"));
		assertThat(outputFileAsString, containsString("Antall brev der NAV-nr er i mellom (2399,2820),(2822,2999),(3100,3399),(3500,3799),(3900,4199),(4300,4400),(4500,4600) eller (5999,10000): 6"));
		assertThat(outputFileAsString, containsString("Antall brev der Infotrygd_BrevkodePage = FLXXX_YY: 1"));
		assertThat(outputFileAsString, containsString("Antall brev der brevtype <> OK: 1"));
		assertThat(outputFileAsString, containsString("Antall brev gruppert på brevkode (inneholder også ignorerte brev):"));
		assertThat(outputFileAsString, containsString("brevkode=AA08B: 4"));
		assertThat(outputFileAsString, containsString("brevkode=A001B: 2"));
		assertThat(outputFileAsString, containsString("brevkode=B001B: 1"));
	}

	private void createArbTblWithTkNr1(String tknr1) {
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow(createRandomId(16))
				.linjedata(Bdok100TestdataUtil.createLinjedata().tknr1(tknr1).build())
				.build());
	}

	private void createArbTblWithBrevnavnStartingWith(String brevnavn) {
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow(createRandomId(16))
				.linjedata(Bdok100TestdataUtil.createLinjedata().brevnavn(brevnavn + createRandomId(2)).build())
				.build());
	}

	private void saveArbTblWithStatus(Bdok100Status status) {
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow(createRandomId(16))
				.status(status)
				.build());
	}
}