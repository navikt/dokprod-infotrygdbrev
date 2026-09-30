package no.nav.dokprod_infotrygdbrev.bdok100.config.config.bdok100;

import no.nav.brevogarkiv.batch.common.ExecutionContextWorkUnitCompletionPolicy;
import no.nav.brevogarkiv.batch.common.LogContextListener;
import no.nav.brevogarkiv.batch.common.ProgressNotifier;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.NAVKontor;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.validation.NAVKontorValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.NAVKontorMapper;
import no.nav.dokprod_infotrygdbrev.bdok100.support.processors.NAVKontorMapperProcessor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.infrastructure.repeat.exception.ExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.beans.factory.annotation.Autowired;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.CURRENT_NAVKONTOR;


/**
 * Step for Reading NavKontor file
 *
 */
@Configuration
public class ReadNAVKontorStepConfiguration {

	@Autowired
	private ExecutionContextWorkUnitCompletionPolicy workUnitCompletionPolicy;
	@Autowired
	private LogContextListener logContextListener;
	@Autowired
	private ExceptionHandler bdok100ExceptionHandler;
	@Autowired
	private ProgressNotifier bdok100BatchCounterLogger;

	@Bean
	public NAVKontorMapperProcessor bulkNAVKontorMapperProcessor() {
		return new NAVKontorMapperProcessor();
	}

	@Bean
	public Step bulkReadNAVKontorFileToMapStep(
		FlatFileItemReader<String> nAVKontorBulkReader,
		JobRepository jobRepository,
		PlatformTransactionManager platformTransactionManager) {
		return new StepBuilder("bulkReadNAVKontorFileToMapStep", jobRepository)
				.<String, NAVKontor>chunk(workUnitCompletionPolicy, platformTransactionManager)
				.reader(nAVKontorBulkReader)
				.processor(bulkNAVKontorMapperProcessor())
				.writer(list -> {
					//noop
				})
				.exceptionHandler(bdok100ExceptionHandler)
				.listener(logContextListener)
				.listener(bdok100BatchCounterLogger)
				.listener(workUnitCompletionPolicy)
				.allowStartIfComplete(true)
				.build();
	}

	@Bean
	@StepScope
	public FlatFileItemReader<String> nAVKontorBulkReader(
			@Value("file:#{jobExecutionContext[" + CURRENT_NAVKONTOR + "]}") Resource currentNAVKontor
	) {
		FlatFileItemReader<String> reader = new FlatFileItemReader<>(currentNAVKontor, new PassThroughLineMapper());
		reader.setEncoding(Bdok100Constants.BDOK100_INPUT_CHARSET.toString());
		return reader;
	}

	@Bean
	public NAVKontorMapper nAVKontorMapper() {
		return new NAVKontorMapper();
	}

	@Bean
	public NAVKontorValidator nAVKontorValidator() {
		return new NAVKontorValidator();
	}

}
