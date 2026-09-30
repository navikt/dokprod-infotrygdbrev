package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Errors;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Linjedata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Adressetype;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.List;

import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.readJournaldataFile;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration test for mapJournaldataStep
 *
 */
public class JournaldataMapperStepTest extends AbstractBdok100StepTest {

    private static final String JOURNALDATA_VALID = "valid_records.txt";
    private static final String JOURNALDATA_INVALID_RECORDS = "invalid_records.txt";

    private static final List<String> VALID_IDS = Arrays.asList("ODIQ210102100001", "ODIQ210102100002", "ODIQ210102100006", "ODIQ210102100010");
    private static final List<String> INVALID_IDS = Arrays.asList("ODIQ210102100001", "ODIQ210102100002", "", "ODIQ210102100003", "ODIQ210102100004", "ODIQ210102100005");

    private List<String> validRecords;
    private List<String> invalidRecords;

    @Autowired
    private Bdok100Repo bdok100Repo;

    @Before
    public void setUp() throws Exception {
        validRecords = readJournaldataFile(JOURNALDATA_VALID);
        invalidRecords = readJournaldataFile(JOURNALDATA_INVALID_RECORDS);
    }

    @Test
    public void shouldMapWhenCorrectStatus() throws Exception {
        insertValidRecords();

        JobExecution jobExecution = launchStep("mapJournaldataStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
        assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

        assertThat(bdok100Repo.findByStatus(Bdok100Status.KAN_IKKE_BEHANDLES), hasSize(1));
        assertThat(bdok100Repo.findByStatus(Bdok100Status.FEILET_GSAK), hasSize(1));

        List<Bdok100ArbTbl> bdok100ArbTbl = bdok100Repo.findByStatus(Bdok100Status.MAPPING_JFF);
        assertThat(bdok100ArbTbl, hasSize(2));

        assertThat(bdok100ArbTbl.get(0).getIdnr(), is(VALID_IDS.get(0)));
        assertThat(bdok100ArbTbl.get(0).getStatus(), is(Bdok100Status.MAPPING_JFF));
        assertThat(bdok100ArbTbl.get(1).getIdnr(), is(VALID_IDS.get(1)));
        assertThat(bdok100ArbTbl.get(1).getStatus(), is(Bdok100Status.MAPPING_JFF));
    }

    @Test
    public void shouldFailWhenFaultyFormat() throws Exception {
        insertFaulty(0);

        JobExecution jobExecution = launchStep("mapJournaldataStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
        assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

        List<Bdok100ArbTbl> failedRecords = bdok100Repo.findByStatus(Bdok100Status.KAN_IKKE_BEHANDLES);
        assertThat(failedRecords, hasSize(1));

        assertThat(failedRecords.get(0).getJournaldata(), nullValue());
        assertThat(failedRecords.get(0).getStatus(), is(Bdok100Status.KAN_IKKE_BEHANDLES));
        assertThat(failedRecords.get(0).getFeilstatus(), containsString("Invalid formatted record"));
    }

    @Test
    public void shouldFailWhenInvalidDataType() throws Exception {
        insertFaulty(5);

        JobExecution jobExecution = launchStep("mapJournaldataStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
        assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

        List<Bdok100ArbTbl> failedRecords = bdok100Repo.findByStatus(Bdok100Status.KAN_IKKE_BEHANDLES);
        assertThat(failedRecords, hasSize(1));

        assertThat(failedRecords.get(0).getJournaldata(), nullValue());
        assertThat(failedRecords.get(0).getStatus(), is(Bdok100Status.KAN_IKKE_BEHANDLES));
        assertThat(failedRecords.get(0).getFeilstatus(), containsString("mapping error:"));
    }

    @Test
    public void shouldMarkJournalstatusA() throws Exception {
        insertJournalStatusA();

        JobExecution jobExecution = launchStep("mapJournaldataStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
        assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

        List<Bdok100ArbTbl> failedRecords = bdok100Repo.findByStatus(Bdok100Status.KAN_IKKE_BEHANDLES);
        assertThat(failedRecords, hasSize(1));

        assertThat(failedRecords.get(0).getJournaldata(), notNullValue());
        assertThat(failedRecords.get(0).getStatus(), is(Bdok100Status.KAN_IKKE_BEHANDLES));
        assertThat(failedRecords.get(0).getFeilstatus(), containsString(Bdok100Errors.NOT_BREVBESTILLING));
    }

    private void insertValidRecords() {
        bdok100Repo.save(Bdok100ArbTbl.builder()
                .idnr(VALID_IDS.get(0))
                .status(Bdok100Status.MAPPING_LPF)
                .journalforingsfil(validRecords.get(0))
				.linjedata(Linjedata.builder()
						.adresseLinje1("adresseLinje1")
						.adresseLinje2("adresseLinje2")
						.adresseLinje3("0123 OSLO")
						.adresseLinje4("NORGE")
						.adressetype(Adressetype.NORSK).build())
                .build());

        bdok100Repo.save(Bdok100ArbTbl.builder()
                .idnr(VALID_IDS.get(1))
                .status(Bdok100Status.MAPPING_LPF)
                .journalforingsfil(validRecords.get(1))
                .linjedata(Linjedata.builder()
                        .adresseLinje1("adresseLinje1")
                        .adresseLinje2("adresseLinje2")
                        .adresseLinje3("0123 OSLO")
                        .adresseLinje4("NORGE")
                        .adressetype(Adressetype.NORSK).build())
                .build());

        bdok100Repo.save(Bdok100ArbTbl.builder()
                .idnr(VALID_IDS.get(2))
                .status(Bdok100Status.FEILET_GSAK)
                .journalforingsfil(validRecords.get(2))
				.linjedata(Linjedata.builder()
						.adresseLinje1("adresseLinje1")
						.adresseLinje2("adresseLinje2")
						.adresseLinje3("0123 OSLO")
						.adresseLinje4("NORGE")
						.adressetype(Adressetype.NORSK).build())
                .build());
        bdok100Repo.save(Bdok100ArbTbl.builder()
                .idnr(VALID_IDS.get(3))
                .status(Bdok100Status.KAN_IKKE_BEHANDLES)
                .journalforingsfil(validRecords.get(3))
				.linjedata(Linjedata.builder()
						.adresseLinje1("adresseLinje1")
						.adresseLinje2("adresseLinje2")
						.adresseLinje3("0123 OSLO")
						.adresseLinje4("NORGE")
						.adressetype(Adressetype.NORSK).build())
                .build());
    }

    private void insertFaulty(int failIndex) {
        bdok100Repo.save(Bdok100ArbTbl.builder()
                .idnr(INVALID_IDS.get(failIndex))
                .status(Bdok100Status.MAPPING_LPF)
                .journalforingsfil(invalidRecords.get(failIndex))
				.linjedata(Linjedata.builder()
						.adresseLinje1("adresseLinje1")
						.adresseLinje2("adresseLinje2")
						.adresseLinje3("0123 OSLO")
						.adresseLinje4("NORGE")
						.adressetype(Adressetype.NORSK).build())
                .build());
    }

    private void insertJournalStatusA() {
        bdok100Repo.save(Bdok100ArbTbl.builder()
                .idnr(VALID_IDS.get(3))
                .status(Bdok100Status.MAPPING_LPF)
                .journalforingsfil(validRecords.get(7))
				.linjedata(Linjedata.builder()
						.adresseLinje1("adresseLinje1")
						.adresseLinje2("adresseLinje2")
						.adresseLinje3("0123 OSLO")
						.adresseLinje4("NORGE")
						.adressetype(Adressetype.NORSK).build())
                .build());
    }
}
