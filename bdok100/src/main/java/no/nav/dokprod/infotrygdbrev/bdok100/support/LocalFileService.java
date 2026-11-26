package no.nav.dokprod.infotrygdbrev.bdok100.support;

import no.nav.dokprod.infotrygdbrev.bdok100.config.Bdok100BatchParameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.JOURNALDATA_CSV;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.KONTOR_CSV;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.LINJEDATA_TXT;
import static no.nav.dokprod.infotrygdbrev.bdok100.FilenameHelper.VEDLEGG_CSV;

public class LocalFileService {

	private final Bdok100BatchParameters batchParameters;

	public LocalFileService(Bdok100BatchParameters batchParameters) {
		this.batchParameters = batchParameters;
	}

	public void prepareWorkspace() {
		try {
			assureDirectoryExistsAndIsEmpty(batchParameters.inputFileLocation());
			assureDirectoryExistsAndIsEmpty(batchParameters.staticInputFileLocation());
		} catch (IOException e) {
			throw new IllegalStateException("Failed to prepare local BDOK100 workspace", e);
		}
	}

	private void assureDirectoryExistsAndIsEmpty(Path directory) throws IOException {
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

	public List<Path> outputFiles() {
		if (!Files.exists(batchParameters.outputFileLocation())) {
			return List.of();
		}
		try (var files = Files.list(batchParameters.outputFileLocation())) {
			return files.filter(Files::isRegularFile).toList();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to list local BDOK100 output files", e);
		}
	}

	public void deleteLocalFiles() {
		try {
			for (String fileName : Set.of(JOURNALDATA_CSV, LINJEDATA_TXT)) {
				Files.deleteIfExists(batchParameters.inputFileLocation().resolve(fileName));
			}
			for (String fileName : Set.of(KONTOR_CSV, VEDLEGG_CSV)) {
				Files.deleteIfExists(batchParameters.staticInputFileLocation().resolve(fileName));
			}
			for (Path file : outputFiles()) {
				Files.delete(file);
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to delete local BDOK100 files after SFTP upload", e);
		}
	}

}
