package no.nav.dokprod.infotrygdbrev.bdok100.config.bdok100;

import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.util.Date;

import static no.nav.dokprod.infotrygdbrev.bdok100.Bdok100Constants.STATIC_INPUT_FILE_LOCATION_KEY;

@Component
public class Bdok100BatchParameterExecutionContextPopulator implements JobExecutionListener {

	private final Bdok100BatchParameters bdok100BatchParameters;

	public Bdok100BatchParameterExecutionContextPopulator(Bdok100BatchParameters bdok100BatchParameters) {
		this.bdok100BatchParameters = bdok100BatchParameters;
	}

	@Override
	public void beforeJob(final JobExecution jobExecution) {
		putParametersOnContext(jobExecution.getExecutionContext(), new Date());
	}

	private void putParametersOnContext(ExecutionContext jobExecutionContext, Date startTime) {
		jobExecutionContext.put(CommonBatchInputParameters.INPUT_FILE_LOCATION_KEY, bdok100BatchParameters.inputFileLocation());
		jobExecutionContext.put(STATIC_INPUT_FILE_LOCATION_KEY, bdok100BatchParameters.staticInputFileLocation());
		jobExecutionContext.put(CommonBatchInputParameters.OUTPUT_FILE_LOCATION_KEY, bdok100BatchParameters.outputFileLocation());
		jobExecutionContext.put(CommonBatchInputParameters.BEHANDLET_FILE_LOCATION_KEY, bdok100BatchParameters.behandletFileLocation());
		jobExecutionContext.put(CommonBatchInputParameters.FAILED_FILE_LOCATION_KEY, bdok100BatchParameters.failedFileLocation());
		jobExecutionContext.putLong(CommonBatchInputParameters.MAX_FAILURES, bdok100BatchParameters.maxFailures());
		jobExecutionContext.putLong(CommonBatchInputParameters.WORK_UNIT_KEY, bdok100BatchParameters.workUnit());
		jobExecutionContext.put(CommonBatchInputParameters.START_TIME_KEY, startTime);
	}
}
