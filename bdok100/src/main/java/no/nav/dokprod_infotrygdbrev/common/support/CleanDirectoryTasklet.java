package no.nav.dokprod_infotrygdbrev.common.support;

import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Tasklet that cleans the supplied directory
 *
 */
public class CleanDirectoryTasklet implements Tasklet, InitializingBean {

	private Resource directory;

	public void setDirectory(Resource directory) {
		this.directory = directory;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		FileUtils.cleanDirectory(directory.getFile());

		return RepeatStatus.FINISHED;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(directory, "directory can not be null.");
	}
}
