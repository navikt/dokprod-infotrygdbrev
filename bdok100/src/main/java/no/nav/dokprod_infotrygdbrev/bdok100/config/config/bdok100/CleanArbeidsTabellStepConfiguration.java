package no.nav.dokprod_infotrygdbrev.bdok100.config.config.bdok100;

import no.nav.brevogarkiv.batch.common.LogContextListener;
import no.nav.brevogarkiv.batch.common.ProgressNotifier;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import no.nav.dokprod_infotrygdbrev.bdok100.support.CleanArbeidstabellTasklet;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.VedleggsLister;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.exception.ExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.beans.factory.annotation.Autowired;

import static no.nav.dokprod_infotrygdbrev.common.BDOKCommonBatchInputParameters.CLEAN_KEY;
import static no.nav.dokprod_infotrygdbrev.common.BDOKCommonBatchInputParameters.SKIP_CLEAN_KEY;


/**
 * Deletes all rows in BDOK100 Arbeidstabell
 *
 */
@Configuration
public class CleanArbeidsTabellStepConfiguration {

	@Autowired
	private LogContextListener logContextListener;
	@Autowired
	private ExceptionHandler bdok100ExceptionHandler;
	@Autowired
	private ProgressNotifier bdok100BatchCounterLogger;

	@Bean
	public Step cleanArbeidsTabellStep(CleanArbeidstabellTasklet cleanArbeidstabellTasklet, JobRepository jobRepository) {
		return new StepBuilder("cleanArbeidsTabellStep", jobRepository)
				.tasklet(cleanArbeidstabellTasklet)
				.exceptionHandler(bdok100ExceptionHandler)
				.listener(logContextListener)
				.listener(bdok100BatchCounterLogger)
				.allowStartIfComplete(true)
				.build();
	}

	@Bean
	@StepScope
	public CleanArbeidstabellTasklet cleanArbeidstabellTasklet(
			@Value("#{jobParameters[" + SKIP_CLEAN_KEY + "] ?: false}") Boolean skipClean,
			@Value("#{jobParameters[" + CLEAN_KEY + "] ?: false}") Boolean clean,
			Bdok100Repo bdok100Repo,
			JdbcTemplate jdbcTemplate,
			NAVKontors navKontors,
			VedleggsLister vedleggsLister
	) {
		CleanArbeidstabellTasklet tasklet = new CleanArbeidstabellTasklet();
		tasklet.setSkipClean(skipClean);
		tasklet.setClean(clean);
		tasklet.setTemplate(jdbcTemplate);
		tasklet.setNavKontors(navKontors);
		tasklet.setVedleggsLister(vedleggsLister);
		tasklet.setBdok100Repo(bdok100Repo);
		return tasklet;
	}
}
