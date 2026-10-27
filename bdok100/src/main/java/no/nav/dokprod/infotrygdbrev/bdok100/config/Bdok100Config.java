package no.nav.dokprod.infotrygdbrev.bdok100.config;

import com.google.common.collect.Lists;
import no.nav.brevogarkiv.batch.common.BatchStatusReportLoggerListener;
import no.nav.brevogarkiv.batch.common.CommonBatchEvents;
import no.nav.brevogarkiv.batch.common.LogContextListener;
import no.nav.brevogarkiv.batch.common.LoggingExceptionHandler;
import no.nav.brevogarkiv.batch.common.ProgressListener;
import no.nav.brevogarkiv.batch.common.ProgressLoggerListener;
import no.nav.brevogarkiv.batch.common.ProgressNotifier;
import no.nav.brevogarkiv.batch.common.SimpleBatchCounter;
import no.nav.brevogarkiv.batch.common.TableFormatter;
import no.nav.dokprod.infotrygdbrev.bdok100.Bdok100Events;
import no.nav.dokprod.infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod.infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod.infotrygdbrev.bdok100.repo.Bdok100Repo;
import no.nav.dokprod.infotrygdbrev.bdok100.support.Bdok100StatusChecker;
import no.nav.dokprod.infotrygdbrev.bdok100.support.Jpa2PingRepository;
import no.nav.dokprod.infotrygdbrev.common.ExitStatusJobExecutionListener;
import no.nav.dokprod.infotrygdbrev.common.UserIdMdcJobExecutionListener;
import no.nav.dokprod.infotrygdbrev.common.support.BeanValidator;
import no.nav.dokprod.infotrygdbrev.common.support.CleanableOnlyEmptyDecider;
import no.nav.dokprod.infotrygdbrev.common.support.JobCompletionFileHandler;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.CompositeJobExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaCursorItemReader;
import org.springframework.batch.infrastructure.item.database.JpaItemWriter;
import org.springframework.batch.infrastructure.repeat.exception.ExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.springframework.beans.factory.annotation.Qualifier;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import java.nio.file.Path;

import static java.util.Collections.singletonList;

/**
 * Spring configuration for BDOK100
 */
@Import({
		InputValidationStepConfiguration.class,
		LoadToArbTblStepConfiguration.class,
		MappingStepConfiguration.class,
		GsakStepConfiguration.class,
		XmlWriterStepConfiguration.class,
		ReadNAVKontorStepConfiguration.class,
		ReadVedleggsListStepConfiguration.class,
		KontrollrapportStepConfiguration.class,
		AvviksrapportStepConfiguration.class,
		AvviksfilerStepConfiguration.class,
		Bdok100BatchParameterExecutionContextPopulator.class,
		CleanArbeidsTabellStepConfiguration.class
})
@Configuration
public class Bdok100Config {

	private static final String JOB_NAME = "BDOK100";
	private static final String LOGNAME = "no.nav.dokprod.batch.bdok100";

	private static final String START = CleanableOnlyEmptyDecider.START.getName();
	private static final String CLEAN = CleanableOnlyEmptyDecider.CLEAN.getName();

	private static final Path WORK_DIRECTORY = Path.of(
			System.getProperty("java.io.tmpdir"),
			"dokprod-infotrygdbrev",
			"bdok100");

	public static final Path INPUT_FILE_LOCATION_VALUE = WORK_DIRECTORY.resolve("input");
	public static final Path STATIC_INPUT_FILE_LOCATION_VALUE = WORK_DIRECTORY.resolve("static");
	public static final Path OUTPUT_FILE_LOCATION_VALUE = WORK_DIRECTORY.resolve("output");
	public static final Path FAILED_FILE_LOCATION_VALUE = WORK_DIRECTORY.resolve("failed");
	public static final Path BEHANDLET_FILE_LOCATION_VALUE = WORK_DIRECTORY.resolve("behandlet");


