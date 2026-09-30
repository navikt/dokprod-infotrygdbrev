package no.nav.dokprod_infotrygdbrev.common.support;

import java.io.IOException;

import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Tasklet for validating an InputFileLocation.
 *
 */
public class InputDirectoryValidationTasklet implements Tasklet, InitializingBean {

	private Resource inputFileLocation;

	public void setInputFileLocation(Resource inputFileLocation) {
		this.inputFileLocation = inputFileLocation;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		return RepeatStatus.FINISHED;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(inputFileLocation, "inputFileLocation can not be null");

		checkFileLocation(inputFileLocation, "inputFileLocation");
	}

	private void checkFileLocation(Resource resourceFile, String resourceKey) throws IOException {
		if (!resourceFile.exists()) {
			throw new RuntimeException(resourceKey + " must exist");
		}

		if (!resourceFile.getFile().isDirectory()) {
			throw new RuntimeException(resourceKey + " must be a directory");
		}

		if (!resourceFile.getFile().canRead()) {
			throw new RuntimeException(resourceKey + " cannot be read");
		}
	}
}
