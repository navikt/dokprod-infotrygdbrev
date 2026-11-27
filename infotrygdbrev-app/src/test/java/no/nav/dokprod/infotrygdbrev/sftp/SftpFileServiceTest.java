package no.nav.dokprod.infotrygdbrev.sftp;

import no.nav.dokprod.infotrygdbrev.bdok100.config.DokprodInfotrygdbrevProperties;
import org.apache.sshd.sftp.client.SftpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.integration.file.remote.InputStreamCallback;
import org.springframework.integration.file.remote.SessionCallback;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
		service = newService(REMOTE_DIRECTORY, "/output");
	}

	private SftpFileService newService(String remoteInputDirectory, String remoteOutputDirectory) {
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
						remoteInputDirectory,
						remoteOutputDirectory,
						Duration.ofSeconds(30)),
						new DokprodInfotrygdbrevProperties.ServiceUser("", ""),
				false,
				2,
				10);
		return new SftpFileService(remoteFileTemplate, properties);
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

	@Test
	public void shouldCreateEmptyRemoteFile() throws Exception {
		Session<SftpClient.DirEntry> session = mock(Session.class);
		doAnswer(invocation -> {
			SessionCallback<SftpClient.DirEntry, Void> callback = invocation.getArgument(0);
			return callback.doInSession(session);
		}).when(remoteFileTemplate).execute(any());

		service.createEmptyFile("started.txt");

		ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
		verify(session).write(inputStreamCaptor.capture(), eq(REMOTE_DIRECTORY + "/started.txt"));
		assertArrayEquals(new byte[0], inputStreamCaptor.getValue().readAllBytes());
	}

	@Test
	public void shouldDeleteRemoteFile() {
		when(remoteFileTemplate.remove(REMOTE_DIRECTORY + "/started.txt")).thenReturn(true);

		service.deleteFile("started.txt");

		verify(remoteFileTemplate).remove(REMOTE_DIRECTORY + "/started.txt");
	}

	@Test
	public void shouldFailWhenRemoteFileCannotBeDeleted() {
		when(remoteFileTemplate.remove(REMOTE_DIRECTORY + "/started.txt")).thenReturn(false);

		assertThrows(
				IllegalStateException.class,
				() -> service.deleteFile("started.txt"));
	}

	@Test
	public void shouldMoveRemoteFile() throws Exception {
		Session<SftpClient.DirEntry> session = mock(Session.class);
		doAnswer(invocation -> {
			SessionCallback<SftpClient.DirEntry, Void> callback = invocation.getArgument(0);
			return callback.doInSession(session);
		}).when(remoteFileTemplate).execute(any());
		when(session.exists(REMOTE_DIRECTORY + "/source.txt")).thenReturn(true);
		when(session.exists("/")).thenReturn(true);
		when(session.mkdir("/behandlet")).thenReturn(true);

		service.moveFile("source.txt", "behandlet/source.txt");

		verify(session).rename(REMOTE_DIRECTORY + "/source.txt", "/behandlet/source.txt");
	}

	@Test
	public void shouldWriteReportToSiblingOutputDirectory() throws Exception {
		Session<SftpClient.DirEntry> session = mock(Session.class);
		doAnswer(invocation -> {
			SessionCallback<SftpClient.DirEntry, Void> callback = invocation.getArgument(0);
			return callback.doInSession(session);
		}).when(remoteFileTemplate).execute(any());
		when(session.exists("/")).thenReturn(true);
		when(session.mkdir("/output")).thenReturn(true);
		byte[] contents = "rapport".getBytes();

		service.writeFile("rapport.txt", contents);

		ArgumentCaptor<InputStream> input = ArgumentCaptor.forClass(InputStream.class);
		verify(session).write(input.capture(), eq("/output/rapport.txt"));
		assertArrayEquals(contents, input.getValue().readAllBytes());
	}

	@Test
	public void shouldStreamDatabaseOutputToSiblingDirectory() throws Exception {
		Session<SftpClient.DirEntry> session = mock(Session.class);
		doAnswer(invocation -> {
			SessionCallback<SftpClient.DirEntry, Void> callback = invocation.getArgument(0);
			return callback.doInSession(session);
		}).when(remoteFileTemplate).execute(any());
		when(session.exists("/")).thenReturn(true);
		when(session.mkdir("/output")).thenReturn(true);
		byte[] content = "avvik".getBytes();
		doAnswer(invocation -> {
			InputStream stream = invocation.getArgument(0);
			assertArrayEquals(content, stream.readAllBytes());
			return null;
		}).when(session).write(any(InputStream.class), eq("/output/avvik.txt"));
		SftpClient.DirEntry uploadedFile = remoteFile("avvik.txt", content.length);
		when(session.list("/output")).thenReturn(new SftpClient.DirEntry[]{uploadedFile});

		service.uploadContent(new ByteArrayInputStream(content), "avvik.txt");

		verify(session).write(any(InputStream.class), eq("/output/avvik.txt"));
		verify(session).list("/output");
	}

	@Test
	public void shouldRejectIncompleteUpload() throws Exception {
		Session<SftpClient.DirEntry> session = mock(Session.class);
		doAnswer(invocation -> {
			SessionCallback<SftpClient.DirEntry, Void> callback = invocation.getArgument(0);
			return callback.doInSession(session);
		}).when(remoteFileTemplate).execute(any());
		when(session.exists("/")).thenReturn(true);
		when(session.mkdir("/output")).thenReturn(true);

		assertThrows(IllegalStateException.class,
			() -> service.uploadContent(new ByteArrayInputStream("avvik".getBytes()), "avvik.txt"));
	}

	@Test
	public void shouldRejectMissingOrTruncatedRemoteFile() throws Exception {
		Session<SftpClient.DirEntry> session = mock(Session.class);
		doAnswer(invocation -> {
			SessionCallback<SftpClient.DirEntry, Void> callback = invocation.getArgument(0);
			return callback.doInSession(session);
		}).when(remoteFileTemplate).execute(any());
		when(session.exists("/")).thenReturn(true);
		when(session.mkdir("/output")).thenReturn(true);
		doAnswer(invocation -> {
			InputStream stream = invocation.getArgument(0);
			stream.transferTo(new ByteArrayOutputStream());
			return null;
		}).when(session).write(any(InputStream.class), eq("/output/avvik.txt"));
		SftpClient.DirEntry truncatedFile = remoteFile("avvik.txt", 2);
		when(session.list("/output")).thenReturn(
			new SftpClient.DirEntry[0],
			new SftpClient.DirEntry[]{truncatedFile});

		assertThrows(IllegalStateException.class,
			() -> service.uploadContent(new ByteArrayInputStream("avvik".getBytes()), "avvik.txt"));
		assertThrows(IllegalStateException.class,
			() -> service.uploadContent(new ByteArrayInputStream("avvik".getBytes()), "avvik.txt"));
	}

	@Test
	public void shouldUseConfiguredOutputDirectoryIndependentlyOfInputArchive() throws Exception {
		service = newService("inbound", "reports");
		Session<SftpClient.DirEntry> session = mock(Session.class);
		doAnswer(invocation -> {
			SessionCallback<SftpClient.DirEntry, Void> callback = invocation.getArgument(0);
			return callback.doInSession(session);
		}).when(remoteFileTemplate).execute(any());
		when(session.exists("inbound/source.txt")).thenReturn(true);
		when(session.mkdir("behandlet")).thenReturn(true);
		when(session.mkdir("behandlet/bdok100-behandlet-2026")).thenReturn(true);
		when(session.mkdir("reports")).thenReturn(true);
		SftpClient.DirEntry uploadedFile = remoteFile("result.txt", 0);
		when(session.list("reports")).thenReturn(new SftpClient.DirEntry[]{uploadedFile});
		service.moveFileIfExists("source.txt", "behandlet/bdok100-behandlet-2026/source.txt");
		service.uploadContent(new ByteArrayInputStream(new byte[0]), "result.txt");

		verify(session).rename("inbound/source.txt", "behandlet/bdok100-behandlet-2026/source.txt");
		verify(session).write(any(InputStream.class), eq("reports/result.txt"));
	}

	private SftpClient.DirEntry entry(String name, boolean directory) {
		SftpClient.DirEntry entry = mock(SftpClient.DirEntry.class);
		SftpClient.Attributes attributes = mock(SftpClient.Attributes.class);
		when(entry.getFilename()).thenReturn(name);
		when(entry.getAttributes()).thenReturn(attributes);
		when(attributes.isDirectory()).thenReturn(directory);
		return entry;
	}

	private SftpClient.DirEntry remoteFile(String name, long size) {
		SftpClient.DirEntry entry = mock(SftpClient.DirEntry.class);
		SftpClient.Attributes attributes = mock(SftpClient.Attributes.class);
		when(entry.getFilename()).thenReturn(name);
		when(entry.getAttributes()).thenReturn(attributes);
		when(attributes.isRegularFile()).thenReturn(true);
		when(attributes.getSize()).thenReturn(size);
		return entry;
	}
}
