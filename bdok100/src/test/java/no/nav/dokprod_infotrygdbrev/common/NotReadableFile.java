package no.nav.dokprod_infotrygdbrev.common;

import java.io.File;
import java.net.URI;

/**
 * File class for testing validation when file is not readable
 * 
 */
public class NotReadableFile extends File {

	private static final long serialVersionUID = 1L;

	public NotReadableFile(String pathname) {
		super(pathname);
	}

	public NotReadableFile(String parent, String child) {
		super(parent, child);
	}

	public NotReadableFile(File parent, String child) {
		super(parent, child);
	}

	public NotReadableFile(URI uri) {
		super(uri);
	}

	@Override
	public boolean canRead() {
		return false;
	}
}
