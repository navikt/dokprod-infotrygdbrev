package no.nav.dokprod_infotrygdbrev.common.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Test file reader
 *
 */
public class FileReader {

	private static Logger logger = LoggerFactory.getLogger(FileReader.class);

	public static List<String> readLines(File file, Charset charset) throws IOException {
		Path path = file.toPath();
		try {
			return Files.readAllLines(path, charset);
		} catch (MalformedInputException exception) {
			logger.error("Could not read file " + file.getAbsolutePath(), exception);
			throw exception;
		}
	}
}
