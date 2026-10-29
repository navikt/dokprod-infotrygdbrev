package no.nav.dokprod.infotrygdbrev.bdok100.config;

import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.util.Date;

import static java.util.Objects.requireNonNull;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.BEHANDLET_FILE_LOCATION_KEY;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.FAILED_FILE_LOCATION_KEY;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.INPUT_FILE_LOCATION_KEY;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.MAX_FAILURES;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.OUTPUT_FILE_LOCATION_KEY;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.START_TIME_KEY;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.WORK_UNIT_KEY;
import static no.nav.dokprod.infotrygdbrev.bdok100.Bdok100Constants.STATIC_INPUT_FILE_LOCATION_KEY;

@Component
public class Bdok100BatchParameterExecutionContextPopulator implements JobExecutionListener {

	private final Bdok100BatchParameters bdok100BatchParameters;

	public Bdok100BatchParameterExecutionContextPopulator(Bdok100BatchParameters bdok100BatchParameters) {
		this.bdok100BatchParameters = bdok100BatchParameters;
	}

	@Override
	public void beforeJob(final JobExecution jobExecution) {
		putParametersOnContext(
				jobExecution.getExecutionContext(),
				requireNonNull(
						jobExecution.getJobParameters().getDate(START_TIME_KEY),
						"Missing required job parameter: " + START_TIME_KEY));
	}

	private void putParametersOnContext(ExecutionContext jobExecutionContext, Date startTime) {
		jobExecutionContext.put(INPUT_FILE_LOCATION_KEY, bdok100BatchParameters.inputFileLocation());
		jobExecutionContext.put(STATIC_INPUT_FILE_LOCATION_KEY, bdok100BatchParameters.staticInputFileLocation());
		jobExecutionContext.put(OUTPUT_FILE_LOCATION_KEY, bdok100BatchParameters.outputFileLocation());
		jobExecutionContext.put(BEHANDLET_FILE_LOCATION_KEY, bdok100BatchParameters.behandletFileLocation());
		jobExecutionContext.put(FAILED_FILE_LOCATION_KEY, bdok100BatchParameters.failedFileLocation());
		jobExecutionContext.putLong(MAX_FAILURES, bdok100BatchParameters.maxFailures());
		jobExecutionContext.putLong(WORK_UNIT_KEY, bdok100BatchParameters.workUnit());
		jobExecutionContext.put(START_TIME_KEY, startTime);
	}
}
