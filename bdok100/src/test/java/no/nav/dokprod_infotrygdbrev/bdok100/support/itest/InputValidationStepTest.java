package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import static no.nav.dokprod_infotrygdbrev.common.BDOKCommonBatchInputParameters.CLEAN_KEY;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.NAVKontor;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.VedleggsLister;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.infrastructure.item.ExecutionContext;

import org.springframework.beans.factory.annotation.Autowired;
import java.nio.file.Files;

/**
 * Step test for input validation bdok100
 *
 */
public class InputValidationStepTest extends AbstractBdok100StepTest {

	public static final String STEP_NAME = "inputValidationStep";
	@Autowired
	private NAVKontors navKontors;
	@Autowired
	private VedleggsLister vedleggsLister;

	@Before
	public void setUp() throws Exception {
		setupBdok100InputFiles();
	}

	@Test
	public void shouldValidateOk() throws Exception {
		JobExecution jobExecution = launchStep(STEP_NAME, getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
	}

	@Test
	public void shouldIgnoreFilesIfRunClean() throws Exception {
		JobParameters defaultBdok100JobParameters = getDefaultBdok100JobParameters();
		Files.delete(tmpInputFileFolder.resolve(FilenameHelper.JOURNALDATA_CSV));
		Files.delete(tmpInputFileFolder.resolve(FilenameHelper.LINJEDATA_TXT));
		Files.delete(tmpInputFileFolder.resolve("static").resolve(FilenameHelper.KONTOR_CSV));
		Files.delete(tmpInputFileFolder.resolve("static").resolve(FilenameHelper.VEDLEGG_CSV));
		Files.delete(tmpOutFileFolder.resolveSibling("failed"));
		Files.delete(tmpOutFileFolder.resolveSibling("behandlet"));
		Files.delete(tmpOutFileFolder);
		ExecutionContext jobExecutionContext = getJobExecutionContext();
		jobExecutionContext.put(CLEAN_KEY, true);
		JobExecution jobExecution = launchStep(STEP_NAME, defaultBdok100JobParameters, jobExecutionContext);
		assertThat(jobExecution.getExitStatus().getExitCode(), is(ExitStatus.COMPLETED.getExitCode()));
	}

	@Test
	public void shouldCleanKontorAndVedlegg() throws Exception {
		NAVKontor kontor = new NAVKontor();
		kontor.setTkNr("0101");
		navKontors.add(kontor);
		VedleggsListe vedleggsListe = new VedleggsListe();
		vedleggsListe.setBrevkode("brevkode");
		vedleggsLister.add(vedleggsListe);

		JobExecution jobExecution = launchStep(STEP_NAME, getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
		assertThat(navKontors.size(), is(0));
		assertThat(vedleggsLister.size(), is(0));
	}

	@Test
	public void shouldFailMissingJournaldata() throws Exception {
		Files.delete(tmpInputFileFolder.resolve(FilenameHelper.JOURNALDATA_CSV));
		JobExecution jobExecution = launchStep(STEP_NAME, getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus().getExitCode(), is(ExitStatus.FAILED.getExitCode()));
		assertThat(jobExecution.getExitStatus().toString(), containsString(FilenameHelper.JOURNALDATA_CSV));
	}

	@Test
	public void shouldFailMissingLinjedata() throws Exception {
		Files.delete(tmpInputFileFolder.resolve(FilenameHelper.LINJEDATA_TXT));
		JobExecution jobExecution = launchStep(STEP_NAME, getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus().getExitCode(), is(ExitStatus.FAILED.getExitCode()));
		assertThat(jobExecution.getExitStatus().toString(), containsString(FilenameHelper.LINJEDATA_TXT));
	}

	@Test
	public void shouldFailMissingKontordata() throws Exception {
		Files.delete(tmpInputFileFolder.resolve("static").resolve(FilenameHelper.KONTOR_CSV));
		JobExecution jobExecution = launchStep(STEP_NAME, getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus().getExitCode(), is(ExitStatus.FAILED.getExitCode()));
		assertThat(jobExecution.getExitStatus().toString(), containsString(FilenameHelper.KONTOR_CSV));
	}

	@Test
	public void shouldFailMissingVedleggdata() throws Exception {
		Files.delete(tmpInputFileFolder.resolve("static").resolve(FilenameHelper.VEDLEGG_CSV));
		JobExecution jobExecution = launchStep(STEP_NAME, getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus().getExitCode(), is(ExitStatus.FAILED.getExitCode()));
		assertThat(jobExecution.getExitStatus().toString(), containsString(FilenameHelper.VEDLEGG_CSV));
	}

	@Test
	public void shouldFailCannotWriteBehandlet() throws Exception {
		JobParameters jobParameters = getDefaultBdok100JobParameters();
		Files.delete(tmpOutFileFolder.resolveSibling("behandlet"));
		JobExecution jobExecution = launchStep(STEP_NAME, jobParameters, getJobExecutionContext());
		assertThat(jobExecution.getExitStatus().getExitCode(), is(ExitStatus.FAILED.getExitCode()));
		assertThat(jobExecution.getExitStatus().toString(), containsString("behandlet"));
	}

	@Test
	public void shouldFailCannotWriteFailed() throws Exception {
		JobParameters jobParameters = getDefaultBdok100JobParameters();
		Files.delete(tmpOutFileFolder.resolveSibling("failed"));
		JobExecution jobExecution = launchStep(STEP_NAME, jobParameters, getJobExecutionContext());
		assertThat(jobExecution.getExitStatus().getExitCode(), is(ExitStatus.FAILED.getExitCode()));
		assertThat(jobExecution.getExitStatus().toString(), containsString("failed"));
	}

	@Test
	public void shouldFailCannotWriteOutFolder() throws Exception {
		JobParameters jobParameters = getDefaultBdok100JobParameters();
		Files.delete(tmpOutFileFolder);
		JobExecution jobExecution = launchStep(STEP_NAME, jobParameters, getJobExecutionContext());
		assertThat(jobExecution.getExitStatus().getExitCode(), is(ExitStatus.FAILED.getExitCode()));
		assertThat(jobExecution.getExitStatus().toString(), containsString("output"));
	}
}
