package no.nav.brevogarkiv.batch.common;

import org.slf4j.MDC;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;

/**
 * Job and Step execution listener that adds job and step name to the slf4j log context.
 * This context can be used in log configuration to obtain job and step name.
 * 
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class LogContextListener implements JobExecutionListener, StepExecutionListener {

	public static final String STEP_NAME_KEY = "stepName";
	public static final String JOB_NAME = "jobName";
	public static final String JOB_ID = "jobId";

	/** {@inheritDoc} */
	@Override
	public void beforeStep(StepExecution stepExecution) {
		MDC.put(STEP_NAME_KEY, stepExecution.getStepName());
	}
	
	/** {@inheritDoc} */
	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		MDC.remove(STEP_NAME_KEY);
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void beforeJob(JobExecution jobExecution) {
		String jobName = jobExecution.getJobInstance().getJobName();
		MDC.put(JOB_NAME, jobName != null ? jobName.toLowerCase() : null);
		MDC.put(JOB_ID, Long.toString(jobExecution.getId()));
	}

	/** {@inheritDoc} */
	@Override
	public void afterJob(JobExecution jobExecution) {
	}
	
}
