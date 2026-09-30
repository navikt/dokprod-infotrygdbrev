package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.LINJEDATA_MED_TOPPTEKST;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.readLinjedataFile;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import no.nav.dokprod_infotrygdbrev.support.StrUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Step test for BDOK100 CleanArbeidsTabellStep
 *
 */
public class CleanArbeidsTabellStepTest extends AbstractBdok100StepTest {

	@Autowired
	private Bdok100Repo repo;

	@Before
	public void setUp() throws Exception {
		saveWithStatus(Bdok100Status.KAN_IKKE_BEHANDLES);
		saveWithStatus(Bdok100Status.TIL_RAPPORT);
		saveWithStatus(Bdok100Status.MAPPING_JFF);
		saveWithStatus(Bdok100Status.MAPPING_LPF);
		saveWithStatus(Bdok100Status.UNDER_INNLESNING_JFF);
		saveWithStatus(Bdok100Status.UNDER_INNLESNING_LPF);
		saveWithStatus(Bdok100Status.BEHANDLET);
		saveWithStatus(Bdok100Status.FEILET_GSAK);
	}

	@Test
	public void shouldCleanArbeidstabell() throws Exception {
		entityManager.clear();
		JobExecution jobExecution = launchStep("cleanArbeidsTabellStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		assertThat(repo.countByStatus(Bdok100Status.KAN_IKKE_BEHANDLES), is(0L));
		assertThat(repo.countByStatus(Bdok100Status.TIL_RAPPORT), is(0L));
		assertThat(repo.countByStatus(Bdok100Status.MAPPING_JFF), is(0L));
		assertThat(repo.countByStatus(Bdok100Status.MAPPING_LPF), is(0L));
		assertThat(repo.countByStatus(Bdok100Status.UNDER_INNLESNING_JFF), is(0L));
		assertThat(repo.countByStatus(Bdok100Status.UNDER_INNLESNING_LPF), is(0L));
		assertThat(repo.countByStatus(Bdok100Status.BEHANDLET), is(0L));

		assertThat(repo.countByStatus(Bdok100Status.FEILET_GSAK), is(1L));
	}

	private void saveWithStatus(Bdok100Status status) {
		repo.save(Bdok100ArbTbl.builder()
				.idnr(StrUtils.createRandomId(16))
				.status(status)
				.lineprintBrev(readLinjedataFile(LINJEDATA_MED_TOPPTEKST))
				.build());
	}
}
