package no.nav.dokprod_infotrygdbrev.common;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Spring batch test base class that handles creation of input and output directories
 *
 */
public abstract class AbstractInputOutputDirectoryBatchTest extends AbstractSpringBatchTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private File tmpInputFileFolder;
	private File tmpOutputFileFolder;

	@Before
	public void setUpInputOutputDirectories() throws IOException {
		tmpInputFileFolder = temporaryFolder.newFolder("input");
		tmpOutputFileFolder = temporaryFolder.newFolder("output");
	}

	protected abstract File getInputFileFolderSource() throws IOException;

	protected void copyFromInputSourceFolderToInputFileFolder() throws IOException {
		FileUtils.copyDirectory(getInputFileFolderSource(), getInputFileFolder());
	}

	protected void copyFromFileFromInputSourceFolderToInputFileFolder(String filename) throws IOException {
		FileUtils.copyFileToDirectory(Paths.get(getInputFileFolderSource().getAbsolutePath(), filename).toFile(),
				getInputFileFolder());
	}

	protected void copyFileToInputFolder(File file) {
		try {
			FileUtils.copyFileToDirectory(file, getInputFileFolder());
		} catch (IOException e) {
			throw new RuntimeException("Could not copy file to inputFileLocation", e);
		}
	}

	protected File getInputFileFolder() {
		return tmpInputFileFolder;
	}

	protected File getOutputFileFolder() {
		return tmpOutputFileFolder;
	}

	protected String getInputFileFolderAsString() {
		return getAbsolutePathAsUnixString(tmpInputFileFolder);
	}

	protected String getInputOutputFolderAsString() {
		return getAbsolutePathAsUnixString(tmpOutputFileFolder);
	}

	protected String getAbsolutePathAsUnixString(File file) {
		return FilenameUtils.separatorsToUnix(file.getAbsolutePath());
	}
}
