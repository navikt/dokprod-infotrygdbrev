package no.nav.dokprod.infotrygdbrev.bdok100.config;

import jakarta.persistence.EntityManagerFactory;
import no.nav.dokprod.infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod.infotrygdbrev.bdok100.support.DatabaseOutputService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.database.JpaCursorItemReader;
import org.springframework.batch.infrastructure.item.ItemStreamWriter;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

import static no.nav.dokprod.infotrygdbrev.bdok100.config.Bdok100Config.createArbTblReaderForStatus;
import static no.nav.dokprod.infotrygdbrev.bdok100.domain.Bdok100Status.KAN_IKKE_BEHANDLES;
import static no.nav.dokprod.infotrygdbrev.bdok100.support.DatabaseOutputService.AvvikType.JOURNALFORINGSFIL;
import static no.nav.dokprod.infotrygdbrev.bdok100.support.DatabaseOutputService.AvvikType.LINJEPRINTFIL;

/**
 * StepConfiguration for the BDOK100 step that creates avviksfiler
 *
 */
@Configuration
public class AvviksfilerStepConfiguration extends AbstractBdok100StepConfig {

	@Bean
	public Step createAvviksfilerStep(
			ItemReader<Bdok100ArbTbl> avviksfilerReader,
			JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
			CompositeItemWriter<Bdok100ArbTbl> avviksfilerCompositeWriter) {
		return new StepBuilder("createAvviksfilerStep", jobRepository)
				.<Bdok100ArbTbl, Bdok100ArbTbl>chunk(workUnitCompletionPolicy, platformTransactionManager)
				.reader(avviksfilerReader)
				.writer(avviksfilerCompositeWriter)
				.exceptionHandler(bdok100ExceptionHandler)
				.listener(logContextListener)
				.listener(bdok100BatchCounterLogger)
				.listener(workUnitCompletionPolicy)
				.build();
	}

	@StepScope
	@Bean(destroyMethod = "")
	public JpaCursorItemReader<Bdok100ArbTbl> avviksfilerReader(
			EntityManagerFactory entityManager) {
		return createArbTblReaderForStatus(KAN_IKKE_BEHANDLES, entityManager);
	}

	@Bean
	@SuppressWarnings("unchecked")
	@StepScope
	public CompositeItemWriter<Bdok100ArbTbl> avviksfilerCompositeWriter(
			ItemStreamWriter<Bdok100ArbTbl> lineprintAvviksfilWriter,
			ItemStreamWriter<Bdok100ArbTbl> journalforingsfilAvviksfilWriter) {
		CompositeItemWriter<Bdok100ArbTbl> writer = new CompositeItemWriter<>();
		writer.setDelegates((List) Arrays.asList(lineprintAvviksfilWriter, journalforingsfilAvviksfilWriter));
		return writer;
	}

	@StepScope
	@Bean
	public ItemStreamWriter<Bdok100ArbTbl> lineprintAvviksfilWriter(
			DatabaseOutputService databaseOutputService,
			@Value("#{stepExecution.jobExecution.jobInstance.id}") Long jobInstanceId) {
		return databaseOutputService.writer(jobInstanceId, LINJEPRINTFIL, Bdok100ArbTbl::getLineprintBrev);
	}

	@StepScope
	@Bean
	public ItemStreamWriter<Bdok100ArbTbl> journalforingsfilAvviksfilWriter(
			DatabaseOutputService databaseOutputService,
			@Value("#{stepExecution.jobExecution.jobInstance.id}") Long jobInstanceId) {
		return databaseOutputService.writer(jobInstanceId, JOURNALFORINGSFIL, Bdok100ArbTbl::getJournalforingsfil);
	}
}
