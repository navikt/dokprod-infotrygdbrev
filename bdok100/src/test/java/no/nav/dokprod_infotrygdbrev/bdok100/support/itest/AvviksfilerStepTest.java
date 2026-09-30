package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.CRLF;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.LINJEDATA_MED_TOPPTEKST;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.readLinjedataFile;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import org.hamcrest.Matcher;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Date;

/**
 * Step test for AvviksfilerStep
 *
 */
public class AvviksfilerStepTest extends AbstractBdok100StepTest {

	private static final Date START_TIME = LocalDate.parse("2016-01-01").toDate();
	private static final String LPF_FILENAME = "BDOK100_avvik_LPF_2016-01-01_00-00-00.txt";
	private static final String JFF_FILENAME = "BDOK100_avvik_JFF_2016-01-01_00-00-00.txt";
	private static final String READ_LINJEDATA_FILE = readLinjedataFile(LINJEDATA_MED_TOPPTEKST);
	private static final String JOURNALDATA = "ODIQ210102100001,INFOT_UT,0106B01,IT01,FOS,0106,ABC123,Ukjent,FS,2010-10-21,11111111111,PERSON,Innhold,11111111111,TESTESEN,,U,B,FO03,,S,T,F,2010-10-05,2010-10-21";
	private static final String STEP_NAME = "createAvviksfilerStep";

	@Autowired
	private Bdok100Repo repo;

	@Test
	public void shouldWriteToBothAvviksfilesWhenManAvviksfilLFPTrue() throws Exception {
		repo.save(Bdok100ArbTbl.builder()
				.idnr("ODIQ013010900000")
				.feilstatus("Something is wrong")
				.status(Bdok100Status.KAN_IKKE_BEHANDLES)
				.lineprintBrev(READ_LINJEDATA_FILE)
				.journalforingsfil(JOURNALDATA)
				.build());

		JobExecution jobExecution = launchStep(STEP_NAME, getDefaultBdok100JobParameters(), getExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		assertOutputFiles();
	}

	@Test
	public void shouldWriteToAvviksfilesWhenManAvviksfilJFFTrue() throws Exception {
		repo.save(Bdok100ArbTbl.builder()
				.idnr("ODIQ013010900000")
				.feilstatus("Something is wrong")
				.status(Bdok100Status.KAN_IKKE_BEHANDLES)
				.lineprintBrev(READ_LINJEDATA_FILE)
				.journalforingsfil(JOURNALDATA)
				.build());

		JobExecution jobExecution = launchStep(STEP_NAME, getDefaultBdok100JobParameters(), getExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		assertOutputFiles();
	}

	@Test
	public void shouldNotWriteToAvviksfilesWhenManAvvikFlagsIsFalse() throws Exception {
		repo.save(Bdok100ArbTbl.builder()
				.idnr("ODIQ013010900000")
				.feilstatus("Something is wrong")
				.status(Bdok100Status.UNDER_INNLESNING_JFF)
				.lineprintBrev(READ_LINJEDATA_FILE)
				.journalforingsfil(JOURNALDATA)
				.build());

		JobExecution jobExecution = launchStep(STEP_NAME, getDefaultBdok100JobParameters(), getExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		assertFile(LPF_FILENAME, isEmptyString());
		assertFile(JFF_FILENAME, isEmptyString());
	}

	@Test
	public void shouldNotWriteNullToFile() throws Exception {
		repo.save(Bdok100ArbTbl.builder()
				.idnr("ODIQ013010900000")
				.feilstatus("Something is wrong")
				.status(Bdok100Status.KAN_IKKE_BEHANDLES)
				.lineprintBrev(READ_LINJEDATA_FILE)
				.journalforingsfil(null)
				.build());

		repo.save(Bdok100ArbTbl.builder()
				.idnr("ODIQ013010900001")
				.feilstatus("Something is wrong")
				.status(Bdok100Status.KAN_IKKE_BEHANDLES)
				.lineprintBrev(null)
				.journalforingsfil(JOURNALDATA)
				.build());

		JobExecution jobExecution = launchStep(STEP_NAME, getDefaultBdok100JobParameters(), getExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		assertOutputFiles();
	}

	private void assertOutputFiles() {
		assertFile(LPF_FILENAME, is(READ_LINJEDATA_FILE + CRLF));
		assertFile(JFF_FILENAME, is(JOURNALDATA + CRLF));
	}

	private void assertFile(String filename, Matcher<String> matcher) {
		String journaldataOutput = getOutputFileAsString(filename, Bdok100Constants.BDOK100_INPUT_CHARSET);
		assertThat(journaldataOutput, matcher);
	}

	private ExecutionContext getExecutionContext() throws Exception {
		ExecutionContext jobExecutionContext = getJobExecutionContext();
		jobExecutionContext.put(CommonBatchInputParameters.START_TIME_KEY, START_TIME);
		return jobExecutionContext;
	}
}
