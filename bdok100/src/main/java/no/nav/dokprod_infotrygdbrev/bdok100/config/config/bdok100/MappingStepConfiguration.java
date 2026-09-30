package no.nav.dokprod_infotrygdbrev.bdok100.config.config.bdok100;

import jakarta.persistence.EntityManagerFactory;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.validation.JournaldataValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.validation.LinjedataValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.JournaldataMapper;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.LinjedataMapper;
import no.nav.dokprod_infotrygdbrev.bdok100.support.processors.JournaldataMapAndValidateProcessor;
import no.nav.dokprod_infotrygdbrev.bdok100.support.processors.LinjedataMapperAndValidationProcessor;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.transaction.PlatformTransactionManager;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.WORK_UNIT_KEY;
import static no.nav.dokprod_infotrygdbrev.bdok100.config.config.Bdok100Config.createArbTblReaderWithoutQuery;

/**
 * Step configuration for mapping of Linjedata and Journaldata
 *
 */
@Configuration
public class MappingStepConfiguration extends AbstractBdok100StepConfig {

	@Bean
	public Step mapLinjedataStep(
			ItemReader<Bdok100ArbTbl> bdok100ArbtblReader,
			ItemProcessor<Bdok100ArbTbl, Bdok100ArbTbl> linjedataMapperProcessor,
			ItemWriter<Bdok100ArbTbl> bdok100ArbTblItemWriter,
			JobRepository jobRepository, PlatformTransactionManager platformTransactionManager
	) {
		return new StepBuilder("mapLinjedataStep", jobRepository)
				.<Bdok100ArbTbl, Bdok100ArbTbl>chunk(workUnitCompletionPolicy, platformTransactionManager)
				.reader(bdok100ArbtblReader)
				.processor(linjedataMapperProcessor)
				.writer(bdok100ArbTblItemWriter)
				.exceptionHandler(bdok100ExceptionHandler)
				.listener(logContextListener)
				.listener(bdok100BatchCounterLogger)
				.listener(workUnitCompletionPolicy)
				.build();
	}

	@Bean
	public Step mapJournaldataStep(
			ItemReader<Bdok100ArbTbl> bdok100ArbtblReader,
			ItemProcessor<Bdok100ArbTbl, Bdok100ArbTbl> journaldataMapperProcessor,
			ItemWriter<Bdok100ArbTbl> bdok100ArbTblItemWriter,
			JobRepository jobRepository, PlatformTransactionManager platformTransactionManager
	) {
		return new StepBuilder("mapJournaldataStep", jobRepository)
				.<Bdok100ArbTbl, Bdok100ArbTbl>chunk(workUnitCompletionPolicy, platformTransactionManager)
				.reader(bdok100ArbtblReader)
				.processor(journaldataMapperProcessor)
				.writer(bdok100ArbTblItemWriter)
				.exceptionHandler(bdok100ExceptionHandler)
				.listener(logContextListener)
				.listener(bdok100BatchCounterLogger)
				.listener(workUnitCompletionPolicy)
				.build();
	}

	@Bean
	public ItemProcessor<Bdok100ArbTbl, Bdok100ArbTbl> linjedataMapperProcessor() {
		return new LinjedataMapperAndValidationProcessor();
	}

	@Bean
	public LinjedataMapper linjedataMapper() {
		return new LinjedataMapper();
	}

	@Bean
	public LinjedataValidator linjedataValidator(NAVKontors navKontors) {
		LinjedataValidator validator = new LinjedataValidator();
		validator.setNavKontors(navKontors);
		return validator;
	}

	@Bean
	@StepScope
	public JpaCursorItemReader<Bdok100ArbTbl> bdok100ArbtblReader(
			EntityManagerFactory entityManager,
			@Value("#{jobParameters[" + WORK_UNIT_KEY + "]}") Integer workUnit) {
		JpaCursorItemReader<Bdok100ArbTbl> rd = createArbTblReaderWithoutQuery(entityManager);
		rd.setQueryString("from Bdok100ArbTbl arbtbl where arbtbl.status not in ('" + Bdok100Status.KAN_IKKE_BEHANDLES + "','" + Bdok100Status.TIL_RAPPORT + "')");
		return rd;
	}

	@Bean
	public ItemProcessor<Bdok100ArbTbl, Bdok100ArbTbl> journaldataMapperProcessor() {
		return new JournaldataMapAndValidateProcessor();
	}

	@Bean
	public JournaldataMapper journaldataMapper() {
		return new JournaldataMapper();
	}

	@Bean
	public JournaldataValidator journaldataValidator() {
		return new JournaldataValidator();
	}
}
