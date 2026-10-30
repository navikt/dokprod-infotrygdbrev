package no.nav.dokprod.infotrygdbrev.bdok100.support;

import static no.nav.dokprod.infotrygdbrev.bdok100.Bdok100Constants.BEHANDLET_COUNT;
import static no.nav.dokprod.infotrygdbrev.bdok100.Bdok100Constants.IS_WARNING_EXIT;
import static no.nav.dokprod.infotrygdbrev.common.BDOKCommonBatchInputParameters.CLEAN_KEY;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokprod.infotrygdbrev.common.StatusChecker;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.data.jpa.repository.JpaRepository;

@Slf4j
public class Bdok100StatusChecker implements StatusChecker {

	private JpaRepository repo;

	@Override
	public boolean isWarning(JobExecution jobExecution) {
		boolean warningDuringRun = (boolean) jobExecution.getExecutionContext().get(IS_WARNING_EXIT);
		boolean behandletNone = jobExecution.getExecutionContext().getLong(BEHANDLET_COUNT) == 0L;
		JobParameter<Boolean> cleanKeyParameter = (JobParameter<Boolean>) jobExecution.getJobParameters().getParameter(CLEAN_KEY);
		boolean executionWasCleanOnly = cleanKeyParameter != null && cleanKeyParameter.value();

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
