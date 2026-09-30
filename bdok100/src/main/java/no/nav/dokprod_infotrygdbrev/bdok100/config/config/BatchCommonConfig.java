package no.nav.dokprod_infotrygdbrev.bdok100.config.config;

import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.brevogarkiv.batch.common.ExecutionContextWorkUnitCompletionPolicy;
import no.nav.brevogarkiv.batch.common.LogContextListener;
import no.nav.brevogarkiv.batch.common.MaxFailuresChunkListener;
import no.nav.brevogarkiv.batch.common.validator.CommonJobParametersValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.config.ConsumerConfig;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import no.nav.dokprod_infotrygdbrev.bdok100.support.AsyncSakConsumer;
import no.nav.dokprod_infotrygdbrev.common.ExitStatusJobExecutionListener;
import no.nav.dokprod_infotrygdbrev.common.UserIdMdcJobExecutionListener;
import no.nav.dokprod_infotrygdbrev.common.support.InputDirectoryValidationTasklet;
import no.nav.dokprod_infotrygdbrev.common.support.JobCompletionFileHandler;
import no.nav.dokprod_infotrygdbrev.consumer.SakService;
import no.nav.dokprod_infotrygdbrev.consumer.sts.StsRestConsumer;
import no.nav.dokprod_infotrygdbrev.serializer.ConverterCapableXStreamExecutionContextStringSerializer;
import no.nav.dokprod_infotrygdbrev.serializer.LocalDateConverter;
import no.nav.dokprod_infotrygdbrev.serializer.LocalDateTimeConverter;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.INPUT_FILE_LOCATION_KEY;
import static no.nav.dokprod_infotrygdbrev.common.BDOKCommonBatchInputParameters.CLEAN_KEY;
import static no.nav.dokprod_infotrygdbrev.common.BDOKCommonBatchInputParameters.SKIP_CLEAN_KEY;

/**
 * Common Spring configuration for all BDOK batches
 */
@Configuration
@Import({ConsumerConfig.class, StsRestConsumer.class, Bdok100Config.class})
@EnableAsync
@EnableBatchProcessing
@EnableJpaRepositories(basePackageClasses = {Bdok100Repo.class})
public class BatchCommonConfig implements AsyncConfigurer {

	@Value("${default.threadpool.corepoolsize}")
	private int corePoolSize;
	@Value("${default.threadpool.maxpoolsize}")
	private int maxPoolSize;
	@Value("${default.threadpool.queuecapacity}")
	private int queueCapacity;

	@Value("${gsak.threadpool.corepoolsize}")
	private int corePoolSizeGsak;
	@Value("${gsak.threadpool.maxpoolsize}")
	private int maxPoolSizeGsak;
	@Value("${gsak.threadpool.queuecapacity}")
	private int queueCapacityGsak;

