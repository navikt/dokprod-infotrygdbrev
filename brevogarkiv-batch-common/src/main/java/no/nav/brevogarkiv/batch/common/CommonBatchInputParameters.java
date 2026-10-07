package no.nav.brevogarkiv.batch.common;

/**
 * Common input parameter keys for all NAV batches.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public final class CommonBatchInputParameters {
	public static final String START_TIME_KEY = "startTime";

	public static final String WORK_UNIT_KEY = "workUnit";
	
	public static final String PROGRESS_INTERVAL_KEY = "progressInterval";

	public static final String INPUT_FILE_LOCATION_KEY = "inputFileLocation";

	public static final String OUTPUT_FILE_LOCATION_KEY = "outputFileLocation";

	public static final String FAILED_FILE_LOCATION_KEY = "failedFileLocation";

	public static final String WORKSPACE_FILE_LOCATION_KEY = "inWorkFileLocation";

	public static final String BEHANDLET_FILE_LOCATION_KEY = "behandletFileLocation";

	public static final String TRANSACTION_TIMEOUT_KEY = "transactionTimeoutSecs";

	public static final String MAX_FAILURES = "maxFailures";

	public static final String MODE_KEY = "modus";

	private CommonBatchInputParameters() {
	}
}
