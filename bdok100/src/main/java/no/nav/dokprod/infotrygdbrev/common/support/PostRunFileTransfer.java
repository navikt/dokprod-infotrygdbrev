package no.nav.dokprod.infotrygdbrev.common.support;

import java.io.InputStream;

/** Target paths are relative to the parent of the remote input directory. */
public interface PostRunFileTransfer {

	boolean moveFileIfExists(String sourceFileName, String targetRelativePath);

	void uploadContent(InputStream content, String targetRelativePath);
}
