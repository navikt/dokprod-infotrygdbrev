package no.nav.dokprod.infotrygdbrev.common.support;

import java.nio.file.Path;

/** Target paths are relative to the parent of the remote input directory. */
public interface PostRunFileTransfer {

	boolean moveFileIfExists(String sourceFileName, String targetRelativePath);

	void uploadFile(Path localFile, String targetRelativePath);
}
