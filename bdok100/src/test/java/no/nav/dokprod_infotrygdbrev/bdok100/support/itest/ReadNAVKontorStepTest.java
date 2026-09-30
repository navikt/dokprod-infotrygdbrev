package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.NAVKontor;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Itest for read Nav Kontor step
 *
 */
public class ReadNAVKontorStepTest extends AbstractBdok100StepTest {

	private static final String KONTOR_FILE = "bdok100/kontor/FF01_ref_dynamisk_kontor_info_infotryd.dat";

	@Test
	public void testLaunchMapperStep() throws Exception {
		JobExecution jobExecution = launchStep("bulkReadNAVKontorFileToMapStep", getDefaultBdok100JobParameters(), getSpecialJobExecutionContext(KONTOR_FILE));
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		assertThat("Unexpected contents in kontors: " + kontors, kontors.size(), is(5));

		NAVKontor navKontor0100 = kontors.getNavKontor("0215");
		assertThat(navKontor0100.getTkNr(), is("0215"));
		assertThat(navKontor0100.getTKName(), is("NAV Frogn"));
		assertThat(navKontor0100.getOrgNummer(), is("993264067"));
		assertThat(navKontor0100.getBankGiroRef(), is("00000222222"));
		assertThat(navKontor0100.getPostGiroRef(), is("22222000000"));
		assertThat(navKontor0100.getPostadresse1(), is("Boks 20"));
		assertThat(navKontor0100.getPostadresse2(), is(""));
		assertThat(navKontor0100.getPostNr(), is("1441"));
		assertThat(navKontor0100.getPoststed(), is("DRØBAK"));
		assertThat(navKontor0100.getBesoeksadresse1(), is("Rådhusveien 6"));
		assertThat(navKontor0100.getBesoeksadresse2(), is(""));
		assertThat(navKontor0100.getPostNrBesoeksadresse(), is("1440"));
		assertThat(navKontor0100.getPoststedBesoeksadresse(), is("DRØBAK"));
		assertThat(navKontor0100.getTelefon(), is("55553333"));
		assertThat(navKontor0100.getTelefaks(), is(""));
		assertThat(navKontor0100.getAapningstid1(), is("08.00-15.00"));
		assertThat(navKontor0100.getAapningstid2(), is(""));
		assertThat(navKontor0100.getAapningstid3(), is(""));

		NAVKontor navKontor0624 = kontors.getNavKontor("0624");
		assertThat(navKontor0624.getPostadresse1(), is("Postboks 97"));
		assertThat(navKontor0624.getPostadresse2(), is(""));
		assertThat(navKontor0624.getBesoeksadresse1(), is(""));
		assertThat(navKontor0624.getBesoeksadresse2(), is("Stasjonsgata 61"));
		assertThat(navKontor0624.getAapningstid1(), is("10:00-15:00"));
		assertThat(navKontor0624.getAapningstid2(), is("BANKKONTONR"));
		assertThat(navKontor0624.getAapningstid3(), is("1111 22 33333"));
	}

	private ExecutionContext getSpecialJobExecutionContext(String kontorFile) throws Exception {
		String vedleggResource = new ClassPathResource(kontorFile).getFile().getPath();

		ExecutionContext executionContext = getDefaultCommonJobExecutionContext();
		executionContext.put(Bdok100Constants.CURRENT_NAVKONTOR, vedleggResource);
		executionContext.putLong(CommonBatchInputParameters.WORK_UNIT_KEY, 2L);
		return executionContext;
	}

}
