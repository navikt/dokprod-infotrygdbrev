package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.LINJEDATA_MED_TOPPTEKST;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.readLinjedataFile;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Date;

/**
 * Step test for BDOK100 createAvviksrapportStep
 *
 */
public class AvviksrapportStepTest extends AbstractBdok100StepTest {

	private static final Date START_TIME = LocalDate.parse("2016-01-01").toDate();
	private static final String FILENAME = "BDOK100_avviksrapport_2016-01-01_00-00-00.txt";

	@Autowired
	private Bdok100Repo repo;

	@Before
	public void setUp() throws Exception {
		repo.save(Bdok100ArbTbl.builder()
				.idnr("ODIQ013010900000")
				.feilstatus("Something is wrong")
				.status(Bdok100Status.KAN_IKKE_BEHANDLES)
				.lineprintBrev(readLinjedataFile(LINJEDATA_MED_TOPPTEKST))
				.build());
		repo.save(Bdok100ArbTbl.builder()
				.idnr("ODIQ013010900001")
				.feilstatus("Something is even more wrong")
				.status(Bdok100Status.KAN_IKKE_BEHANDLES)
				.lineprintBrev(readLinjedataFile(LINJEDATA_MED_TOPPTEKST))
				.build());
	}

	@Test
	public void shouldPrintAvviksrapport() throws Exception {
		ExecutionContext jobExecutionContext = getJobExecutionContext();
		jobExecutionContext.put(CommonBatchInputParameters.START_TIME_KEY, START_TIME);

		JobExecution jobExecution = launchStep("createAvviksrapportStep", getDefaultBdok100JobParameters(), jobExecutionContext);
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		String outputFileAsString = getOutputFileAsString(FILENAME, Bdok100Constants.BDOK100_OUTPUT_CHARSET);
		assertThat(outputFileAsString, containsString("ArkivID = ODIQ013010900000 Brev feilet, Something is wrong"));
		assertThat(outputFileAsString, containsString("ArkivID = ODIQ013010900001 Brev feilet, Something is even more wrong"));
	}
}
