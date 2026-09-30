package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.VedleggsLister;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Itest for read Vedlegg step
 *
 */
public class ReadVedleggsListeStepTest extends AbstractBdok100StepTest {

	@Autowired
	private VedleggsLister lister;

	@Test
	public void testLaunchMapperStep() throws Exception {
		JobExecution jobExecution = launchStep("bulkReadVedleggsListeFileToMapStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		assertThat(lister.getVedleggsListe("0DI0"), notNullValue());
		VedleggsListe vedleggsListe = lister.getVedleggsListe("EFGJ");
		assertThat(vedleggsListe.getVedleggs(), hasSize(2));
		assertThat(vedleggsListe.getVedleggs().get(0).getVedleggsKode(), is("NAV_15-00.05"));
		assertThat(vedleggsListe.getVedleggs().get(1).getVedleggsKode(), is("NAV_21-12.05"));
		assertThat(lister.getVedleggsListe("YSG2"), notNullValue());
	}

	@Override
	protected ExecutionContext getJobExecutionContext() throws Exception {
		String vedleggResource = new ClassPathResource("bdok100/vedlegg/valid_infotrygd_vedlegg.txt").getFile().getPath();

		ExecutionContext executionContext = getDefaultCommonJobExecutionContext();
		executionContext.put(Bdok100Constants.CURRENT_VEDLEGG, vedleggResource);
		executionContext.putLong(CommonBatchInputParameters.WORK_UNIT_KEY, 2L);
		return executionContext;
	}

}
