package no.nav.brevogarkiv.batch.common;

import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.support.DatabaseType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.util.Assert;

/**
 * Copied from Stelvio and adapted to Oracle.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class BatchStatusReportLoggerListener implements JobExecutionListener, InitializingBean {

	@SuppressWarnings("serial")
	private final Map<String, String> dbTypeToSql = new HashMap<String, String>() {
		{
			put(DatabaseType.ORACLE.name(), "select sys_context('userenv', 'current_schema') from dual");
			put(DatabaseType.HSQL.name(), "select value from information_schema.system_sessioninfo where key = 'CURRENT SCHEMA'");
		}
	};

	private Logger logger;
	private JdbcTemplate jdbcTemplate;
	private DataSource dataSource;
	private List<String> sqlStatistics;

	/**
	 * Constructs a new BatchStatusReportLoggerListener.
	 *
	 */
	public BatchStatusReportLoggerListener(String logname, DataSource dataSource) {
		this.logger = LoggerFactory.getLogger(logname);
		this.dataSource = dataSource;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/** {@inheritDoc} */
	@Override
	public void afterJob(JobExecution jobExecution) {

		logger.info(formatJobExecution(jobExecution) + formatStepExecution(jobExecution.getStepExecutions()));

		if (sqlStatistics != null) {
			logger.info(formatSqlStatistics());
			sqlStatistics.clear();
		}
	}

	/**
	 * Format the list of SQL statistics
	 *
	 * @return The formated String
	 */
	private String formatSqlStatistics() {

		StringBuilder sb = new StringBuilder();
		sb.append("\n\n\n");
		sb.append("=================================================================================================================\n");
		sb.append("=============================================== SQL Statistics Summary ==========================================\n");
		sb.append("+===============================================================================================================+\n");
		String format = "|%1$-100s%2$-11s|\n";
		sb.append(String.format(format, "Step description", "#rows"));
		sb.append("+---------------------------------------------------------------------------------------------------------------+\n");

		for (String stepStatistics : sqlStatistics) {
			sb.append(stepStatistics).append("\n");
		}

		sb.append("+===============================================================================================================+\n");

		return sb.toString();
	}

	/**
	 * Format JobExecution summary
	 *
	 * @param jobExecution
	 *            the jobExecution
	 * @return The formated String
	 */
	protected String formatJobExecution(JobExecution jobExecution) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("=======================================================================================================================\n");
		sb.append("=============================================== Spring Job Execution Summary ==========================================\n");
		sb.append("=======================================================================================================================\n");
		sb.append("Job name: ").append(jobExecution.getJobInstance().getJobName()).append("\n");
		sb.append("Job parameters:").append(jobExecution.getJobParameters().parameters().toString());
		sb.append("\nTotal execution time: ").append(
				formatMillisecondsDurationAsHumanReadableString(Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime())));
		sb.append("\nHost: ").append(getHostNameAndIp());
		sb.append("\nDB Schema: ").append(getCurrentSchema() + "\n");
		sb.append("\n");
		sb.append("\n");
		sb.append("+====================================================================== Spring Job Execution Summary ===============================================================================================+\n");
		sb.append("|Name                                    |ExitStatus |Exit Description                                                 |Time             |Start Time                  |End Time                     |\n");
		sb.append("+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+\n");

		String format = "|%1$-40s|%2$-11s|%3$-65s|%4$17s|%5$28s|%6$29s|\n";

		sb.append(String.format(format, jobExecution.getJobInstance().getJobName(), jobExecution.getExitStatus().getExitCode(),
				jobExecution.getExitStatus().getExitDescription(),
				formatMillisecondsDurationAsHumanReadableString(Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime())),
				jobExecution.getStartTime(), jobExecution.getEndTime()));

		sb.append("+===================================================================================================================================================================================================+\n");

		return sb.toString();
	}

	/**
	 * Format step execution summary
	 *
	 * @param stepExecutions
	 *            the step executions
	 * @return the formated String
	 */
	protected String formatStepExecution(Collection<StepExecution> stepExecutions) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("+========================================================================= Step Execution Summary =================================================================================================+\n");
		sb.append("|Name                                    |ExitStatus |Time             |Read Count |Filter Count |Write Count |Read Skip Count |Write Skip Count |Process Skip Count |Commit Count |Rollback Count |\n");
		sb.append("+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+\n");
		for (StepExecution stepExecution : stepExecutions) {
			String format = "|%1$-40s|%2$-11s|%3$17s|%4$11s|%5$13s|%6$12s|%7$16s|%8$17s|%9$19s|%10$13s|%11$15s|\n";
			sb.append(String.format(format, stepExecution.getStepName(), stepExecution.getExitStatus().getExitCode(),
					formatMillisecondsDurationAsHumanReadableString(Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime())),
					stepExecution.getReadCount(), stepExecution.getFilterCount(),
					stepExecution.getWriteCount(), stepExecution.getReadSkipCount(), stepExecution.getWriteSkipCount(),
					stepExecution.getProcessSkipCount(), stepExecution.getCommitCount(), stepExecution.getRollbackCount()));
		}

		sb.append("+==================================================================================================================================================================================================+\n");

		return sb.toString();
	}

	/** {@inheritDoc} */
	@Override
	public void beforeJob(JobExecution jobExecution) {

		String jobName = jobExecution.getJobInstance().getJobName();
		long jobId = jobExecution.getId();

		String format = "==================================== Executing batch: %1$-7s(ID:%2$6s) ==============================================\n";

		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("=======================================================================================================================\n");
		sb.append(String.format(format, jobName, jobId));
		sb.append("=======================================================================================================================\n");
		sb.append("Start time: " + jobExecution.getStartTime() + "\n");
		sb.append("Job parameters:").append(jobExecution.getJobParameters().parameters().toString() + "\n");
		sb.append("Host: ").append(getHostNameAndIp() + "\n");
		sb.append("DB Schema: ").append(getCurrentSchema() + "\n");

		sb.append("=======================================================================================================================\n");

		logger.info(sb.toString());

		if (sqlStatistics != null) {
			sqlStatistics.clear();
		}

	}

	/**
	 * Get host name and IP.
	 *
	 * @return String representing HostName and IP
	 */
	private String getHostNameAndIp() {
		String myIp = "UNKNOWN";
		try {
			String hostName = null;
			hostName = InetAddress.getLocalHost().getHostName();

			InetAddress[] addrs = null;
			addrs = InetAddress.getAllByName(hostName);

			for (InetAddress addr : addrs) {
				if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
					myIp = addr.getHostName() + "/" + addr.getHostAddress();
				}
			}
		} catch (Exception e) {
			myIp += e.getMessage();
			logger.info("Exception caught when deciding IP-address and hostname to display in batch summary. "
					+ e.toString());
		}
		return myIp;

	}

	private String getCurrentSchema() {
		String currentSchema = "UNKNOWN";
		String currentSchemaSql = null;
		try {
			String dataBaseName = DatabaseType.fromMetaData(dataSource).name();
			currentSchemaSql = dbTypeToSql.get(dataBaseName);
		} catch (MetaDataAccessException e) {
		}

		if (currentSchemaSql != null) {
			currentSchema = (String) jdbcTemplate.queryForObject(currentSchemaSql, String.class);
		}

		return currentSchema;
	}

	/**
	 * Format a duration given in milliseconds as hours, minutes, seconds and milliseconds.
	 *
	 * @param duration
	 *            the duration in milliseconds
	 * @return a formatted string representing the duration in human readable format
	 */
	public static String formatMillisecondsDurationAsHumanReadableString(Duration duration) {
		long durationMillis = duration.toMillis();

		int seconds = (int) (durationMillis / 1000) % 60;
		int minutes = (int) ((durationMillis / 1000) % 3600) / 60;
		int hours = (int) (durationMillis / 1000) / 3600;
		int milliseconds = (int) durationMillis % 1000;

		return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
	}

	/**
	 * @param sqlStatistics
	 *            the sqlStatistics to set
	 */
	public void setSqlStatistics(List<String> sqlStatistics) {
		this.sqlStatistics = sqlStatistics;
	}

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(logger, "BatchStatusReportLoggerListener requires a InfoLogger");
		Assert.notNull(jdbcTemplate, "BatchStatusReportLoggerListener requires a JdbcTemplate");
	}

}
