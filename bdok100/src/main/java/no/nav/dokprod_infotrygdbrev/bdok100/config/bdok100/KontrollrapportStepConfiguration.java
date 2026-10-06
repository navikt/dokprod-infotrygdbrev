package no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100;

import no.nav.brevogarkiv.batch.common.LogContextListener;
import no.nav.brevogarkiv.batch.common.ProgressNotifier;
import no.nav.dokprod_infotrygdbrev.bdok100.support.KontrollrapportTasklet;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.exception.ExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Configuration for the BDOK100 step that creates the kontrollrapport
 *
 */
@Configuration
public class KontrollrapportStepConfiguration {

	@Autowired
	private LogContextListener logContextListener;
	@Autowired
	private ExceptionHandler bdok100ExceptionHandler;
	@Autowired
	private ProgressNotifier bdok100BatchCounterLogger;

	@Bean
	public Step createKontrollrapportStep(KontrollrapportTasklet kontrollrapportTasklet, JobRepository jobRepository) {
		return new StepBuilder("createKontrollrapportStep", jobRepository)
				.tasklet(kontrollrapportTasklet)
				.exceptionHandler(bdok100ExceptionHandler)
				.listener(logContextListener)
				.listener(bdok100BatchCounterLogger)
				.allowStartIfComplete(true)
				.build();
	}

	@Bean
	@StepScope
	public KontrollrapportTasklet kontrollrapportTasklet() {
		return new KontrollrapportTasklet();
	}
}
