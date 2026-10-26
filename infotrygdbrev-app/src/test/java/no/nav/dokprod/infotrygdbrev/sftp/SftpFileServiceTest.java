package no.nav.dokprod.infotrygdbrev.sftp;

import no.nav.dokprod.infotrygdbrev.bdok100.config.DokprodInfotrygdbrevProperties;
import org.apache.sshd.sftp.client.SftpClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.file.remote.InputStreamCallback;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SftpFileServiceTest {

	private static final String REMOTE_DIRECTORY = "/input";

	private final SftpRemoteFileTemplate remoteFileTemplate = mock(SftpRemoteFileTemplate.class);
	private SftpFileService service;

	@Before
	public void setUp() {
		DokprodInfotrygdbrevProperties properties = new DokprodInfotrygdbrevProperties(
				null,
				null,
				new DokprodInfotrygdbrevProperties.SftpProperties(
						"localhost",
						22,
						"user",
						"private-key",
						"",
						"known-hosts",
						REMOTE_DIRECTORY,
						Duration.ofSeconds(30)),
				2,
				10);
		service = new SftpFileService(remoteFileTemplate, properties);
	}

	@Test
	public void shouldListFilesAndExcludeDirectories() {
		SftpClient.DirEntry[] entries = {
				entry("b.txt", false),
				entry("folder", true),
				entry("a.txt", false)
		};
		when(remoteFileTemplate.list(REMOTE_DIRECTORY)).thenReturn(entries);

		assertEquals(Set.of("a.txt", "b.txt"), service.listFiles());
	}

	@Test
	public void shouldReadRemoteFile() {
		byte[] contents = "contents".getBytes();
		doAnswer(invocation -> {
			InputStreamCallback callback = invocation.getArgument(1);
			callback.doWithInputStream(new ByteArrayInputStream(contents));
			return true;
		}).when(remoteFileTemplate).get(anyString(), any(InputStreamCallback.class));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		service.readFile("file.txt", outputStream);

		assertArrayEquals(contents, outputStream.toByteArray());
		verify(remoteFileTemplate).get(eq(REMOTE_DIRECTORY + "/file.txt"), any(InputStreamCallback.class));
	}

	@Test
	public void shouldFailWhenRemoteFileDoesNotExist() {
		when(remoteFileTemplate.get(anyString(), any(InputStreamCallback.class))).thenReturn(false);

		assertThrows(
				IllegalStateException.class,
				() -> service.readFile("missing.txt", new ByteArrayOutputStream()));
	}

	private SftpClient.DirEntry entry(String name, boolean directory) {
		SftpClient.DirEntry entry = mock(SftpClient.DirEntry.class);
		SftpClient.Attributes attributes = mock(SftpClient.Attributes.class);
		when(entry.getFilename()).thenReturn(name);
		when(entry.getAttributes()).thenReturn(attributes);
		when(attributes.isDirectory()).thenReturn(directory);
		return entry;
	}
}
