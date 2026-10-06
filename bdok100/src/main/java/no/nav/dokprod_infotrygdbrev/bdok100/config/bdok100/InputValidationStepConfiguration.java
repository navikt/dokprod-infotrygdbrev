package no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.VedleggsLister;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.BEHANDLET_FILE_LOCATION_KEY;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.FAILED_FILE_LOCATION_KEY;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.INPUT_FILE_LOCATION_KEY;
import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.OUTPUT_FILE_LOCATION_KEY;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.STATIC_INPUT_FILE_LOCATION_KEY;
import static no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper.JOURNALDATA_CSV;
import static no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper.KONTOR_CSV;
import static no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper.LINJEDATA_TXT;
import static no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper.VEDLEGG_CSV;
import static no.nav.dokprod_infotrygdbrev.common.BDOKCommonBatchInputParameters.CLEAN_KEY;

/**
 * Inputvalidation step configuration
 *
 */
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
			@Value("file:#{jobParameters[" + INPUT_FILE_LOCATION_KEY + "]}") final Resource inputFileLocation,
			@Value("file:#{jobParameters[" + STATIC_INPUT_FILE_LOCATION_KEY + "]}") Resource staticFileLocation,
			@Value("file:#{jobParameters[" + OUTPUT_FILE_LOCATION_KEY + "]}") Resource outputFileLocation,
			@Value("file:#{jobParameters[" + BEHANDLET_FILE_LOCATION_KEY + "]}") Resource behandletFileLocation,
			@Value("file:#{jobParameters[" + FAILED_FILE_LOCATION_KEY + "]}") Resource failedFileLocation,
			NAVKontors navKontors,
			VedleggsLister vedleggsLister
	) throws IOException {
		Bdok100InputValidationTasklet tasklet = new Bdok100InputValidationTasklet();
		tasklet.setInputFolder(inputFileLocation);
		tasklet.setStaticInputFolder(staticFileLocation);
		tasklet.setWritableAreas(outputFileLocation, behandletFileLocation, failedFileLocation);
		tasklet.setNavKontors(navKontors);
		tasklet.setVedleggsLister(vedleggsLister);
		return tasklet;
	}

	public static class Bdok100InputValidationTasklet implements Tasklet {

		private Path inputFolder;
		private Path staticInputFolder;
		private List<Path> writeableFolders = Lists.newArrayList();
		private NAVKontors navKontors;
		private VedleggsLister vedleggsLister;

		@Override
		public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
			ExecutionContext executionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();

			boolean cleanOnly = BooleanUtils.isTrue((Boolean) executionContext.get(CLEAN_KEY));
			if (cleanOnly) {
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

		public void setInputFolder(Resource inputFolder) {
			this.inputFolder = toPath(inputFolder);
		}

		public void setStaticInputFolder(Resource staticInputFolder) {
			this.staticInputFolder = toPath(staticInputFolder);
		}

		public void setWritableAreas(Resource... locations) {
			for (Resource location : locations) {
				writeableFolders.add(toPath(location));
			}
		}

		public void setNavKontors(NAVKontors navKontors) {
			this.navKontors = navKontors;
		}

		public void setVedleggsLister(VedleggsLister vedleggsLister) {
			this.vedleggsLister = vedleggsLister;
		}

		@SneakyThrows
		private Path toPath(Resource file) {
			return file.getFile().toPath();
		}
	}
}
