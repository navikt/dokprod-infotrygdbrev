package no.nav.dokprod_infotrygdbrev.common;


import org.springframework.batch.core.job.JobExecution;

public interface StatusChecker {

	boolean isWarning(JobExecution jobExecution);

	boolean isError(JobExecution jobExecution);

	void beforeJob(JobExecution jobExecution);
}