	@Bean
	public Jpa2PingRepository pingRepository() {
		return new Jpa2PingRepository();
	}

	@Bean
	public Job bdok100Job(Bdok100BatchParameterExecutionContextPopulator bdok100BatchParameterExecutionContextPopulator,
						  LogContextListener logContextListener,
						  UserIdMdcJobExecutionListener userIdMdcJobExecutionListener,
						  CompositeJobExecutionListener bdok100BatchExitListener,
						  JobCompletionFileHandler jobCompletionFileHandler,
						  CleanableOnlyEmptyDecider cleanableOnlyEmptyDeciderBdok100,
						  JobRepository jobRepository,
						  Step inputValidationStep,
						  Step bulkLinjedataToArbTblStep,
						  Step bulkJournaldataToArbTblStep,
						  Step mapLinjedataStep,
						  Step mapJournaldataStep,
						  Step bulkReadNAVKontorFileToMapStep,
						  Step bulkReadVedleggsListeFileToMapStep,
						  Step callGsakStep,
						  Step xmlDokumentbestillingWriterStep,
						  Step createAvviksfilerStep,
						  Step createKontrollrapportStep,
						  Step createAvviksrapportStep,
						  Step cleanArbeidsTabellStep) {
		return new JobBuilder(JOB_NAME, jobRepository)
				.listener(logContextListener)
				.listener(userIdMdcJobExecutionListener)
				.listener(bdok100BatchExitListener)
				.listener(bdok100BatchParameterExecutionContextPopulator)
				.listener(bdok100BatchCounterLogger())
				.listener(jobCompletionFileHandler)

				.start(inputValidationStep)
				.next(cleanableOnlyEmptyDeciderBdok100)

				.from(cleanableOnlyEmptyDeciderBdok100)
				.on(START)
				.to(bulkLinjedataToArbTblStep)
				.next(bulkJournaldataToArbTblStep)
				.next(bulkReadNAVKontorFileToMapStep)
				.from(cleanableOnlyEmptyDeciderBdok100)
				.on(CLEAN)
				.to(cleanArbeidsTabellStep)

				.from(bulkReadNAVKontorFileToMapStep)
				.next(bulkReadVedleggsListeFileToMapStep)
				.next(mapLinjedataStep)
				.next(mapJournaldataStep)
				.next(callGsakStep)
				.next(xmlDokumentbestillingWriterStep)

				.next(createAvviksfilerStep)
				.next(createAvviksrapportStep)
				.next(createKontrollrapportStep)
				.next(cleanArbeidsTabellStep)
				.end()
				.build();
	}

	@Bean
	public CleanableOnlyEmptyDecider cleanableOnlyEmptyDeciderBdok100(
			Bdok100Repo bdok100Repo,
			@Qualifier("jobRepository") JobExplorer jobExplorer) {
		CleanableOnlyEmptyDecider decider = new CleanableOnlyEmptyDecider();
		decider.setRepository(bdok100Repo);
		decider.setJobExplorer(jobExplorer);
		return decider;
	}

	@Bean
	public Bdok100StatusChecker bdok100StatusChecker(Bdok100Repo bdok100Repo) {
		Bdok100StatusChecker bdok100StatusChecker = new Bdok100StatusChecker();
		bdok100StatusChecker.setRepo(bdok100Repo);
		return bdok100StatusChecker;
	}

	@Bean
	public ExitStatusJobExecutionListener bdok100ExitStatusJobExecutionListener(Bdok100StatusChecker bdok100StatusChecker) {
		ExitStatusJobExecutionListener listener = new ExitStatusJobExecutionListener();
		listener.setStatusChecker(bdok100StatusChecker);
		return listener;
	}

	@Bean
	public ItemWriter<Bdok100ArbTbl> bdok100ArbTblItemWriter(
			EntityManagerFactory entityManagerFactory
	) {
		return new JpaItemWriter<>(entityManagerFactory);
	}

