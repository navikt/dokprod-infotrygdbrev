package no.nav.dokprod_infotrygdbrev.common.file;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Test helper for file read and map
 *
 */
public abstract class ObjectFromFileReader<T> {

	public List<T> readObjectsFromFile(String classPathResource, Charset charset) throws IOException {
		List<String> objectLines = FileReader.readLines(new ClassPathResource(classPathResource).getFile(), charset);
		List<T> toReturn = new ArrayList<>();

		for (String object : objectLines) {
			toReturn.add(map(object));
		}

		return toReturn;

	}

	public abstract T map(String string);
}
