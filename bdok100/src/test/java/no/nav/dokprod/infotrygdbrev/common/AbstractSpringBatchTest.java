package no.nav.dokprod.infotrygdbrev.common;

import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.dokprod.infotrygdbrev.config.BatchTestConfig;
import no.nav.dokprod.infotrygdbrev.util.MDCOperations;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.MDC;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {BatchTestConfig.class})
@ActiveProfiles("itest")
public abstract class AbstractSpringBatchTest extends JobLauncherTestUtils {

	private static final AtomicLong NEXT_START_TIME = new AtomicLong(System.currentTimeMillis());

	@Autowired
	protected EntityManager entityManager;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Override
	@Autowired
	public void setJobRepository(JobRepository jobRepository) {
		super.setJobRepository(jobRepository);
	}

	@Override
	@Autowired
	public void setJobLauncher(JobLauncher jobLauncher) {
		super.setJobLauncher(jobLauncher);
	}

	@Before
	public void setUpBaseTest() throws Exception {
		cleanDatabase();
	}

	private void cleanDatabase() {
		doInTransaction(() ->
			entityManager.createQuery("delete from Bdok100ArbTbl").executeUpdate());
	}

	protected Date getStartTime() {
		return new Date(NEXT_START_TIME.addAndGet(1_000));
	}

	protected abstract String getWorkUnit();

	protected abstract String getProgressInterval();

	protected JobParameters getDefaultCommonJobParameters() {
		return getDefaultCommonJobParametersBuilder().toJobParameters();
	}

	protected JobParametersBuilder getDefaultCommonJobParametersBuilder() {
		return new JobParametersBuilder()
				.addDate(CommonBatchInputParameters.START_TIME_KEY, getStartTime())
				.addString(CommonBatchInputParameters.WORK_UNIT_KEY, getWorkUnit())
				.addString(CommonBatchInputParameters.PROGRESS_INTERVAL_KEY, getProgressInterval());
	}

	protected ExecutionContext getDefaultCommonJobExecutionContext() {
		ExecutionContext jobExecutionContext = new ExecutionContext();
		jobExecutionContext.put(CommonBatchInputParameters.START_TIME_KEY, new Date());
		jobExecutionContext.putLong(CommonBatchInputParameters.WORK_UNIT_KEY, Long.valueOf(getWorkUnit()));
		jobExecutionContext.putString(CommonBatchInputParameters.PROGRESS_INTERVAL_KEY, getProgressInterval());
		return jobExecutionContext;
	}

	protected void doInTransaction(final Action action) {
		TransactionCallbackWithoutResult transactionCallbackWithoutResult =
				new TransactionCallbackWithoutResult() {
					@Override
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						action.doAction();
					}
				};
		transactionTemplate.execute(transactionCallbackWithoutResult);
	}

	protected interface Action {
		void doAction();
	}

	protected void persist(final Object... objects) {
		doInTransaction(new Action() {
			public void doAction() {
				boolean mdcSet = false;
				if (MDC.get(MDCOperations.MDC_USER_ID) == null) {
					MDC.put(MDCOperations.MDC_USER_ID, "itest");
					mdcSet = true;
				}
				for (Object object : objects) {
					entityManager.persist(object);
				}
				if (mdcSet) {
					MDC.remove(MDCOperations.MDC_USER_ID);
				}
			}
		});
	}
}
