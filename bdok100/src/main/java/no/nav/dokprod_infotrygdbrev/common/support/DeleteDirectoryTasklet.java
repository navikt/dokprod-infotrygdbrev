package no.nav.dokprod_infotrygdbrev.common.support;

import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tasklet that deletes the supplied directory.
 * Directory can only contain files
 *
 */
public class DeleteDirectoryTasklet implements Tasklet, InitializingBean {

	private Resource deleteDirectory;

	public void setDeleteDirectory(Resource deleteDirectory) {
		this.deleteDirectory = deleteDirectory;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		Path path = deleteDirectory.getFile().toPath();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path file : stream) {
				Files.delete(file);
			}
		}
		Files.delete(path);
		return RepeatStatus.FINISHED;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(deleteDirectory, "deleteDirectory can not be null.");
	}
}
