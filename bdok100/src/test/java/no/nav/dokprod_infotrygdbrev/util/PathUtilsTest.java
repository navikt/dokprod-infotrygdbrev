package no.nav.dokprod_infotrygdbrev.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;

/**
 * Unit tests for PathUtils
 *
 */
public class PathUtilsTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private Path directory;

	@Before
	public void setUp() throws Exception {
		directory = temporaryFolder.getRoot().toPath();
	}

	@Test
	public void shouldBeTrueWhenDirectoryIsEmpty() throws Exception {
		assertThat(PathUtils.isDirectoryEmpty(directory), is(true));
	}

	@Test
	public void shouldBeFalseWhenDirectoryIsNotEmpty() throws Exception {
		temporaryFolder.newFile();

		assertThat(PathUtils.isDirectoryEmpty(directory), is(false));
	}
}