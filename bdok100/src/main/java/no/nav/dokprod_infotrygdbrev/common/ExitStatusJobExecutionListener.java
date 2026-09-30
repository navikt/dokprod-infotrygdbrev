package no.nav.dokprod_infotrygdbrev.common;

import static no.nav.brevogarkiv.batch.common.MaxFailuresChunkListener.NO_OF_FAILURES_KEY;

import no.nav.dokprod_infotrygdbrev.util.NavExitStatus;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;

/**
 * Listener that sets the exit status for a job
 *
 */
public class ExitStatusJobExecutionListener implements JobExecutionListener {

	private StatusChecker statusChecker;

	@Override
	public void beforeJob(JobExecution jobExecution) {
		if (statusChecker != null) {
			statusChecker.beforeJob(jobExecution);
		}
		jobExecution.getExecutionContext().putLong(NO_OF_FAILURES_KEY, 0);
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
			checkForErrors(jobExecution);

			if (statusChecker != null) {
				if (statusChecker.isWarning(jobExecution)) {
					jobExecution.setExitStatus(NavExitStatus.WARNING);
				}
				if (statusChecker.isError(jobExecution)) {
					jobExecution.setExitStatus(NavExitStatus.ERROR);
					jobExecution.setStatus(BatchStatus.FAILED);
				}
			}
		}
	}

	/**
	 * Checks for exceptions thrown by steps in the job
	 *
	 * @param jobExecution The JobExecution
	 */
	protected void checkForErrors(JobExecution jobExecution) {
		if (!jobExecution.getAllFailureExceptions().isEmpty()) {
			jobExecution.setExitStatus(NavExitStatus.WARNING);
		}

		for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
			if (!stepExecution.getFailureExceptions().isEmpty()) {
				jobExecution.setExitStatus(NavExitStatus.WARNING);
				break;
			}
		}
	}

	public void setStatusChecker(StatusChecker statusChecker) {
		this.statusChecker = statusChecker;
	}
}
