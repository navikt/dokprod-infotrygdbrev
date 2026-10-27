package no.nav.dokprod.infotrygdbrev.bdok100.config;

import jakarta.persistence.EntityManagerFactory;
import no.nav.dokprod.infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod.infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaCursorItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.START_TIME_KEY;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.getAvviksrapport;

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
	@Bean(destroyMethod = "")
	public FlatFileItemWriter<Bdok100ArbTbl> avviksrapportWriter(
			Bdok100BatchParameters bdok100BatchParameters,
			@Value("#{jobExecutionContext[" + START_TIME_KEY + "]}") Date startTime) {

		FlatFileItemWriter<Bdok100ArbTbl> writer = new FlatFileItemWriter<>(
			new FileSystemResource(bdok100BatchParameters.outputFileLocation().resolve(getAvviksrapport(startTime)).toFile()),
			item -> "ArkivID = %s Brev feilet, %s".formatted(item.getIdnr(), item.getFeilstatus())
		);
		writer.setEncoding(Bdok100Constants.BDOK100_OUTPUT_CHARSET.toString());
		return writer;
	}

}
