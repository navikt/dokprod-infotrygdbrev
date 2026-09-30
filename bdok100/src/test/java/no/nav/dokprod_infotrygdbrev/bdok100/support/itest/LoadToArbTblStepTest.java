package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Errors;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.repo.Bdok100Repo;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * Itest for load files steps
 *
 */
public class LoadToArbTblStepTest extends AbstractBdok100StepTest {
	public static final String LINJE_ID_NR_FIRST = "ODIQ026082400020";
	public static final String LINJE_ID_NR_LAST = "ODIQ026082400024";

	public static final String JOURNAL_ID_NR_FIRST = "ODIQ210102100001";
	public static final String JOURNAL_ID_NR_LAST = "ODIQ210102100009";

	private static final String LINJEDATA_FOLDER = "bdok100/linjedata/";
	private static final String FULL_LINJEDATA = "full_utdrag.crlf.txt";
	private static final String MISSING_ARKIV_LPF = "manglende_arkivid.crlf.txt";

	private static final String JOURNALDATA_FOLDER = "bdok100/journaldata/";
	private static final String VALID_JOURNALDATA = "valid_records.txt";
	private static final String MISSING_ONDEMANDID_JFF = "missing_ondemandid.txt";

	@Autowired
	private Bdok100Repo bdok100Repo;

