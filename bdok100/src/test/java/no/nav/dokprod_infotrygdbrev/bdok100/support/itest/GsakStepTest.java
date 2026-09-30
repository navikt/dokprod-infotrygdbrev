package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import static no.nav.brevogarkiv.batch.common.MaxFailuresChunkListener.NO_OF_FAILURES_KEY;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.createArbeidstabellRow;
import static no.nav.dokprod_infotrygdbrev.consumer.mock.SakConsumerMock.ERROR_ID;
import static no.nav.dokprod_infotrygdbrev.consumer.mock.SakConsumerMock.SAK_ID;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Integration test for xmlDokumentbestillingWriterStep
 *
 */
public class GsakStepTest extends AbstractBdok100StepTest {
	public static final String ID_NR = "ODIQ01301090004";
	public static final String ID_HAS_ALREADY = ID_NR + 3;
	public static final String HAS_ID_ALREADY = "7895462";

	@Autowired
	private Bdok100Repo bdok100Repo;

	@Before
	public void setUp() throws Exception {
		bdok100Repo.save(createRow(ID_NR + 0, Bdok100Status.UNDER_INNLESNING_LPF, null));
		bdok100Repo.save(createRow(ID_NR + 1, Bdok100Status.MAPPING_LPF, null));
		bdok100Repo.save(createRow(ID_HAS_ALREADY, Bdok100Status.TIL_BEHANDLING, HAS_ID_ALREADY));

		bdok100Repo.save(createRow(ID_NR + 4, Bdok100Status.FEILET_GSAK, null));
		bdok100Repo.save(createRow(ID_NR + 5, Bdok100Status.FEILET_GSAK, null));
		bdok100Repo.save(createRow(ID_NR + 6, Bdok100Status.MAPPING_JFF, null));
		bdok100Repo.save(createRow(ID_NR + 7, Bdok100Status.MAPPING_JFF, null));
		bdok100Repo.save(createRow(ID_NR + 8, Bdok100Status.MAPPING_JFF, null));
		bdok100Repo.save(createRow(ID_NR + 9, Bdok100Status.MAPPING_JFF, null));
	}

	@Test
	public void shouldCallGsak() throws Exception {
		JobExecution jobExecution = launchStep("callGsakStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		List<Bdok100ArbTbl> byStatus = bdok100Repo.findByStatus(Bdok100Status.TIL_BEHANDLING);
		int newTilBehandling = 6;
		int old = 1;
		assertThat(byStatus.size(), is(newTilBehandling + old));
		assertThat(jobExecution.getStepExecutions().iterator().next().getWriteCount(), is((long) newTilBehandling));

		for (Bdok100ArbTbl arbTbl : byStatus) {
			if (arbTbl.getIdnr().equals(ID_HAS_ALREADY)) {
				assertThat(arbTbl.getSaksID(), is(HAS_ID_ALREADY));
			} else {
				assertThat(arbTbl.getSaksID(), is(SAK_ID));

			}
		}
		assertThat(jobExecution.getExecutionContext().getLong(NO_OF_FAILURES_KEY), is(0L));
	}

	@Test
	public void shouldCatchFailuresAndIncrementWarning() throws Exception {
		Bdok100ArbTbl four = bdok100Repo.findById(ID_NR + 4).get();
		four.getJournaldata().setSaksNummer(ERROR_ID);
		bdok100Repo.save(four);

		JobExecution jobExecution = launchStep("callGsakStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		List<Bdok100ArbTbl> failed = bdok100Repo.findByStatus(Bdok100Status.FEILET_GSAK);
		assertThat(failed, hasSize(1));

		assertThat(jobExecution.getExecutionContext().getLong(NO_OF_FAILURES_KEY), is(1L));
	}

	@Test
	public void shouldFailIfOverMaxFailuresAndSaveStatusForFailedRows() throws Exception {
		Bdok100ArbTbl four = bdok100Repo.findById(ID_NR + 4).get();
		four.getJournaldata().setSaksNummer(ERROR_ID);
		Bdok100ArbTbl five = bdok100Repo.findById(ID_NR + 5).get();
		five.getJournaldata().setSaksNummer(ERROR_ID);
		bdok100Repo.save(four);
		bdok100Repo.save(five);

		JobExecution jobExecution = launchStep("callGsakStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus().getExitCode(), is(ExitStatus.FAILED.getExitCode()));
		assertThat(jobExecution.getExitStatus().getExitDescription(), containsString("Number of failures (2) reached the maxFailures (2) threshold."));
		assertThat(bdok100Repo.findByStatus(Bdok100Status.FEILET_GSAK), hasSize(2));

		assertThat(jobExecution.getExecutionContext().getLong(NO_OF_FAILURES_KEY), is(2L));
	}

	private Bdok100ArbTbl createRow(String idnr, Bdok100Status status, String saksID) {
		return createArbeidstabellRow()
				.saksID(saksID).status(status).idnr(idnr)
				.build();
	}
}
