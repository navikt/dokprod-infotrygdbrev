package no.nav.dokprod_infotrygdbrev.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Util methods for NIO2 Path
 * <p/>
 * Meant to be like commons-io FileUtils
 *
 */
public class PathUtils {
	public PathUtils() {
	}

	public static boolean isDirectoryEmpty(final Path directory) throws IOException {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
			return !directoryStream.iterator().hasNext();
		}
	}
}
