package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.FOR_LITE_MELLOMROM_MELLOM_ADRESSE_OG_BREVTEKST;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.LINJEDATA_MED_BREVTEKST2;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.LINJEDATA_MED_TOPPTEKST;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.readLinjedataFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.NAVKontor;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * Integration test for linjedataMapperStep
 *
 */
public class LinjedataMapperStepTest extends AbstractBdok100StepTest {

	private static final String ID_NR = "ODIQ013010900049";
	private static final String ID_NR_2 = "ODIQ013010900010";
	private static final String ID_NR_3 = "ODIQ013010900011";
	private static final String ID_NR_4 = "ODIQ013010900012";

	private static final String TKNR = "0101";

	@Autowired
	private NAVKontors navKontors;
	@Autowired
	private Bdok100Repo bdok100Repo;

	@Before
	public void setUp() throws Exception {
		bdok100Repo.save(Bdok100ArbTbl.builder()
				.idnr(ID_NR)
				.status(Bdok100Status.UNDER_INNLESNING_JFF)
				.lineprintBrev(readLinjedataFile(LINJEDATA_MED_TOPPTEKST))
				.build());
		bdok100Repo.save(Bdok100ArbTbl.builder()
				.idnr(ID_NR_2)
				.status(Bdok100Status.UNDER_INNLESNING_JFF)
				.lineprintBrev(readLinjedataFile(LINJEDATA_MED_BREVTEKST2))
				.build());
		bdok100Repo.save(Bdok100ArbTbl.builder()
				.idnr(ID_NR_3)
				.status(Bdok100Status.UNDER_INNLESNING_JFF)
				.lineprintBrev(null)
				.build());

		bdok100Repo.save(Bdok100ArbTbl.builder()
				.idnr(ID_NR_4)
				.status(Bdok100Status.UNDER_INNLESNING_JFF)
				.lineprintBrev(readLinjedataFile(FOR_LITE_MELLOMROM_MELLOM_ADRESSE_OG_BREVTEKST))
				.build());

		navKontors.clear();
		navKontors.add(NAVKontor.builder().tkNr(TKNR).build());
	}

	@Test
	public void shouldRunStep() throws Exception {
		JobExecution jobExecution = launchStep("mapLinjedataStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		List<Bdok100ArbTbl> bdok100ArbTbl = bdok100Repo.findByStatus(Bdok100Status.MAPPING_LPF);
		assertThat(bdok100ArbTbl, hasSize(2));
		assertThat(bdok100ArbTbl.get(0).getLinjedata().getTknr2(), is(TKNR));
		assertThat(bdok100ArbTbl.get(1).getLinjedata().getTknr2(), is(TKNR));

		List<Bdok100ArbTbl> failed = bdok100Repo.findByStatus(Bdok100Status.KAN_IKKE_BEHANDLES);
		assertThat(failed, hasSize(2));
	}

	@Test
	public void shouldSaveOkIfFieldValidationFails() throws Exception {
		String linjedata = readLinjedataFile(LINJEDATA_MED_TOPPTEKST);
		Bdok100ArbTbl one = bdok100Repo.findById(ID_NR).get();
		one.setLineprintBrev(linjedata.substring(0, 6) + "x3" + linjedata.substring(8));
		bdok100Repo.save(one);

		JobExecution jobExecution = launchStep("mapLinjedataStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		one = bdok100Repo.findById(ID_NR).get();
		assertThat(one.getLinjedata(), notNullValue());
		assertThat(one.getLinjedata().getAar(), is("x3"));
	}

	@Test
	public void shouldFailWhenLessThanTwoLinesBetweenAdressAndLetterText() throws Exception {

		JobExecution jobExecution = launchStep("mapLinjedataStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		Bdok100ArbTbl one = bdok100Repo.findById(ID_NR_4).get();
		assertThat(one, notNullValue());
		assertThat(one.getFeilstatus(), is(equalTo("Linjedata mapping error: Too many address lines")) );
		assertThat(one.getStatus(), is(equalTo(Bdok100Status.KAN_IKKE_BEHANDLES)) );
	}
}
