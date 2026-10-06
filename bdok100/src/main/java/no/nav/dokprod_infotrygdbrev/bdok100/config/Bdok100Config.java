package no.nav.dokprod_infotrygdbrev.bdok100.config;

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
import no.nav.brevogarkiv.batch.common.validator.CommonJobParametersValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Events;
import no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100.AvviksfilerStepConfiguration;
import no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100.AvviksrapportStepConfiguration;
import no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100.CleanArbeidsTabellStepConfiguration;
import no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100.GsakStepConfiguration;
import no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100.InputValidationStepConfiguration;
import no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100.KontrollrapportStepConfiguration;
import no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100.LoadToArbTblStepConfiguration;
import no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100.MappingStepConfiguration;
import no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100.ReadNAVKontorStepConfiguration;
import no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100.ReadVedleggsListStepConfiguration;
import no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100.XmlWriterStepConfiguration;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100StatusChecker;
import no.nav.dokprod_infotrygdbrev.bdok100.support.Jpa2PingRepository;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.VedleggsLister;
import no.nav.dokprod_infotrygdbrev.common.ExitStatusJobExecutionListener;
import no.nav.dokprod_infotrygdbrev.common.UserIdMdcJobExecutionListener;
import no.nav.dokprod_infotrygdbrev.common.support.BeanValidator;
import no.nav.dokprod_infotrygdbrev.common.support.CleanableOnlyEmptyDecider;
import no.nav.dokprod_infotrygdbrev.common.support.JobCompletionFileHandler;
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
import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Collections.singletonList;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.STATIC_INPUT_FILE_LOCATION_KEY;

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
		CleanArbeidsTabellStepConfiguration.class
})
@Configuration
public class Bdok100Config {

	private static final String JOB_NAME = "BDOK100";
	private static final String LOGNAME = "no.nav.dokprod.batch.bdok100";

	private static final String START = CleanableOnlyEmptyDecider.START.getName();
	private static final String CLEAN = CleanableOnlyEmptyDecider.CLEAN.getName();


	@Bean
	public Jpa2PingRepository pingRepository() {
		return new Jpa2PingRepository();
	}

	@Bean
	public Job bdok100Job(CommonJobParametersValidator bdok100JobParametersValidator,
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
				.validator(bdok100JobParametersValidator)
				.listener(logContextListener)
				.listener(userIdMdcJobExecutionListener)
				.listener(bdok100BatchExitListener)
				.listener(bdok100JobParametersValidator)
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
	public NAVKontors getKontorMap() {
		return new NAVKontors();
	}

	@Bean
	public VedleggsLister getVedleggsMap() {
		return new VedleggsLister();
	}

	@Bean
	public ItemWriter<Bdok100ArbTbl> bdok100ArbTblItemWriter(
			EntityManagerFactory entityManagerFactory
	) {
		return new JpaItemWriter<>(entityManagerFactory);
	}

	public static JpaCursorItemReader<Bdok100ArbTbl> createArbTblReaderForStatus(
		Bdok100Status status, Integer workUnit, EntityManagerFactory em) {
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
	public CommonJobParametersValidator bdok100JobParametersValidator(
			@Qualifier("commonJobParametersValidator")
					CommonJobParametersValidator commonJobParametersValidator,
			CommonJobParametersValidator.StringUriJobParameter inputFileLocationParameter,
			CommonJobParametersValidator.StringUriJobParameter staticInputFileLocationParameter,
			CommonJobParametersValidator.StringUriJobParameter outputFileLocationParameter,
			CommonJobParametersValidator.StringLongJobParameter maxFailuresParameter,
			CommonJobParametersValidator.StringUriJobParameter behandletFileLocationParameter,
			CommonJobParametersValidator.StringUriJobParameter failedFileLocationParameter,
			CommonJobParametersValidator.StringBooleanJobParameter skipClean,
			CommonJobParametersValidator.StringBooleanJobParameter clean) {
		CommonJobParametersValidator bdok100JobParametersValidator = new CommonJobParametersValidator();
		bdok100JobParametersValidator.setRequiredParameters(commonJobParametersValidator.getRequiredParameters());
		bdok100JobParametersValidator.addRequiredParameters(
				Arrays.asList(
						inputFileLocationParameter,
						staticInputFileLocationParameter,
						outputFileLocationParameter,
						maxFailuresParameter,
						behandletFileLocationParameter,
						failedFileLocationParameter)
		);
		ArrayList<CommonJobParametersValidator.StringJobParameter> optional =
				Lists.newArrayList(commonJobParametersValidator.getOptionalParameters());
		optional.add(skipClean);
		optional.add(clean);
		bdok100JobParametersValidator.setOptionalParameters(optional);
		return bdok100JobParametersValidator;
	}

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
	public CommonJobParametersValidator.StringUriJobParameter staticInputFileLocationParameter() {
		return new CommonJobParametersValidator.StringUriJobParameter(STATIC_INPUT_FILE_LOCATION_KEY);
	}

	@Bean
	public BeanValidator beanValidator() {
		return new BeanValidator();
	}

}
