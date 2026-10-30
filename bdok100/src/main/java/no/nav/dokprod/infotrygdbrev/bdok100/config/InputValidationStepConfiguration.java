package no.nav.dokprod.infotrygdbrev.bdok100.config;

import no.nav.dokprod.infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod.infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod.infotrygdbrev.bdok100.support.to.VedleggsLister;
import org.apache.commons.io.FilenameUtils;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.JOURNALDATA_CSV;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.KONTOR_CSV;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.LINJEDATA_TXT;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.VEDLEGG_CSV;
import static no.nav.dokprod.infotrygdbrev.common.BDOKCommonBatchInputParameters.CLEAN_KEY;

@Configuration
public class InputValidationStepConfiguration extends AbstractBdok100StepConfig {

	@Bean
	public Step inputValidationStep(Tasklet bdok100InputValidationTasklet, JobRepository jobRepository) {
		return new StepBuilder("inputValidationStep", jobRepository)
				.tasklet(bdok100InputValidationTasklet)
				.exceptionHandler(bdok100ExceptionHandler)
				.listener(logContextListener)
				.listener(bdok100BatchCounterLogger)
				.allowStartIfComplete(true)
				.build();
	}

	@StepScope
	@Bean
	public Tasklet bdok100InputValidationTasklet(
		Bdok100BatchParameters bdok100BatchParameters,
			NAVKontors navKontors,
			VedleggsLister vedleggsLister
	) {
		return new Bdok100InputValidationTasklet(
			bdok100BatchParameters.inputFileLocation(),
			bdok100BatchParameters.staticInputFileLocation(),
			List.of(bdok100BatchParameters.outputFileLocation(), bdok100BatchParameters.behandletFileLocation(), bdok100BatchParameters.failedFileLocation()),
			navKontors, vedleggsLister);
	}

	public static class Bdok100InputValidationTasklet implements Tasklet {

		private final Path inputFolder;
		private final Path staticInputFolder;
		private final List<Path> writeableFolders = new ArrayList<>();
		private final NAVKontors navKontors;
		private final VedleggsLister vedleggsLister;

		public Bdok100InputValidationTasklet(
			Path inputFolder,
			Path staticInputFolder,
			Collection<Path> writeableFolders,
			NAVKontors navKontors,
			VedleggsLister vedleggsLister
		) {
			this.inputFolder = inputFolder;
			this.staticInputFolder = staticInputFolder;
			this.writeableFolders.addAll(writeableFolders);
			this.navKontors = navKontors;
			this.vedleggsLister = vedleggsLister;
		}

		@Override
		public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
			ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

			Boolean cleanOnly = (Boolean) executionContext.get(CLEAN_KEY);

			if (cleanOnly != null && cleanOnly) {
				return RepeatStatus.FINISHED;
			}

			executionContext.put(Bdok100Constants.CURRENT_LINJEDATA, pathToString(inputFolder.resolve(LINJEDATA_TXT)));
			executionContext.put(Bdok100Constants.CURRENT_JOURNALDATA, pathToString(inputFolder.resolve(JOURNALDATA_CSV)));
			executionContext.put(Bdok100Constants.CURRENT_NAVKONTOR, pathToString(staticInputFolder.resolve(KONTOR_CSV)));
			executionContext.put(Bdok100Constants.CURRENT_VEDLEGG, pathToString(staticInputFolder.resolve(VEDLEGG_CSV)));

			for (Path path : writeableFolders) {
				Assert.isTrue(Files.isWritable(path), "Cannot write to location " + path.toString());
			}
			navKontors.clear();
			vedleggsLister.clear();

			return RepeatStatus.FINISHED;
		}

		private String pathToString(Path path) {
			assertFile(path);
			return FilenameUtils.separatorsToUnix(path.toString());
		}

		private void assertFile(Path path) {
			if (!Files.isReadable(path)) {
				throw new RuntimeException("Cannot read file " + path.toString());
			}
		}
	}
}
