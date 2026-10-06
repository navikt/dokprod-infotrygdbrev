package no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100;

import no.nav.brevogarkiv.batch.common.BatchCounter;
import no.nav.brevogarkiv.batch.common.ExecutionContextWorkUnitCompletionPolicy;
import no.nav.brevogarkiv.batch.common.LogContextListener;
import no.nav.brevogarkiv.batch.common.ProgressNotifier;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.support.processors.BulkJournaldataProcessor;
import no.nav.dokprod_infotrygdbrev.bdok100.support.processors.BulkLinjedataProcessor;
import no.nav.dokprod_infotrygdbrev.bdok100.support.readers.MultiLineItemReader;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.UpdateArbTbl;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.batch.infrastructure.item.support.SingleItemPeekableItemReader;
import org.springframework.batch.infrastructure.repeat.exception.ExceptionHandler;
import org.springframework.batch.infrastructure.support.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.CURRENT_JOURNALDATA;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.CURRENT_LINJEDATA;

/**
 * Loading of batchdata Linjedata and Journaldata steps configuration
 *
 */
@Configuration
public class LoadToArbTblStepConfiguration {

	@Autowired
	private ExecutionContextWorkUnitCompletionPolicy workUnitCompletionPolicy;
	@Autowired
	private LogContextListener logContextListener;
	@Autowired
	private ExceptionHandler bdok100ExceptionHandler;
	@Autowired
	private ProgressNotifier bdok100BatchCounterLogger;

	@Bean
	public Step bulkLinjedataToArbTblStep(
			MultiLineItemReader linjedataFileReader,
			ItemProcessor<String, Bdok100ArbTbl> bulkLinjedataProcessor,
			ItemWriter<Bdok100ArbTbl> bdok100ArbTblItemWriter,
			JobRepository jobRepository,
			PlatformTransactionManager platformTransactionManager

	) {
		return new StepBuilder("bulkLinjedataToArbTblStep", jobRepository)
				.<String, Bdok100ArbTbl>chunk(workUnitCompletionPolicy, platformTransactionManager)
				.reader(linjedataFileReader)
				.processor(bulkLinjedataProcessor)
				.writer(bdok100ArbTblItemWriter)
				.exceptionHandler(bdok100ExceptionHandler)
				.listener(logContextListener)
				.listener(bdok100BatchCounterLogger)
				.listener(workUnitCompletionPolicy)
				.build();
	}

	@Bean
	public Step bulkJournaldataToArbTblStep(
			FlatFileItemReader<String> bulkJournaldataReader,
			ItemProcessor<String, UpdateArbTbl> bulkJournaldataProcessor,
			CompositeItemWriter<UpdateArbTbl> bulkJournaldataWriter,
			JobRepository jobRepository,
			PlatformTransactionManager platformTransactionManager
	) throws Exception {
		return new StepBuilder("bulkJournaldataToArbTblStep", jobRepository)
				.<String, UpdateArbTbl>chunk(workUnitCompletionPolicy, platformTransactionManager)
				.reader(bulkJournaldataReader)
				.processor(bulkJournaldataProcessor)
				.writer(bulkJournaldataWriter)
				.exceptionHandler(bdok100ExceptionHandler)
				.listener(logContextListener)
				.listener(bdok100BatchCounterLogger)
				.listener(workUnitCompletionPolicy)
				.build();
	}

	@Bean
	@StepScope
	public MultiLineItemReader linjedataFileReader(
			@Value("file:#{jobExecutionContext[" + CURRENT_LINJEDATA + "]}") final Resource currentLinjedata
	) {
		return new MultiLineItemReader() {{
			setDelegate(readerPeek(currentLinjedata));
			setNewRecordPrefix("1");
		}};
	}

	private SingleItemPeekableItemReader<String> readerPeek(final Resource currentLinjedata) {
		return new SingleItemPeekableItemReader<>(createFlatFileReader(currentLinjedata)) {};
	}

	private FlatFileItemReader<String> createFlatFileReader(Resource currentLinjedata) {
		FlatFileItemReader<String> reader = new FlatFileItemReader<>(currentLinjedata, new PassThroughLineMapper());
		reader.setEncoding(Bdok100Constants.BDOK100_INPUT_CHARSET.toString());
		return reader;
	}

	@Bean
	public BulkLinjedataProcessor bulkLinjedataProcessor(BatchCounter bdok100BatchCounter) {
		BulkLinjedataProcessor processor = new BulkLinjedataProcessor();
		processor.setBatchCounter(bdok100BatchCounter);
		return processor;
	}

	@Bean
	@StepScope
	public FlatFileItemReader<String> bulkJournaldataReader(
			@Value("file:#{jobExecutionContext[" + CURRENT_JOURNALDATA + "]}") Resource currentJournaldata
	) {
		return createFlatFileReader(currentJournaldata);
	}

	@Bean
	public BulkJournaldataProcessor bulkJournaldataProcessor(BatchCounter bdok100BatchCounter) {
		BulkJournaldataProcessor processor = new BulkJournaldataProcessor();
		processor.setBatchCounter(bdok100BatchCounter);
		return processor;
	}

	@Bean
	@SuppressWarnings("unchecked")
	public CompositeItemWriter<UpdateArbTbl> bulkJournaldataWriter(DataSource dataSource) throws Exception {
		CompositeItemWriter compositeWriter = new CompositeItemWriter<>();

		List<JdbcBatchItemWriter<UpdateArbTbl>> itemWriters = new ArrayList<>();
		itemWriters.add(arbTblJournaldataUpdater(dataSource));
		itemWriters.add(arbTblStatusUpdater(dataSource));

		compositeWriter.setDelegates(itemWriters);
		return compositeWriter;
	}

	@Bean
	public JdbcBatchItemWriter<UpdateArbTbl> arbTblJournaldataUpdater(DataSource dataSource) throws Exception {
		JdbcBatchItemWriter<UpdateArbTbl> writer = new JdbcBatchItemWriter<>();
		writer.setDataSource(dataSource);
		String using;
		if (DatabaseType.fromMetaData(dataSource) == DatabaseType.HSQL) {
			using = "USING (values(1)) as dual ";
		} else {
			using = "USING dual ";
		}
		writer.setSql("MERGE INTO ARBTB_BDOK100 a "
				+ using
				+ "ON (a.id_nr = :onDemandId) "
				+ "WHEN MATCHED THEN "
				+ "UPDATE SET "
				+ "journalforingsfil = :journalforingsfil "
				+ "WHEN NOT MATCHED THEN "
				+ "INSERT (id_nr, journalforingsfil, feilstatus) "
				+ "VALUES (:onDemandId, :journalforingsfil, :feilStatus)");
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<UpdateArbTbl>());
		return writer;
	}

	@Bean
	public JdbcBatchItemWriter<UpdateArbTbl> arbTblStatusUpdater(DataSource dataSource) {
		JdbcBatchItemWriter<UpdateArbTbl> writer = new JdbcBatchItemWriter<>();
		writer.setDataSource(dataSource);
		writer.setAssertUpdates(false);
		writer.setSql("UPDATE ARBTB_BDOK100 SET "
				+ "status = :status "
				+ "WHERE id_nr = :onDemandId AND (status = :updateStatus OR status IS NULL)");
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<UpdateArbTbl>());
		return writer;
	}
}