	@Test
	public void readAndSaveLinjedata() throws Exception {
		JobExecution jobExecution = launchStep("bulkLinjedataToArbTblStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		List<Bdok100ArbTbl> bdok100ArbTbl = bdok100Repo.findAll();
		assertThat(bdok100ArbTbl, hasSize(5));

		Bdok100ArbTbl firstRecord = bdok100ArbTbl.get(0);
		assertThat(firstRecord.getIdnr(), is(LINJE_ID_NR_FIRST));
		assertThat(firstRecord.getLineprintBrev(), notNullValue());
		assertThat(firstRecord.getStatus(), is(Bdok100Status.UNDER_INNLESNING_LPF));

		Bdok100ArbTbl lastRecord = bdok100ArbTbl.get(4);
		assertThat(lastRecord.getIdnr(), is(LINJE_ID_NR_LAST));
		assertThat(lastRecord.getLineprintBrev(), notNullValue());
		assertThat(lastRecord.getStatus(), is(Bdok100Status.UNDER_INNLESNING_LPF));
	}

	@Test
	public void setStatusFailWhenMissingArkivId() throws Exception {
		JobExecution jobExecution = launchStep("bulkLinjedataToArbTblStep", getDefaultBdok100JobParameters(), getFailingLinjedataContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		List<Bdok100ArbTbl> bdok100ArbTbl = bdok100Repo.findAll();
		assertThat(bdok100ArbTbl, hasSize(1));

		Bdok100ArbTbl record = bdok100ArbTbl.get(0);
		assertThat(record.getIdnr(), notNullValue());
		assertThat(record.getLineprintBrev(), notNullValue());
		assertThat(record.getStatus(), is(Bdok100Status.KAN_IKKE_BEHANDLES));
		assertThat(record.getFeilstatus(), is(Bdok100Errors.MISSING_ARKIVID_LPF));
	}

	@Test
	public void readAndSaveJournaldata() throws Exception {
		insertLinjedata();

		JobExecution jobExecution = launchStep("bulkJournaldataToArbTblStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		List<Bdok100ArbTbl> bdok100ArbTbl = bdok100Repo.findAll();
		assertThat(bdok100ArbTbl, hasSize(11));

		for (Bdok100ArbTbl record : bdok100ArbTbl) {
			assertThat(record.getIdnr(), is(record.getJournalforingsfil().substring(0, 16)));
			assertThat(record.getJournalforingsfil(), notNullValue());
			assertThat(record.getStatus(), is(Bdok100Status.UNDER_INNLESNING_JFF));
			assertThat(record.getFeilstatus(), nullValue());

			if (record.getIdnr().equals(JOURNAL_ID_NR_FIRST)) {
				assertThat(record.getLineprintBrev(), is("test"));
			} else if (record.getIdnr().equals(JOURNAL_ID_NR_LAST)) {
				assertThat(record.getLineprintBrev(), is("test2"));
			} else {
				assertThat(record.getLineprintBrev(), nullValue());
			}

		}
	}

	@Test
	public void setStatusFailWhenMissingOnDemandId() throws Exception {
		JobExecution jobExecution = launchStep("bulkJournaldataToArbTblStep", getDefaultBdok100JobParameters(), getFailingJournaldataContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		List<Bdok100ArbTbl> bdok100ArbTbl = bdok100Repo.findAll();
		assertThat(bdok100ArbTbl, hasSize(1));

		Bdok100ArbTbl record = bdok100ArbTbl.get(0);
		assertThat(record.getIdnr(), notNullValue());
		assertThat(record.getJournalforingsfil(), notNullValue());
		assertThat(record.getStatus(), is(Bdok100Status.KAN_IKKE_BEHANDLES));
		assertThat(record.getFeilstatus(), is(Bdok100Errors.MISSING_ONDEMANDID_JFF));
	}

	@Test
	public void shouldNotUpdateStatusOnFailedLPF() throws Exception {
		insertFailedLinjedata();

		JobExecution jobExecution = launchStep("bulkJournaldataToArbTblStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
		assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));

		List<Bdok100ArbTbl> failedRecords = bdok100Repo.findByStatus(Bdok100Status.KAN_IKKE_BEHANDLES);
		assertThat(failedRecords, hasSize(1));

		Bdok100ArbTbl failedRecord = failedRecords.get(0);
		assertThat(failedRecord.getJournalforingsfil(), notNullValue());
		assertThat(failedRecord.getStatus(), is(Bdok100Status.KAN_IKKE_BEHANDLES));
		assertThat(failedRecord.getFeilstatus(), is(Bdok100Errors.MISSING_ARKIVID_LPF));

		List<Bdok100ArbTbl> okRecords = bdok100Repo.findByStatus(Bdok100Status.UNDER_INNLESNING_JFF);
		assertThat(okRecords, hasSize(10));

	}

	private void insertLinjedata() {
		bdok100Repo.save(Bdok100ArbTbl.builder()
				.idnr(JOURNAL_ID_NR_FIRST)
				.lineprintBrev("test")
				.status(Bdok100Status.UNDER_INNLESNING_LPF)
				.build());

		bdok100Repo.save(Bdok100ArbTbl.builder()
				.idnr(JOURNAL_ID_NR_LAST)
				.lineprintBrev("test2")
				.status(Bdok100Status.UNDER_INNLESNING_LPF)
				.build());
	}

	private void insertFailedLinjedata() {
		bdok100Repo.save(Bdok100ArbTbl.builder()
				.idnr(JOURNAL_ID_NR_FIRST)
				.lineprintBrev("test")
				.status(Bdok100Status.KAN_IKKE_BEHANDLES)
				.feilstatus(Bdok100Errors.MISSING_ARKIVID_LPF)
				.build());
	}

	protected ExecutionContext getJobExecutionContext() throws Exception {
		String linjedataPath = new ClassPathResource(LINJEDATA_FOLDER + FULL_LINJEDATA).getFile().getPath();
		String journaldataPath = new ClassPathResource(JOURNALDATA_FOLDER + VALID_JOURNALDATA).getFile().getPath();

		ExecutionContext executionContext = getDefaultCommonJobExecutionContext();
		executionContext.put(Bdok100Constants.CURRENT_LINJEDATA, linjedataPath);
		executionContext.put(Bdok100Constants.CURRENT_JOURNALDATA, journaldataPath);
		executionContext.putLong(CommonBatchInputParameters.WORK_UNIT_KEY, 2L);
		return executionContext;
	}

	private ExecutionContext getFailingLinjedataContext() throws Exception {
		String linjedataPath = new ClassPathResource(LINJEDATA_FOLDER + MISSING_ARKIV_LPF).getFile().getPath();
		ExecutionContext executionContext = getJobExecutionContext();
		executionContext.put(Bdok100Constants.CURRENT_LINJEDATA, linjedataPath);

		return executionContext;
	}

	private ExecutionContext getFailingJournaldataContext() throws Exception {
		String journaldataPath = new ClassPathResource(JOURNALDATA_FOLDER + MISSING_ONDEMANDID_JFF).getFile().getPath();
		ExecutionContext executionContext = getJobExecutionContext();
		executionContext.put(Bdok100Constants.CURRENT_JOURNALDATA, journaldataPath);

		return executionContext;
	}
}
