package no.nav.dokprod.infotrygdbrev.sftp;

import no.nav.dokprod.infotrygdbrev.bdok100.config.DokprodInfotrygdbrevProperties;
import no.nav.dokprod.infotrygdbrev.bdok100.support.RemoteReportWriter;
import no.nav.dokprod.infotrygdbrev.common.support.PostRunFileTransfer;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SftpFileService implements PostRunFileTransfer, RemoteReportWriter {

	private final SftpRemoteFileTemplate remoteFileTemplate;
	private final DokprodInfotrygdbrevProperties.SftpProperties sftpProperties;

	public SftpFileService(
			SftpRemoteFileTemplate remoteFileTemplate,
			DokprodInfotrygdbrevProperties dokprodInfotrygdbrevProperties
	) {
		this.remoteFileTemplate = remoteFileTemplate;
		this.sftpProperties = dokprodInfotrygdbrevProperties.sftp();
	}

	public Set<String> listFiles() {
		return Arrays.stream(remoteFileTemplate.list(sftpProperties.remoteInputDirectory()))
			.filter(entry -> !entry.getAttributes().isDirectory())
			.map(SftpClient.DirEntry::getFilename)
			.collect(Collectors.toUnmodifiableSet());
	}
	public Set<String> listBaseDirs() {
		return Arrays.stream(remoteFileTemplate.list("."))
			.map(SftpClient.DirEntry::getFilename)
			.collect(Collectors.toUnmodifiableSet());
	}

	public Set<String> list() {
		return Arrays.stream(remoteFileTemplate.list(sftpProperties.remoteInputDirectory()))
				.map(SftpClient.DirEntry::getFilename)
				.collect(Collectors.toUnmodifiableSet());
	}

	public void readFile(String fileName, OutputStream outputStream) {
		boolean read = remoteFileTemplate.get(remotePath(fileName), inputStream -> inputStream.transferTo(outputStream));
		if (!read) {
			throw new IllegalStateException("SFTP file not found: " + fileName);
		}
	}

	public void createEmptyFile(String fileName) {
		remoteFileTemplate.execute(session -> {
			session.write(InputStream.nullInputStream(), remotePath(fileName));
			return null;
		});
	}

	public void deleteFile(String fileName) {
		if (!remoteFileTemplate.remove(remotePath(fileName))) {
			throw new IllegalStateException("SFTP file could not be deleted: " + fileName);
		}
	}

	public void moveFile(String sourceFileName, String targetRelativePath) {
		String sourcePath = remotePath(sourceFileName);
		String targetPath = archivePath(targetRelativePath);
		remoteFileTemplate.execute(session -> {
			if (!session.exists(sourcePath)) {
				throw new IllegalStateException("SFTP source file not found for move: " + sourceFileName);
			}
			ensureRemoteDirectories(session, parentDirectory(targetPath));
			session.rename(sourcePath, targetPath);
			return null;
		});
	}

	@Override
	public boolean moveFileIfExists(String sourceFileName, String targetRelativePath) {
		String sourcePath = remotePath(sourceFileName);
		String targetPath = archivePath(targetRelativePath);
		return remoteFileTemplate.execute(session -> {
			if (!session.exists(sourcePath)) {
				return false;
			}
			ensureRemoteDirectories(session, parentDirectory(targetPath));
			session.rename(sourcePath, targetPath);
			return true;
		});
	}

	@Override
	public void writeFile(String fileName, byte[] content) {
		String targetPath = outputPath(fileName);
		remoteFileTemplate.execute(session -> {
			ensureRemoteDirectories(session, parentDirectory(targetPath));
			session.write(new ByteArrayInputStream(content), targetPath);
			return null;
		});
	}

	@Override
	public void uploadContent(InputStream content, String fileName) {
		String targetPath = outputPath(fileName);
		remoteFileTemplate.execute(session -> {
			ensureRemoteDirectories(session, parentDirectory(targetPath));
			CountingInputStream countedContent = new CountingInputStream(content);
			session.write(countedContent, targetPath);
			if (countedContent.read() != -1) {
				throw new IllegalStateException("SFTP upload did not consume the full content: " + targetPath);
			}
			String filename = Path.of(targetPath).getFileName().toString();
			boolean sizeMatches = Arrays.stream(session.list(parentDirectory(targetPath)))
				.anyMatch(entry -> entry.getFilename().equals(filename)
					&& entry.getAttributes().isRegularFile()
					&& entry.getAttributes().getSize() == countedContent.bytesRead);
			if (!sizeMatches) {
				throw new IllegalStateException("SFTP upload could not be verified: " + targetPath);
			}
			return null;
		});
	}

	private static final class CountingInputStream extends FilterInputStream {
		private long bytesRead;

		private CountingInputStream(InputStream input) {
			super(input);
		}

		@Override
		public int read() throws IOException {
			int value = super.read();
			if (value != -1) {
				bytesRead++;
			}
			return value;
		}

		@Override
		public int read(byte[] buffer, int offset, int length) throws IOException {
			int count = super.read(buffer, offset, length);
			if (count > 0) {
				bytesRead += count;
			}
			return count;
		}
	}

	private void ensureRemoteDirectories(Session<SftpClient.DirEntry> session, String remoteDirectory) {
		if (remoteDirectory == null || remoteDirectory.isBlank()) {
			return;
		}
		List<String> segments = Arrays.stream(remoteDirectory.split("/"))
			.filter(segment -> !segment.isBlank())
			.toList();
		String currentPath = remoteDirectory.startsWith("/") ? "/" : "";
		for (String segment : segments) {
			currentPath = currentPath.isEmpty() || currentPath.endsWith("/") ? currentPath + segment : currentPath + "/" + segment;
			try {
				if (!session.exists(currentPath) && !session.mkdir(currentPath)) {
					throw new IllegalStateException("Failed to create SFTP directory: " + currentPath);
				}
			} catch (IOException e) {
				throw new IllegalStateException("Failed to create SFTP directory: " + currentPath, e);
			}
		}
	}

	private String parentDirectory(String remotePath) {
		int lastSlash = remotePath.lastIndexOf('/');
		if (lastSlash < 0) {
			return "";
		}
		return remotePath.substring(0, lastSlash);
	}

	private String remotePath(String fileName) {
		String remoteDirectory = sftpProperties.remoteInputDirectory();
		return remoteDirectory.endsWith("/") ? remoteDirectory + fileName : remoteDirectory + "/" + fileName;
	}

	private String outputPath(String fileName) {
		return Path.of(sftpProperties.remoteOutputDirectory()).resolve(fileName).toString();
	}

	private String archivePath(String relativePath) {
		Path inputDirectory = Path.of(sftpProperties.remoteInputDirectory()).normalize();
		Path workspaceDirectory = inputDirectory.getParent();
		if (workspaceDirectory == null) {
			workspaceDirectory = inputDirectory.isAbsolute() ? inputDirectory : Path.of("");
		}
		return workspaceDirectory.resolve(relativePath).toString();
	}
}
