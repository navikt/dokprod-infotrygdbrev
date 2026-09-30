package no.nav.dokprod_infotrygdbrev.bdok100.config.config.bdok100;

import com.google.common.collect.Lists;
import jakarta.persistence.EntityManagerFactory;
import no.nav.brevogarkiv.batch.common.BatchCounter;
import no.nav.brevogarkiv.batch.common.MaxFailuresChunkListener;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.support.AsyncSakConsumer;
import no.nav.dokprod_infotrygdbrev.bdok100.support.SakProcessor;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.Bdok100GsakMapper;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaCursorItemReader;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.WORK_UNIT_KEY;
import static no.nav.dokprod_infotrygdbrev.bdok100.config.config.Bdok100Config.createArbTblReaderWithoutQuery;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status.FEILET_GSAK;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status.MAPPING_JFF;

/**
 * Spring config for Bdok100 step that calls GSAK
 *
 */
@Configuration
public class GsakStepConfiguration extends AbstractBdok100StepConfig {

	@Bean
	public Step callGsakStep(
			ItemReader<Bdok100ArbTbl> gsakArbtblReader,
			CompositeItemWriter<Bdok100ArbTbl> gsakProcessorWriter,
			MaxFailuresChunkListener maxFailuresChunkListener,
			JobRepository jobRepository,
			PlatformTransactionManager platformTransactionManager,
			SakProcessor sakProcessor
	) {
		return new StepBuilder("callGsakStep", jobRepository)
				.<Bdok100ArbTbl, Bdok100ArbTbl>chunk(workUnitCompletionPolicy, platformTransactionManager)
				.reader(gsakArbtblReader)
				.writer(gsakProcessorWriter)
				.exceptionHandler(bdok100ExceptionHandler)
				.listener(maxFailuresChunkListener)
				.listener(sakProcessor)
				.listener(logContextListener)
				.listener(bdok100BatchCounterLogger)
				.listener(workUnitCompletionPolicy)
				.allowStartIfComplete(true)
				.build();
	}

	@Bean(destroyMethod = "")
	@StepScope
	public JpaCursorItemReader<Bdok100ArbTbl> gsakArbtblReader(
			EntityManagerFactory entityManager,
			@Value("#{jobParameters[" + WORK_UNIT_KEY + "]}") Integer workUnit) {
		JpaCursorItemReader<Bdok100ArbTbl> reader = createArbTblReaderWithoutQuery(entityManager);
		reader.setQueryString("from Bdok100ArbTbl arbtbl where arbtbl.status in ('" + FEILET_GSAK + "','" + MAPPING_JFF + "')");
		return reader;
	}

	@Bean
	@SuppressWarnings("unchecked")
	@StepScope
	public CompositeItemWriter<Bdok100ArbTbl> gsakProcessorWriter(
			SakProcessor sakProcessor,
			ItemWriter<Bdok100ArbTbl> bdok100ArbTblItemWriter
	) {
		CompositeItemWriter<Bdok100ArbTbl> writer = new CompositeItemWriter<>();
		writer.setDelegates(Lists.newArrayList(sakProcessor, bdok100ArbTblItemWriter));
		return writer;
	}

	@Bean
	public SakProcessor gsakProcessor(AsyncSakConsumer gsakConsumer,
									  Bdok100GsakMapper bdok100GsakMapper,
									  BatchCounter bdok100BatchCounter) {
		// implemented as writer
		SakProcessor sakProcessor = new SakProcessor();
		sakProcessor.setSakConsumer(gsakConsumer);
		sakProcessor.setBdok100GsakMapper(bdok100GsakMapper);
		sakProcessor.setBdok100BatchCounter(bdok100BatchCounter);
		return sakProcessor;
	}

	@Bean
	public Bdok100GsakMapper bdok100GsakMapper() {
		return new Bdok100GsakMapper();
	}

}
