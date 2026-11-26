package no.nav.dokprod.infotrygdbrev.sftp;

import no.nav.dokprod.infotrygdbrev.bdok100.support.LocalFileService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

@Service
public class SftpToLocalFileStagingService {

	private final SftpFileService sftpFileService;
	private final LocalFileService localFileService;

	public SftpToLocalFileStagingService(
			SftpFileService sftpFileService,
			LocalFileService localFileService
	) {
		this.sftpFileService = sftpFileService;
		this.localFileService = localFileService;
	}

	public void prepareLocalWorkspace() {
		localFileService.prepareWorkspace();
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
