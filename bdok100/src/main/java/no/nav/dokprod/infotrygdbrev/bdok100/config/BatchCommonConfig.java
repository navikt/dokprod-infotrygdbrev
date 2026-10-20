package no.nav.dokprod.infotrygdbrev.bdok100.config;

import no.nav.brevogarkiv.batch.common.ExecutionContextWorkUnitCompletionPolicy;
import no.nav.brevogarkiv.batch.common.LogContextListener;
import no.nav.brevogarkiv.batch.common.MaxFailuresChunkListener;
import no.nav.dokprod.infotrygdbrev.bdok100.config.bdok100.Bdok100BatchParameters;
import no.nav.dokprod.infotrygdbrev.bdok100.repo.Bdok100Repo;
import no.nav.dokprod.infotrygdbrev.bdok100.support.AsyncSakConsumer;
import no.nav.dokprod.infotrygdbrev.common.ExitStatusJobExecutionListener;
import no.nav.dokprod.infotrygdbrev.common.UserIdMdcJobExecutionListener;
import no.nav.dokprod.infotrygdbrev.common.support.InputDirectoryValidationTasklet;
import no.nav.dokprod.infotrygdbrev.common.support.JobCompletionFileHandler;
import no.nav.dokprod.infotrygdbrev.consumer.SakService;
import no.nav.dokprod.infotrygdbrev.serializer.ConverterCapableXStreamExecutionContextStringSerializer;
import no.nav.dokprod.infotrygdbrev.serializer.LocalDateConverter;
import no.nav.dokprod.infotrygdbrev.serializer.LocalDateTimeConverter;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


@Configuration
@Import({ConsumerConfig.class, Bdok100Config.class})
@EnableAsync
@EnableBatchProcessing
@EnableJpaRepositories(basePackageClasses = {Bdok100Repo.class})
public class BatchCommonConfig implements AsyncConfigurer {

	// @Value("${default.threadpool.corepoolsize}")
	// private int corePoolSize;
	// @Value("${default.threadpool.maxpoolsize}")
	// private int maxPoolSize;
	// @Value("${default.threadpool.queuecapacity}")
	// private int queueCapacity;

	// @Value("${gsak.threadpool.corepoolsize}")
	// private int corePoolSizeGsak;
	// @Value("${gsak.threadpool.maxpoolsize}")
	// private int maxPoolSizeGsak;
	// @Value("${gsak.threadpool.queuecapacity}")
	// private int queueCapacityGsak;

	@Bean
	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		// threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
		// threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
		// threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
		threadPoolTaskExecutor.setThreadNamePrefix("DefaultBatchThreadPool-");
		threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		return threadPoolTaskExecutor;
	}

	@Bean
	public TaskExecutor gsakAsyncExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		// threadPoolTaskExecutor.setCorePoolSize(corePoolSizeGsak);
		// threadPoolTaskExecutor.setMaxPoolSize(maxPoolSizeGsak);
		// threadPoolTaskExecutor.setQueueCapacity(queueCapacityGsak);
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
	@StepScope
	public InputDirectoryValidationTasklet inputDirectoryValidationTasklet(Bdok100BatchParameters bdok100BatchParameters) {
		return new InputDirectoryValidationTasklet(new FileSystemResource(bdok100BatchParameters.inputFileLocation()));
	}

	@Bean
	public MaxFailuresChunkListener maxFailuresChunkListener(Bdok100BatchParameters bdok100BatchParameters) {
		return new MaxFailuresChunkListener(bdok100BatchParameters.maxFailures());
	}

	@Bean
	public JobCompletionFileHandler jobCompletionFileHandler(Bdok100BatchParameters bdok100BatchParameters) {
		return new JobCompletionFileHandler(bdok100BatchParameters);
	}
}
