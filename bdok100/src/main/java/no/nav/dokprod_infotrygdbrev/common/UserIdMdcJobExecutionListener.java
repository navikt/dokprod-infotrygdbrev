package no.nav.dokprod_infotrygdbrev.common;

import no.nav.dokprod_infotrygdbrev.util.MDCOperations;
import org.slf4j.MDC;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;

/**
 * Job listener that puts the jobName in MDC as userId.
 * UserId is used for both logging and changestamp.
 *
 */
public class UserIdMdcJobExecutionListener implements JobExecutionListener {

	@Override
	public void beforeJob(JobExecution jobExecution) {
		String jobName = jobExecution.getJobInstance().getJobName();
		MDC.put(MDCOperations.MDC_USER_ID, jobName != null ? jobName.toLowerCase() : null);
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		MDC.remove(MDCOperations.MDC_USER_ID);
	}

}