	public static JpaCursorItemReader<Bdok100ArbTbl> createArbTblReaderForStatus(
		Bdok100Status status, EntityManagerFactory em) {
		JpaCursorItemReader<Bdok100ArbTbl> rd = createArbTblReaderWithoutQuery(em);
		rd.setQueryString("from Bdok100ArbTbl arbtbl where arbtbl.status = '" + status + "'");
		return rd;
	}

	public static JpaCursorItemReader<Bdok100ArbTbl> createArbTblReaderWithoutQuery(EntityManagerFactory em) {
		JpaCursorItemReader<Bdok100ArbTbl> rd = new JpaCursorItemReader<>(em);
		rd.setSaveState(false);
		return rd;
	}

	@Bean
	public CompositeJobExecutionListener bdok100BatchExitListener(
			BatchStatusReportLoggerListener bdok100BatchStatusReportLoggerListener,
			ExitStatusJobExecutionListener bdok100ExitStatusJobExecutionListener
	) {
		CompositeJobExecutionListener listener = new CompositeJobExecutionListener();
		// Important for ordering, last is run first
		listener.setListeners(Lists.newArrayList(bdok100BatchStatusReportLoggerListener, bdok100ExitStatusJobExecutionListener));
		return listener;
	}

	@Bean
	Bdok100BatchParameters bdok100BatchParameterProvider(DokprodInfotrygdbrevProperties dokprodInfotrygdbrevProperties) {
		return new Bdok100BatchParameters(INPUT_FILE_LOCATION_VALUE, STATIC_INPUT_FILE_LOCATION_VALUE, OUTPUT_FILE_LOCATION_VALUE, FAILED_FILE_LOCATION_VALUE, BEHANDLET_FILE_LOCATION_VALUE, 2, dokprodInfotrygdbrevProperties.maxFailures());
	}

	// // batchens parametre (fra confluence)                                          // fremover
	// startTime               // standard // tidspunkt start batch       // påkrevd   // LocalDateTime.now() e.l.
	// workUnit                // standard // chunksize for batch         // påkrevd   // fra config
	// progressInterval        // standard // hvor ofte logges progress   // optional  // droppes
	// inputFileLocation       // bdok100  // inputfiler                  // påkrevd   // statisk / fra sftp
	// staticInputFileLocation // bdok100  // statiske inputfiler         // påkrevd   // statisk / fra sftp
	// outputFileLocation      // bdok100  // rapporter output            // påkrevd   // statisk
	// maxFailures             // bdok100  // max feil før jobb avbrytes  // påkrevd   // fra config
	// failedFileLocation      // bdok100  // Metabatch BMF001 fail       // påkrevd   // statisk
	// behandletFileLocation   // bdok100  // Metabatch BMF001 success    // påkrevd   // statisk
	// skipClean               // bdok100  // ikke rydd etter kjøring     // optional  // fra config?
	// clean                   // bdok100  // rydd arbeidstab., ikke kjør // optional  // fra config?


	@Bean
	public ExceptionHandler bdok100ExceptionHandler() {
		return new LoggingExceptionHandler(LOGNAME);
	}

	@Bean
	public BatchStatusReportLoggerListener bdok100BatchStatusReportLoggerListener(DataSource dataSource) {
		return new BatchStatusReportLoggerListener(LOGNAME, dataSource);
	}

	@Bean
	public ProgressNotifier bdok100BatchCounterLogger() {
		ProgressNotifier counterLogger = new ProgressNotifier(bdok100BatchCounter());
		counterLogger.setProgressListeners(singletonList(bdok100ProgressListener()));
		return counterLogger;
	}

	@Bean
	public SimpleBatchCounter bdok100BatchCounter() {
		return new SimpleBatchCounter(CommonBatchEvents.class, Bdok100Events.class);
	}

	@Bean
	public ProgressListener bdok100ProgressListener() {
		return new ProgressLoggerListener(LOGNAME, new TableFormatter());
	}

	@Bean
	public BeanValidator beanValidator() {
		return new BeanValidator();
	}

}
