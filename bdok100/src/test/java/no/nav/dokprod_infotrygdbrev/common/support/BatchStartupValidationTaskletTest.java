package no.nav.dokprod_infotrygdbrev.common.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import no.nav.dokprod_infotrygdbrev.common.StartupCriteria;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class BatchStartupValidationTaskletTest {

	private BatchStartupValidationTasklet batchStartupValidationTasklet = new BatchStartupValidationTasklet();

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void shouldValidateDirectories() throws Exception {
		Resource folder1 = new FileSystemResource(temporaryFolder.newFolder("folder1"));
		Resource folder2 = new FileSystemResource(temporaryFolder.newFolder("folder2"));
		batchStartupValidationTasklet.setDirectoryResources(Arrays.asList(folder1, folder2));

		batchStartupValidationTasklet.execute(stepContribution(), chunkContext());
	}

	@Test
	public void shouldThrowExceptionIfResourceDoesNotExist() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("must exist");

		List<Resource> resources = Arrays.<Resource>asList(new FileSystemResource(new File("doesnotexist")));
		batchStartupValidationTasklet.setDirectoryResources(resources);

		batchStartupValidationTasklet.execute(stepContribution(), chunkContext());
	}

	@Test
	public void shouldThrowExceptionIfResourceIsNotDirectory() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("must be a directory");

		Resource file = new FileSystemResource(temporaryFolder.newFile("afileinstead.xml"));
		batchStartupValidationTasklet.setDirectoryResources(Arrays.asList(file));

		batchStartupValidationTasklet.execute(stepContribution(), chunkContext());
	}

	@Test
	public void shouldCheckCriteria() throws Exception {
		Resource folder = new FileSystemResource(temporaryFolder.getRoot());
		StartupCriteria startupCriteriaMock = Mockito.mock(StartupCriteria.class);

		batchStartupValidationTasklet.setDirectoryResources(Arrays.asList(folder));
		batchStartupValidationTasklet.setStartupCriteria(startupCriteriaMock);

		batchStartupValidationTasklet.execute(stepContribution(), chunkContext());

		verify(startupCriteriaMock).check(any(ExecutionContext.class));
	}

	private StepContribution stepContribution() {
		return MetaDataInstanceFactory.createStepExecution().createStepContribution();
	}

	private ChunkContext chunkContext() {
		return new ChunkContext(new StepContext(MetaDataInstanceFactory.createStepExecution()));
	}
}