	@Bean
	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
		threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
		threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
		threadPoolTaskExecutor.setThreadNamePrefix("DefaultBatchThreadPool-");
		threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		return threadPoolTaskExecutor;
	}

	@Bean
	public TaskExecutor gsakAsyncExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(corePoolSizeGsak);
		threadPoolTaskExecutor.setMaxPoolSize(maxPoolSizeGsak);
		threadPoolTaskExecutor.setQueueCapacity(queueCapacityGsak);
		threadPoolTaskExecutor.setThreadNamePrefix("GsakThreadPool-");
		threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		return threadPoolTaskExecutor;
	}

	@Bean
	public JobRegistry jobRegistry() {
		return new MapJobRegistry();
	}

	@Bean
	public AsyncSakConsumer asyncSakConsumer(SakService sakService) {
		AsyncSakConsumer asyncSakConsumer = new AsyncSakConsumer();
		asyncSakConsumer.setRetries(1);
		asyncSakConsumer.setSakService(sakService);
		return asyncSakConsumer;
	}

	@Bean
	public JobRepository jobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource(dataSource);
		factory.setTransactionManager(transactionManager);
		factory.setIsolationLevelForCreate("ISOLATION_DEFAULT");
		factory.setSerializer(jodaTimeCapableXstreamContextSerializer());
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	@Bean
	public ConverterCapableXStreamExecutionContextStringSerializer jodaTimeCapableXstreamContextSerializer() {
		ConverterCapableXStreamExecutionContextStringSerializer serializer = new ConverterCapableXStreamExecutionContextStringSerializer();
		serializer.addConverter(new LocalDateTimeConverter());
		serializer.addConverter(new LocalDateConverter());
		return serializer;
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public ChunkListener<Object, Object> maxFailureChunkListener() {
		return new MaxFailuresChunkListener();
	}

	@Bean
	public TransactionTemplate transactionTemplate(PlatformTransactionManager platformTransactionManager) {
		return new TransactionTemplate(platformTransactionManager);
	}

	@Bean
	public JobParametersConverter jobParametersConverter() {
		return new DefaultJobParametersConverter();
	}

	@Bean
	public JobOperator jobOperator(JobRegistry jobRegistry,
								   JobRepository jobRepository) {
		SimpleJobOperator simpleJobOperator = new SimpleJobOperator();
		simpleJobOperator.setJobRegistry(jobRegistry);
		simpleJobOperator.setJobRepository(jobRepository);
		return simpleJobOperator;
	}

	@Bean
	public ExecutionContextWorkUnitCompletionPolicy workUnitCompletionPolicy() {
		return new ExecutionContextWorkUnitCompletionPolicy();
	}

	@Bean
	public LogContextListener logContextStepExecutionListener() {
		return new LogContextListener();
	}

	@Bean
	public UserIdMdcJobExecutionListener userIdMdcJobExecutionListener() {
		return new UserIdMdcJobExecutionListener();
	}

	@Bean
	public ExitStatusJobExecutionListener exitStatusJobExecutionListener() {
		return new ExitStatusJobExecutionListener();
	}

	@Bean
	public CommonJobParametersValidator.StringLongJobParameter workUnitParameter() {
		return new CommonJobParametersValidator
				.StringLongJobParameter(CommonBatchInputParameters.WORK_UNIT_KEY);
	}

	@Bean
	public CommonJobParametersValidator.StringUriJobParameter inputFileLocationParameter() {
		return new CommonJobParametersValidator
				.StringUriJobParameter(INPUT_FILE_LOCATION_KEY);
	}

	@Bean
	public CommonJobParametersValidator.StringUriJobParameter outputFileLocationParameter() {
		return new CommonJobParametersValidator
				.StringUriJobParameter(CommonBatchInputParameters.OUTPUT_FILE_LOCATION_KEY);
	}

	@Bean
	public CommonJobParametersValidator.StringUriJobParameter failedFileLocationParameter() {
		return new CommonJobParametersValidator
				.StringUriJobParameter(CommonBatchInputParameters.FAILED_FILE_LOCATION_KEY);
	}

	@Bean
	public CommonJobParametersValidator.StringUriJobParameter inWorkFileLocationParameter() {
		return new CommonJobParametersValidator
				.StringUriJobParameter(CommonBatchInputParameters.WORKSPACE_FILE_LOCATION_KEY);
	}

	@Bean
	public CommonJobParametersValidator.StringUriJobParameter behandletFileLocationParameter() {
		return new CommonJobParametersValidator
				.StringUriJobParameter(CommonBatchInputParameters.BEHANDLET_FILE_LOCATION_KEY);
	}

	@Bean
	public CommonJobParametersValidator.StringLongJobParameter progressIntervalParameter() {
		return new CommonJobParametersValidator
				.StringLongJobParameter(CommonBatchInputParameters.PROGRESS_INTERVAL_KEY);
	}

	@Bean
	public CommonJobParametersValidator.StringLongJobParameter maxFailuresParameter() {
		return new CommonJobParametersValidator
				.StringLongJobParameter(CommonBatchInputParameters.MAX_FAILURES);
	}

	@Bean
	public CommonJobParametersValidator.StringBooleanJobParameter skipClean() {
		return new CommonJobParametersValidator.StringBooleanJobParameter(SKIP_CLEAN_KEY);
	}

	@Bean
	public CommonJobParametersValidator.StringBooleanJobParameter clean() {
		return new CommonJobParametersValidator.StringBooleanJobParameter(CLEAN_KEY);
	}


	@Bean
	@StepScope
	public InputDirectoryValidationTasklet inputDirectoryValidationTasklet(
			@Value("file:#{jobParameters[" + INPUT_FILE_LOCATION_KEY + "]}") Resource inputFileLocation) {
		InputDirectoryValidationTasklet inputDirectoryValidationTasklet = new InputDirectoryValidationTasklet();
		inputDirectoryValidationTasklet.setInputFileLocation(inputFileLocation);
		return inputDirectoryValidationTasklet;
	}

	@Bean
	public CommonJobParametersValidator commonJobParametersValidator(
			CommonJobParametersValidator.StringDateJobParameter startTimeParameter) {
		CommonJobParametersValidator jobParametersValidator = new CommonJobParametersValidator();
		jobParametersValidator.setRequiredParameters(
				Arrays.asList(
						workUnitParameter(),
						startTimeParameter
				)
		);
		jobParametersValidator.setOptionalParameters(Collections.singletonList(progressIntervalParameter()));

		return jobParametersValidator;
	}

	@Bean
	public MaxFailuresChunkListener maxFailuresChunkListener() {
		return new MaxFailuresChunkListener();
	}

	@Bean
	public JobCompletionFileHandler jobCompletionFileHandler() {
		return new JobCompletionFileHandler();
	}
}
