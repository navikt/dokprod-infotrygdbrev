package no.nav.dokprod_infotrygdbrev.bdok100.support;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.IS_WARNING_EXIT;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status.BEHANDLET;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status.FEILET_GSAK;

import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.VedleggsLister;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Tasklet that deletes all rows in BDOK100 Arbeidstabell
 *
 */
public class CleanArbeidstabellTasklet implements Tasklet {

	private Boolean skipClean;
	private JdbcTemplate template;

	private NAVKontors navKontors;
	private VedleggsLister vedleggsLister;
	private Boolean clean;
	private Bdok100Repo bdok100Repo;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution()
				.getJobExecution().getExecutionContext();
		registerWarningStatus(executionContext);
		registerBehandletCount(executionContext);
		if (!skipClean) {
			if (clean) {
				template.execute("DELETE FROM ARBTB_BDOK100");
			} else {
				template.execute("DELETE FROM ARBTB_BDOK100 WHERE STATUS <> '" + FEILET_GSAK + "'");
			}
		}
		navKontors.clear();
		vedleggsLister.clear();
		return RepeatStatus.FINISHED;
	}

	private void registerBehandletCount(ExecutionContext executionContext) {
		long behandletCount = bdok100Repo.countByStatus(BEHANDLET);
		executionContext.putLong(Bdok100Constants.BEHANDLET_COUNT, behandletCount);
	}

	private void registerWarningStatus(ExecutionContext executionContext) {
		long kanIkkeBehandles = bdok100Repo.countByStatus(Bdok100Status.KAN_IKKE_BEHANDLES);

		executionContext.put(IS_WARNING_EXIT, false);

		if (kanIkkeBehandles > 0) {
			executionContext.put(IS_WARNING_EXIT, true);
		}
	}

	public void setSkipClean(Boolean skipClean) {
		this.skipClean = skipClean;
	}

	public void setTemplate(JdbcTemplate template) {
		this.template = template;
	}

	public void setNavKontors(NAVKontors navKontors) {
		this.navKontors = navKontors;
	}

	public void setVedleggsLister(VedleggsLister vedleggsLister) {
		this.vedleggsLister = vedleggsLister;
	}

	public void setClean(Boolean clean) {
		this.clean = clean;
	}

	public void setBdok100Repo(Bdok100Repo bdok100Repo) {
		this.bdok100Repo = bdok100Repo;
	}

}
