package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.STATIC_INPUT_FILE_LOCATION_KEY;
import static no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper.JOURNALDATA_CSV;
import static no.nav.dokprod_infotrygdbrev.bdok100.FilenameHelper.LINJEDATA_TXT;

import lombok.SneakyThrows;
import no.nav.brevogarkiv.batch.common.BatchCounter;
import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.brevogarkiv.batch.common.CounterEvent;
import no.nav.brevogarkiv.batch.common.EventCounter;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod_infotrygdbrev.common.AbstractSpringBatchTest;
import no.nav.dokprod_infotrygdbrev.util.MDCOperations;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.MDC;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.jms.JMSException;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

/**
 * Base test class for BDOK100 tests
 *
 */
public abstract class AbstractBdok100BatchTest extends AbstractSpringBatchTest {

	private static final Long PROGRESS_INTERVAL = 1L;
	private static final Long DEFAULT_WORK_UNIT = 2L;

	public static final String LINJEDATA_CRLF_TXT = "linjedata.crlf.txt";
	public static final String STATIC_FOLDER = "static";

	protected Path tmpInputFileFolder;
	protected Path tmpOutFileFolder;

	@Autowired
	protected Bdok100Repo bdok100Repo;

	@Autowired
	protected NAVKontors kontors;

	@Autowired
	private BatchCounter bdok100BatchCounter;

	@Autowired
	protected JmsTemplate jmsBdok100Template;

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Override
	@Autowired
	public void setJob(Job bdok100Job) {
		super.setJob(bdok100Job);
	}


	@Before
	public void setUpBdok100() throws Exception {
		MDC.put(MDCOperations.MDC_CALL_ID, "itestcall");
		MDC.put(MDCOperations.MDC_USER_ID, "itestuser");
		MDC.put(MDCOperations.MDC_CONSUMER_ID, "itestconsumer");

		setUpDirectories();
		bdok100BatchCounter.resetCounter();
		kontors.clear();
	}

	@After
	public void tearDownBdist100Itest() {
		MDC.remove(MDCOperations.MDC_CALL_ID);
		MDC.remove(MDCOperations.MDC_USER_ID);
		MDC.remove(MDCOperations.MDC_CONSUMER_ID);

	}

	protected void setupBdok100InputFiles() throws IOException {
		copyFileFromInputSourceFolderToInputFileFolder(JOURNALDATA_CSV);
		copyFileFromInputSourceFolderToInputFileFolder(LINJEDATA_CRLF_TXT, LINJEDATA_TXT);
		copyFolderFromInputSourceFolderToInputFileFolder(STATIC_FOLDER);
	}

	private void setUpDirectories() throws IOException {
		tmpInputFileFolder = temporaryFolder.newFolder("input").toPath();
		tmpOutFileFolder = temporaryFolder.newFolder("output").toPath();
	}

	public Path getTmpInputFileFolder() {
		return tmpInputFileFolder;
	}

	protected void copyFolderFromInputSourceFolderToInputFileFolder(String foldername) throws IOException {
		FileUtils.copyDirectoryToDirectory(Paths.get(getInputFileFolderSource().getAbsolutePath(), foldername).toFile(),
				tmpInputFileFolder.toFile());
	}

	protected void copyFileFromInputSourceFolderToInputFileFolder(String filename) throws IOException {
		FileUtils.copyFileToDirectory(Paths.get(getInputFileFolderSource().getAbsolutePath(), filename).toFile(),
				tmpInputFileFolder.toFile());
	}

	protected void copyFileFromInputSourceFolderToInputFileFolder(String filename, String targetFilename) throws IOException {
		FileUtils.copyFileToDirectory(Paths.get(getInputFileFolderSource().getAbsolutePath(), filename).toFile(),
				tmpInputFileFolder.toFile());
		Path file = tmpInputFileFolder.resolve(filename);
		Path target = file.resolveSibling(targetFilename);
		if (Files.exists(target)) {
			Files.delete(target);
		}
		Files.move(file, target);
	}

	protected File getInputFileFolderSource() throws IOException {
		Resource onClasspath = new ClassPathResource("bdok100/itest/");
		return onClasspath.getFile();
	}

	@SneakyThrows
	protected String getOutputFileAsString(String filename, Charset charset) {
		return new String(Files.readAllBytes(Paths.get(tmpOutFileFolder.toString(), filename)), charset);
	}

	protected Map<CounterEvent, ? extends EventCounter> getEventReport() {
		return bdok100BatchCounter.getEventReport();
	}

	@Override
	protected String getWorkUnit() {
		return DEFAULT_WORK_UNIT.toString();
	}

	@Override
	protected String getProgressInterval() {
		return PROGRESS_INTERVAL.toString();
	}

	protected JobParameters getDefaultBdok100JobParameters() {
		return parameterBuilder()
				.toJobParameters();
	}

	@SuppressWarnings({ "unchecked"})
	public int getJmsMessageCount()
	{
		return jmsBdok100Template.browseSelected(null, new BrowserCallback<Integer>() {

			@Override
			public Integer doInJms(Session s, QueueBrowser qb) throws JMSException {
				return Collections.list(qb.getEnumeration()).size();

			}
		});
	}

	@SneakyThrows
	protected JobParametersBuilder parameterBuilder() {
		Path behandlet = tmpOutFileFolder.resolveSibling("behandlet");
		Path failed = tmpOutFileFolder.resolveSibling("failed");
		Files.createDirectory(behandlet);
		Files.createDirectory(failed);
		return getDefaultCommonJobParametersBuilder()
				.addString(CommonBatchInputParameters.INPUT_FILE_LOCATION_KEY, FilenameUtils.separatorsToUnix(tmpInputFileFolder.toString()))
				.addString(STATIC_INPUT_FILE_LOCATION_KEY, FilenameUtils.separatorsToUnix(tmpInputFileFolder.resolve("static").toString()))
				.addString(CommonBatchInputParameters.OUTPUT_FILE_LOCATION_KEY, FilenameUtils.separatorsToUnix(tmpOutFileFolder.toString()))
				.addString(CommonBatchInputParameters.BEHANDLET_FILE_LOCATION_KEY, FilenameUtils.separatorsToUnix(behandlet.toString()))
				.addString(CommonBatchInputParameters.FAILED_FILE_LOCATION_KEY, FilenameUtils.separatorsToUnix(failed.toString()))
				.addString(CommonBatchInputParameters.MAX_FAILURES, "10");
	}
}
