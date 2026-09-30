package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.BREVNAVN;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.createArbeidstabellRow;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.createNavKontor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Spraak;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.VedleggsLister;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration test for xmlDokumentbestillingWriterStep
 *
 */
public class XmlWriterStepTest extends AbstractBdok100StepTest {
	public static final String ID_NR = "ODIQ01301090004";

	@Autowired
	private NAVKontors navKontors;
	@Autowired
	private VedleggsLister vedleggsLister;

	@Autowired
	private Bdok100Repo bdok100Repo;

	@Before
	public void setUp() throws Exception {
		bdok100Repo.save(createArbeidstabellRow().status(Bdok100Status.UNDER_INNLESNING_LPF).idnr(ID_NR + 1).build());
		bdok100Repo.save(createArbeidstabellRow().status(Bdok100Status.UNDER_INNLESNING_JFF).idnr(ID_NR + 2).build());
		bdok100Repo.save(createArbeidstabellRow().status(Bdok100Status.MAPPING_LPF).idnr(ID_NR + 3).build());
		bdok100Repo.save(createArbeidstabellRow().status(Bdok100Status.MAPPING_JFF).idnr(ID_NR + 4).build());

		bdok100Repo.save(createArbeidstabellRow().status(Bdok100Status.TIL_BEHANDLING).idnr(ID_NR + 5).build());
		bdok100Repo.save(createArbeidstabellRow().status(Bdok100Status.TIL_BEHANDLING).idnr(ID_NR + 6).build());

		createTos();
	}

	@Test
	public void test() throws Exception {
		JobExecution jobExecution = launchStep("xmlDokumentbestillingWriterStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		assertThat(jobExecution.getStepExecutions().iterator().next().getWriteCount(), is(2L));
	}

	private void createTos() {
		vedleggsLister.clear();
		navKontors.clear();
		vedleggsLister.add(VedleggsListe.builder().brevkode(BREVNAVN + Spraak.B)
				.vedleggs(Lists.newArrayList(new VedleggsListe.Vedlegg("v1"), new VedleggsListe.Vedlegg("v2")))
				.build());
		navKontors.add(createNavKontor().build());
	}

}
