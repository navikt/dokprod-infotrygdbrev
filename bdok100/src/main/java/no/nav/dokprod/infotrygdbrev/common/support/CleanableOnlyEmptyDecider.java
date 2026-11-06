package no.nav.dokprod.infotrygdbrev.common.support;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokprod.infotrygdbrev.common.BDOKCommonBatchInputParameters;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CleanableOnlyEmptyDecider
 *
 */
@Slf4j
public class CleanableOnlyEmptyDecider implements JobExecutionDecider {

	public static final FlowExecutionStatus START = new FlowExecutionStatus("START");
	public static final FlowExecutionStatus CLEAN = new FlowExecutionStatus("CLEAN");

	private JpaRepository repository;
	private JobExplorer jobExplorer;

	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		JobParameter<Boolean> cleanKeyParameter = (JobParameter<Boolean>) jobExecution.getJobParameters().getParameter(BDOKCommonBatchInputParameters.CLEAN_KEY);
		boolean cleanOnly = cleanKeyParameter != null && cleanKeyParameter.value();

		if (cleanOnly) {
			return CLEAN;
		}

		if (!isRestart(jobExecution) && repository.count() > 0) {
			log.error("New batch was started, but work data already contains data from a previous run. Run once with clean first if intentional");
			return FlowExecutionStatus.FAILED;
		}

		return START;
	}

	private boolean isRestart(JobExecution jobExecution) {
		return jobExplorer.getJobExecutions(jobExecution.getJobInstance()).size() > 1;
	}


	public void setRepository(JpaRepository repository) {
		this.repository = repository;
	}

	public void setJobExplorer(JobExplorer jobExplorer) {
		this.jobExplorer = jobExplorer;
	}
}
