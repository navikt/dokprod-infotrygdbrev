package no.nav.dokprod_infotrygdbrev.bdok100.config.config.bdok100;

import no.nav.brevogarkiv.batch.common.ExecutionContextWorkUnitCompletionPolicy;
import no.nav.brevogarkiv.batch.common.LogContextListener;
import no.nav.brevogarkiv.batch.common.ProgressNotifier;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.validation.VedleggsListeValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.VedleggsListeMapper;
import no.nav.dokprod_infotrygdbrev.bdok100.support.processors.VedleggsListeMapperProcessor;
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

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.BDOK100_INPUT_CHARSET;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.CURRENT_VEDLEGG;


/**
 * Step for reading Vedleggsliste
 *
 */
@Configuration
public class ReadVedleggsListStepConfiguration {

	@Autowired
	private ExecutionContextWorkUnitCompletionPolicy workUnitCompletionPolicy;
	@Autowired
	private LogContextListener logContextListener;
	@Autowired
	private ExceptionHandler bdok100ExceptionHandler;
	@Autowired
	private ProgressNotifier bdok100BatchCounterLogger;

	@Bean
	public VedleggsListeMapperProcessor bulkVedleggsListeMapperProcessor() {
		return new VedleggsListeMapperProcessor();
	}

	@Bean
	public Step bulkReadVedleggsListeFileToMapStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
		return new StepBuilder("bulkReadVedleggsListeFileToMapStep", jobRepository)
				.<String, VedleggsListe>chunk(workUnitCompletionPolicy, platformTransactionManager)
				.reader(vedleggsListeBulkReader(null))
				.processor(bulkVedleggsListeMapperProcessor())
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
	public FlatFileItemReader<String> vedleggsListeBulkReader(
			@Value("file:#{jobExecutionContext[" + CURRENT_VEDLEGG + "]}") Resource currentVedleggsListe
	) {
		FlatFileItemReader<String> reader = new FlatFileItemReader<>(currentVedleggsListe, new PassThroughLineMapper());
		reader.setEncoding(BDOK100_INPUT_CHARSET.toString());
		return reader;
	}

	@Bean
	public VedleggsListeMapper vedleggsListeMapper() {
		return new VedleggsListeMapper();
	}

	@Bean
	public VedleggsListeValidator vedleggsListeValidator() {
		return new VedleggsListeValidator();
	}

}
