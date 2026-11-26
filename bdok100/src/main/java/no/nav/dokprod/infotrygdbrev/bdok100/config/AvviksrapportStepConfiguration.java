package no.nav.dokprod.infotrygdbrev.bdok100.config;

import jakarta.persistence.EntityManagerFactory;
import no.nav.dokprod.infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod.infotrygdbrev.bdok100.support.DatabaseOutputService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaCursorItemReader;
import org.springframework.batch.infrastructure.item.ItemStreamWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import static no.nav.dokprod.infotrygdbrev.bdok100.support.DatabaseOutputService.AvvikType.REPORT;

@Configuration
public class AvviksrapportStepConfiguration extends AbstractBdok100StepConfig {

	@Bean
	public Step createAvviksrapportStep(
		ItemReader<Bdok100ArbTbl> avviksrapportReader,
		ItemWriter<Bdok100ArbTbl> avviksrapportWriter,
		JobRepository jobRepository,
		PlatformTransactionManager platformTransactionManager) {
		return new StepBuilder("createAvviksrapportStep", jobRepository)
				.<Bdok100ArbTbl, Bdok100ArbTbl>chunk(workUnitCompletionPolicy, platformTransactionManager)
				.reader(avviksrapportReader)
				.writer(avviksrapportWriter)
				.exceptionHandler(bdok100ExceptionHandler)
				.listener(logContextListener)
				.listener(bdok100BatchCounterLogger)
				.listener(workUnitCompletionPolicy)
				.build();
	}

	@StepScope
	@Bean(destroyMethod = "")
	public JpaCursorItemReader<Bdok100ArbTbl> avviksrapportReader(EntityManagerFactory entityManager) {
		JpaCursorItemReader<Bdok100ArbTbl> reader = Bdok100Config.createArbTblReaderWithoutQuery(entityManager);
		reader.setQueryString("from Bdok100ArbTbl arbtbl where arbtbl.feilstatus is not null and status = 'KAN_IKKE_BEHANDLES'");
		return reader;
	}

	@StepScope
	@Bean
	public ItemStreamWriter<Bdok100ArbTbl> avviksrapportWriter(
			DatabaseOutputService databaseOutputService,
			@Value("#{stepExecution.jobExecution.jobInstance.id}") Long jobInstanceId) {
		return databaseOutputService.writer(jobInstanceId, REPORT,
			item -> "ArkivID = %s Brev feilet, %s".formatted(item.getIdnr(), item.getFeilstatus()));
	}

}
