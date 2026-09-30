package no.nav.dokprod_infotrygdbrev.bdok100.support;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.BEHANDLET_COUNT;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.IS_WARNING_EXIT;
import static no.nav.dokprod_infotrygdbrev.common.BDOKCommonBatchInputParameters.CLEAN_KEY;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokprod_infotrygdbrev.common.StatusChecker;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Checks status for batchrun
 *
 */
@Slf4j
public class Bdok100StatusChecker implements StatusChecker {

	private JpaRepository repo;

	@Override
	public boolean isWarning(JobExecution jobExecution) {
		boolean warningDuringRun = (boolean) jobExecution.getExecutionContext().get(IS_WARNING_EXIT);
		boolean behandletNone = jobExecution.getExecutionContext().getLong(BEHANDLET_COUNT) == 0L;
		boolean executionWasCleanOnly = BooleanUtils.isTrue((Boolean) jobExecution.getExecutionContext().get(CLEAN_KEY));

		boolean behandletNoneWarning = !executionWasCleanOnly && behandletNone;
		if (behandletNoneWarning && !warningDuringRun) {
			log.warn("BDOK100 avsluttet med WARNING siden ingen rader ble funnet i input, Journaldata/ Linjeprintfil er tom");
		}

		return warningDuringRun || behandletNoneWarning;
	}

	@Override
	public boolean isError(JobExecution jobExecution) {
		return repo.count() > 0;
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
		jobExecution.getExecutionContext().put(IS_WARNING_EXIT, false);
	}

	public void setRepo(JpaRepository repo) {
		this.repo = repo;
	}
}
