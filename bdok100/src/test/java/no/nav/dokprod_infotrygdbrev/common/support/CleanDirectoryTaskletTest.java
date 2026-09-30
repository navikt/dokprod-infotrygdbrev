package no.nav.dokprod_infotrygdbrev.common.support;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.core.io.FileSystemResource;

public class CleanDirectoryTaskletTest {

	private CleanDirectoryTasklet cleanDirectoryTasklet = new CleanDirectoryTasklet();

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldCleanDirectory() throws Exception {
		temporaryFolder.newFile("hello.xml");
		temporaryFolder.newFile("hello.zip");
		cleanDirectoryTasklet.setDirectory(new FileSystemResource(temporaryFolder.getRoot()));

		cleanDirectoryTasklet.execute(stepContribution(), chunkContext());

		assertThat(temporaryFolder.getRoot().list().length, is(0));
	}

	@Test
	public void shouldThrowExceptionIfDirectoryIsNull() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("directory can not be null.");

		cleanDirectoryTasklet.afterPropertiesSet();
	}

	private StepContribution stepContribution() {
		return MetaDataInstanceFactory.createStepExecution().createStepContribution();
	}

	private ChunkContext chunkContext() {
		return new ChunkContext(new StepContext(MetaDataInstanceFactory.createStepExecution()));
	}
}
