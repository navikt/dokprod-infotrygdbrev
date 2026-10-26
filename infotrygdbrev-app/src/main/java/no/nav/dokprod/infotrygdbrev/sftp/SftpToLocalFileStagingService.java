package no.nav.dokprod.infotrygdbrev.sftp;

import no.nav.dokprod.infotrygdbrev.bdok100.config.bdok100.Bdok100BatchParameters;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

@Service
public class SftpToLocalFileStagingService {

	private final SftpFileService sftpFileService;
	private final Bdok100BatchParameters batchParameters;

	public SftpToLocalFileStagingService(
			SftpFileService sftpFileService,
			Bdok100BatchParameters batchParameters
	) {
		this.sftpFileService = sftpFileService;
		this.batchParameters = batchParameters;
	}

	public void prepareLocalWorkspace() {
		try {
			assureLocalDirectoryExistsAndIsEmpty(batchParameters.inputFileLocation());
			assureLocalDirectoryExistsAndIsEmpty(batchParameters.staticInputFileLocation());
			Files.createDirectories(batchParameters.outputFileLocation());
			Files.createDirectories(batchParameters.failedFileLocation());
			Files.createDirectories(batchParameters.behandletFileLocation());
		} catch (IOException e) {
			throw new IllegalStateException("Failed to prepare local BDOK100 workspace", e);
		}
	}

	private void assureLocalDirectoryExistsAndIsEmpty(Path directory) throws IOException {
		Files.createDirectories(directory);
		try (var files = Files.list(directory)) {
			for (Path file : files.toList()) {
				deleteRecursively(file);
			}
		}
	}

	private void deleteRecursively(Path path) throws IOException {
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
			try (var children = Files.list(path)) {
				for (Path child : children.toList()) {
					deleteRecursively(child);
				}
			}
		}
		Files.delete(path);
	}

	public void stageFile(String remoteFileName, Path target) {
		Path partialFile = target.resolveSibling(target.getFileName() + ".part");
		try {
			Files.createDirectories(target.getParent());
			try (OutputStream outputStream = Files.newOutputStream(
					partialFile,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING)) {
				sftpFileService.readFile(remoteFileName, outputStream);
			}
			finalizeSftpDownload(partialFile, target);
		} catch (IOException | RuntimeException e) {
			deleteTemporaryPartialFile(partialFile, e);
			throw new IllegalStateException("Failed to stage SFTP file " + remoteFileName, e);
		}
	}

	private void finalizeSftpDownload(Path partialFile, Path target) throws IOException {
		try {
			Files.move(
					partialFile,
					target,
					StandardCopyOption.ATOMIC_MOVE,
					StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(partialFile, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private void deleteTemporaryPartialFile(Path partialFile, Exception originalException) {
		try {
			Files.deleteIfExists(partialFile);
		} catch (IOException cleanupException) {
			originalException.addSuppressed(cleanupException);
		}
	}
}
