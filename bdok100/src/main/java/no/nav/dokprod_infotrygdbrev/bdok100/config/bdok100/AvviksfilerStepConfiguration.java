package no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100;

import com.google.common.collect.Lists;
import jakarta.persistence.EntityManagerFactory;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.database.JpaCursorItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.transform.LineAggregator;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.OUTPUT_FILE_LOCATION_KEY;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.START_TIME_KEY;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.WORK_UNIT_KEY;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.CRLF;
import static no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper.getJFFAvvikFilename;
import static no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper.getLPFAvvikFilename;
import static no.nav.dokprod_infotrygdbrev.bdok100.config.Bdok100Config.createArbTblReaderForStatus;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status.KAN_IKKE_BEHANDLES;

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
			EntityManagerFactory entityManager,
			@Value("#{jobParameters[" + WORK_UNIT_KEY + "]}") Integer workUnit) {
		return createArbTblReaderForStatus(KAN_IKKE_BEHANDLES, workUnit, entityManager);
	}

	@Bean
	@SuppressWarnings("unchecked")
	@StepScope
	public CompositeItemWriter<Bdok100ArbTbl> avviksfilerCompositeWriter(
			FlatFileItemWriter<Bdok100ArbTbl> lineprintAvviksfilWriter,
			FlatFileItemWriter<Bdok100ArbTbl> journalforingsfilAvviksfilWriter) {
		CompositeItemWriter<Bdok100ArbTbl> writer = new CompositeItemWriter<>();
		writer.setDelegates((List) Arrays.asList(lineprintAvviksfilWriter, journalforingsfilAvviksfilWriter));
		return writer;
	}

	@StepScope
	@Bean(destroyMethod = "")
	public FlatFileItemWriter<Bdok100ArbTbl> lineprintAvviksfilWriter(
			@Value("file:#{jobParameters[" + OUTPUT_FILE_LOCATION_KEY + "]}") Resource outputLocation,
			@Value("#{jobExecutionContext[" + START_TIME_KEY + "]}") Date startTime) throws IOException {
		FlatFileItemWriter<Bdok100ArbTbl> writer = new RemoveNullFlatFileItemWriter<>(new LineAggregator<Bdok100ArbTbl>() {
			@Override
			public String aggregate(Bdok100ArbTbl item) {
				return item.getLineprintBrev();
			}
		});
		writer.setResource(new FileSystemResource(outputLocation.getFile().toPath().resolve(getLPFAvvikFilename(startTime)).toFile()));
		writer.setLineSeparator(CRLF);
		writer.setEncoding(Bdok100Constants.BDOK100_INPUT_CHARSET.toString());
		return writer;
	}

	@StepScope
	@Bean(destroyMethod = "")
	public FlatFileItemWriter<Bdok100ArbTbl> journalforingsfilAvviksfilWriter(
			@Value("file:#{jobParameters[" + OUTPUT_FILE_LOCATION_KEY + "]}") Resource outputLocation,
			@Value("#{jobExecutionContext[" + START_TIME_KEY + "]}") Date startTime) throws IOException {

		FlatFileItemWriter<Bdok100ArbTbl> writer = new RemoveNullFlatFileItemWriter<>(new LineAggregator<Bdok100ArbTbl>() {
			@Override
			public String aggregate(Bdok100ArbTbl item) {
				return item.getJournalforingsfil();
			}
		});
		writer.setResource(new FileSystemResource(outputLocation.getFile().toPath().resolve(getJFFAvvikFilename(startTime)).toFile()));
		writer.setLineSeparator(CRLF);
		writer.setEncoding(Bdok100Constants.BDOK100_INPUT_CHARSET.toString());
		return writer;
	}

	private static class RemoveNullFlatFileItemWriter<T> extends FlatFileItemWriter<T> {
		private final LineAggregator<T> aggregator;

		RemoveNullFlatFileItemWriter(LineAggregator<T> aggregator) {
			super(aggregator);
			// setLineAggregator (aggregator);
			this.aggregator = aggregator;
		}

		@Override
		public void write(Chunk<? extends T> items) throws Exception {
			ArrayList<T> items1 = Lists.newArrayList();
			for (T item : items) {
				if (aggregator.aggregate(item) != null) {
					items1.add(item);
				}
			}
			super.write(new Chunk<T>(items1));
		}
	}
}
