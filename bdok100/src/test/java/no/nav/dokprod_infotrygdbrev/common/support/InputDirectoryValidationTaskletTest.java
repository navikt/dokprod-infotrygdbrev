package no.nav.dokprod_infotrygdbrev.common.support;

import java.io.File;
import java.io.IOException;

import no.nav.dokprod_infotrygdbrev.common.NotReadableFile;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * Unit tests for InputDirectoryValidationTasklet
 *
 */
public class InputDirectoryValidationTaskletTest {

	private InputDirectoryValidationTasklet inputDirectoryValidationTasklet;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private StepExecution stepExecution;

	@Before
	public void setUp() throws IOException {
		inputDirectoryValidationTasklet = new InputDirectoryValidationTasklet();
		stepExecution = createStepExecution();
	}

	@Test
	public void shouldValidateInputFileLocation() throws Exception {
		temporaryFolder.newFile("test.xml");
		Resource resource = new FileSystemResource(temporaryFolder.getRoot());
		inputDirectoryValidationTasklet.setInputFileLocation(resource);
		inputDirectoryValidationTasklet.afterPropertiesSet();

		inputDirectoryValidationTasklet.execute(stepContribution(), chunkContext());
	}

	@Test
	public void shouldThrowExceptionIfInputFileLocationDoesNotExist() throws Exception {
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("inputFileLocation must exist");

		Resource resource = new FileSystemResource(new File(temporaryFolder.getRoot(), "doesnotexist"));
		inputDirectoryValidationTasklet.setInputFileLocation(resource);
		inputDirectoryValidationTasklet.afterPropertiesSet();
	}

	@Test
	public void shouldThrowExceptionIfInputFileLocationIsNotDirectory() throws Exception {
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("inputFileLocation must be a directory");

		Resource resource = new FileSystemResource(temporaryFolder.newFile("notdirectory.xml"));
		inputDirectoryValidationTasklet.setInputFileLocation(resource);
		inputDirectoryValidationTasklet.afterPropertiesSet();
	}

	@Test
	public void shouldThrowExceptionIfInputFileLocationIsNotReadable() throws Exception {
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("inputFileLocation cannot be read");
		NotReadableFile notreadabledir = new NotReadableFile(temporaryFolder.getRoot(), "notreadabledir");
		notreadabledir.mkdir();
		Resource resource = new FileSystemResource(notreadabledir);

		inputDirectoryValidationTasklet.setInputFileLocation(resource);
		inputDirectoryValidationTasklet.afterPropertiesSet();
	}

	private StepContribution stepContribution() {
		return MetaDataInstanceFactory.createStepExecution().createStepContribution();
	}

	private ChunkContext chunkContext() {
		return new ChunkContext(new StepContext(stepExecution));
	}

	private StepExecution createStepExecution() {
		return MetaDataInstanceFactory.createStepExecution();
	}

}
