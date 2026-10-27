package no.nav.dokprod.infotrygdbrev.sftp;

import no.nav.dokprod.infotrygdbrev.bdok100.config.Bdok100BatchParameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.JOURNALDATA_CSV;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class SftpToLocalFileStagingServiceTest {

	private final SftpFileService sftpFileService = mock(SftpFileService.class);
	private SftpToLocalFileStagingService service;
	private Bdok100BatchParameters batchParameters;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Before
	public void setUp() {
		Path root = temporaryFolder.getRoot().toPath();
		batchParameters = new Bdok100BatchParameters(
				root.resolve("input"),
				root.resolve("static"),
				root.resolve("output"),
				root.resolve("failed"),
				root.resolve("behandlet"),
				2,
				10);
		service = new SftpToLocalFileStagingService(sftpFileService, batchParameters);
	}

	@Test
	public void shouldPrepareWorkspaceAndCleanStaleInput() throws Exception {
		Files.createDirectories(batchParameters.inputFileLocation());
		Files.createDirectories(batchParameters.staticInputFileLocation());
		Path staleInput = Files.writeString(batchParameters.inputFileLocation().resolve("stale.txt"), "stale");
		Path staleStaticInput = Files.writeString(batchParameters.staticInputFileLocation().resolve("stale.csv"), "stale");

		service.prepareLocalWorkspace();

		assertTrue(Files.notExists(staleInput));
		assertTrue(Files.notExists(staleStaticInput));
		assertTrue(Files.isDirectory(batchParameters.outputFileLocation()));
		assertTrue(Files.isDirectory(batchParameters.failedFileLocation()));
		assertTrue(Files.isDirectory(batchParameters.behandletFileLocation()));
	}

	@Test
	public void shouldStageFileAtTargetPath() throws Exception {
		byte[] contents = "journaldata".getBytes();
		Path target = batchParameters.inputFileLocation().resolve(JOURNALDATA_CSV);
		doAnswer(invocation -> {
			OutputStream outputStream = invocation.getArgument(1);
			outputStream.write(contents);
			return null;
		}).when(sftpFileService).readFile(eq(JOURNALDATA_CSV), any(OutputStream.class));

		service.stageFile(JOURNALDATA_CSV, target);

		assertArrayEquals(contents, Files.readAllBytes(target));
		assertTrue(Files.notExists(target.resolveSibling(JOURNALDATA_CSV + ".part")));
	}

	@Test
	public void shouldDeletePartialFileWhenDownloadFails() {
		Path target = batchParameters.inputFileLocation().resolve(JOURNALDATA_CSV);
		doThrow(new IllegalStateException("download failed"))
				.when(sftpFileService)
				.readFile(eq(JOURNALDATA_CSV), any(OutputStream.class));

		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				() -> service.stageFile(JOURNALDATA_CSV, target));

		assertThat(exception.getMessage(), containsString(JOURNALDATA_CSV));
		assertTrue(Files.notExists(target.resolveSibling(JOURNALDATA_CSV + ".part")));
	}
}
