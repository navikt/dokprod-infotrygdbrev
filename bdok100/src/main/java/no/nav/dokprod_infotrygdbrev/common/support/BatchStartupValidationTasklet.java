package no.nav.dokprod_infotrygdbrev.common.support;

import lombok.Setter;
import no.nav.dokprod_infotrygdbrev.common.StartupCriteria;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;

/**
 * Tasklet that does typical batch startup validation for a Spring Batch.
 * <p/>
 * Validates directory resources, checking whether they exist, are directories and are readable.
 *
 */
@Setter
public class BatchStartupValidationTasklet implements Tasklet, InitializingBean {

	private List<Resource> directoryResources;
	private StartupCriteria startupCriteria;

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

		checkDirectoryResources();
		checkStartupCriterias(getJobExecutionContext(chunkContext));

		return RepeatStatus.FINISHED;
	}

	private ExecutionContext getJobExecutionContext(ChunkContext chunkContext) {
		return chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
	}

	private void checkStartupCriterias(ExecutionContext jobExecutionContext) throws Exception {
		if (startupCriteria != null) {
			startupCriteria.check(jobExecutionContext);
		}
	}

	private void checkDirectoryResources() throws IOException {
		for (Resource resource : directoryResources) {
			checkResource(resource);
		}
	}

	private void checkResource(Resource resourceFile) throws IOException {
		String absolutePath = resourceFile.getFile().getAbsolutePath();

		Assert.isTrue(resourceFile.exists(), absolutePath + " must exist.");
		Assert.isTrue(resourceFile.getFile().isDirectory(), absolutePath + " must be a directory.");
		Assert.isTrue(resourceFile.getFile().canRead(), absolutePath + " must be readable. It is currently not.");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(directoryResources, "directoryResources can not be null.");
	}
}
