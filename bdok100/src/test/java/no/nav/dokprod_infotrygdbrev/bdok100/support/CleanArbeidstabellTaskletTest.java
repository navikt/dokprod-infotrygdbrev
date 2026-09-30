package no.nav.dokprod_infotrygdbrev.bdok100.support;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.VedleggsLister;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Unit test for {@link CleanArbeidstabellTasklet}
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CleanArbeidstabellTaskletTest {

	@Mock
	private JdbcTemplate template;
	@Mock
	private VedleggsLister vedleggsLister;
	@Mock
	private NAVKontors navKontors;
	@Mock
	private Bdok100Repo bdok100Repo;

	@InjectMocks
	private CleanArbeidstabellTasklet tasklet;

	private JobExecution jobExecution;
	private ChunkContext chunkContext;

	@Before
	public void setUp() throws Exception {
		tasklet.setSkipClean(false);
		tasklet.setClean(false);
		jobExecution = new JobExecution(1L, new JobInstance(1L, "test"), new JobParameters());
		chunkContext = new ChunkContext(new StepContext(new StepExecution("step", jobExecution)));
	}

	@Test
	public void shouldDeleteAllExceptFailed() throws Exception {
		assertThat(tasklet.execute(null, chunkContext), is(RepeatStatus.FINISHED));
		verify(template).execute(eq("DELETE FROM ARBTB_BDOK100 WHERE STATUS <> 'FEILET_GSAK'"));

		verify(vedleggsLister).clear();
		verify(navKontors).clear();
	}

	@Test
	public void shouldDeleteAllIfClean() throws Exception {
		tasklet.setClean(true);
		assertThat(tasklet.execute(null, chunkContext), is(RepeatStatus.FINISHED));
		verify(template).execute(eq("DELETE FROM ARBTB_BDOK100"));

		verify(vedleggsLister).clear();
		verify(navKontors).clear();
	}

	@Test
	public void shouldSkipClean() throws Exception {
		tasklet.setSkipClean(true);
		assertThat(tasklet.execute(null, chunkContext), is(RepeatStatus.FINISHED));
		verifyNoInteractions(template);
	}

	@Test
	public void shouldNotSetWarnIfOk() throws Exception {
		when(bdok100Repo.countByStatus(Bdok100Status.KAN_IKKE_BEHANDLES)).thenReturn(0L);
		tasklet.execute(null, chunkContext);
		assertThat(((Boolean) jobExecution.getExecutionContext().get(Bdok100Constants.IS_WARNING_EXIT)), is(false));
	}

	@Test
	public void shouldSetWarnIfAvvikRows() throws Exception {
		when(bdok100Repo.countByStatus(Bdok100Status.KAN_IKKE_BEHANDLES)).thenReturn(1L);
		tasklet.execute(null, chunkContext);
		assertThat(((Boolean) jobExecution.getExecutionContext().get(Bdok100Constants.IS_WARNING_EXIT)), is(true));
	}

	@Test
	public void shouldCountBehandlet() throws Exception {
		when(bdok100Repo.countByStatus(Bdok100Status.BEHANDLET)).thenReturn(4L);
		tasklet.execute(null, chunkContext);
		assertThat((jobExecution.getExecutionContext().getLong(Bdok100Constants.BEHANDLET_COUNT)), is(4L));
	}
}