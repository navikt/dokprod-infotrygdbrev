package no.nav.dokprod.infotrygdbrev.sftp;

import no.nav.dokprod.infotrygdbrev.bdok100.config.DokprodInfotrygdbrevProperties;
import no.nav.dokprod.infotrygdbrev.common.support.PostRunFileTransfer;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SftpFileService implements PostRunFileTransfer {

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
		return Arrays.stream(remoteFileTemplate.list(sftpProperties.remoteDirectory()))
			.filter(entry -> !entry.getAttributes().isDirectory())
			.map(SftpClient.DirEntry::getFilename)
			.collect(Collectors.toUnmodifiableSet());
	}

	public Set<String> list() {
		return Arrays.stream(remoteFileTemplate.list(sftpProperties.remoteDirectory()))
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
		String targetPath = workspacePath(targetRelativePath);
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
		String targetPath = workspacePath(targetRelativePath);
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
	public void uploadFile(Path localFile, String targetRelativePath) {
		if (!Files.isRegularFile(localFile)) {
			throw new IllegalStateException("Local file not found for upload: " + localFile);
		}
		String targetPath = workspacePath(targetRelativePath);
		remoteFileTemplate.execute(session -> {
			ensureRemoteDirectories(session, parentDirectory(targetPath));
			try (InputStream inputStream = Files.newInputStream(localFile)) {
				session.write(inputStream, targetPath);
			}
			return null;
		});
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
		String remoteDirectory = sftpProperties.remoteDirectory();
		return remoteDirectory.endsWith("/") ? remoteDirectory + fileName : remoteDirectory + "/" + fileName;
	}

	private String workspacePath(String relativePath) {
		Path inputDirectory = Path.of(sftpProperties.remoteDirectory()).normalize();
		Path workspaceDirectory = inputDirectory.getParent();
		if (workspaceDirectory == null) {
			workspaceDirectory = inputDirectory.isAbsolute() ? inputDirectory : Path.of("");
		}
		return workspaceDirectory.resolve(relativePath).toString();
	}
}
