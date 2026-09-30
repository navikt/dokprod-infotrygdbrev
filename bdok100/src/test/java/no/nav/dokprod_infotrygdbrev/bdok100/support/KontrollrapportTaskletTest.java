package no.nav.dokprod_infotrygdbrev.bdok100.support;

import static no.nav.brevogarkiv.batch.common.CommonBatchInputParameters.OUTPUT_FILE_LOCATION_KEY;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Unit test for {@link KontrollrapportTasklet}
 */
@RunWith(MockitoJUnitRunner.class)
public class KontrollrapportTaskletTest {

	private static final String DATE_STRING = "2016-10-09_11-12-13";

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Mock
	private Bdok100Repo repo;

	@Mock
	private ChunkContext chunk;
	@Mock
	private StepContext stepContext;
	@Mock
	private StepContribution stepContribution;

	@InjectMocks
	private KontrollrapportTasklet kontrollrapportTasklet;

	private DateTimeFormatter format = DateTimeFormat.forPattern(Bdok100Constants.FILE_DATE_FORMAT);
	private File outputFolder;

	@Before
	public void setUp() throws IOException {
		Map<String, Object> map = Maps.newHashMap();
		outputFolder = folder.newFolder();
		map.put(OUTPUT_FILE_LOCATION_KEY, outputFolder.getAbsolutePath());
		LocalDateTime date = LocalDateTime.parse(DATE_STRING, format);
		map.put(CommonBatchInputParameters.START_TIME_KEY, date.toDate());

		when(repo.countByBrevtypeNotOk()).thenReturn(1L);
		when(repo.countByInfotrygdBrevkodePageEqualToFLXXXYY()).thenReturn(2L);
		when(repo.countByJournalforingsfilIsNotNull()).thenReturn(4L);
		when(repo.countByLineprintBrevIsNotNull()).thenReturn(5L);
		when(repo.countByStatus(Bdok100Status.BEHANDLET)).thenReturn(6L);
		when(repo.countByStatus(Bdok100Status.FEILET_GSAK)).thenReturn(11L);
		when(repo.countByStatus(Bdok100Status.KAN_IKKE_BEHANDLES)).thenReturn(12L);
		when(repo.countInvalidTkNr1()).thenReturn(15L);
		when(repo.countLocalBrevkode()).thenReturn(16L);
		when(repo.countByStatus(Bdok100Status.KAN_IKKE_BEHANDLES)).thenReturn(12L);
		when(repo.countByTknr1StartingWith23()).thenReturn(7L);
		when(chunk.getStepContext()).thenReturn(stepContext);
		when(stepContext.getJobExecutionContext()).thenReturn(map);
	}

	@Test
	public void shouldRun() throws Exception {
		RepeatStatus status = kontrollrapportTasklet.execute(stepContribution, chunk);
		assertThat(status, is(RepeatStatus.FINISHED));
		String[] list = outputFolder.list();
		String filename = list != null ? list[0] : "";
		assertThat(filename, startsWith("BDOK100_kontrollrapport_" + DATE_STRING));
		String fileAsString = Files.toString(new File(outputFolder, filename), Bdok100Constants.BDOK100_OUTPUT_CHARSET);
		assertThat(fileAsString, containsString("Antall dokumenter i linjeprintfil: 5"));
		assertThat(fileAsString, containsString("Antall rader i journalføringsfil: 4"));
		assertThat(fileAsString, containsString("Antall brev i dokumentbestilling: 6"));
		assertThat(fileAsString, containsString("Antall rader feilet: 12"));
		assertThat(fileAsString, containsString("Antall rader feilet teknisk: 11"));
		assertThat(fileAsString, containsString("Antall brev der NAV-nr = starter med '23': 7"));
		assertThat(fileAsString, containsString("Antall brev der brevnavn starter på ZH2,ZE2,Z10,Z20,Z30,Z40,ZH3 eller ZE3: 16"));
		assertThat(fileAsString, containsString("Antall brev der NAV-nr er i mellom (2399,2820),(2822,2999),(3100,3399),(3500,3799),(3900,4199),(4300,4400),(4500,4600) eller (5999,10000): 15"));
		assertThat(fileAsString, containsString("Antall brev der Infotrygd_BrevkodePage = FLXXX_YY: 2"));
		assertThat(fileAsString, containsString("Antall brev der brevtype <> OK: 1"));
	}

}
