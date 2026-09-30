package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import org.junit.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ReadNAVKontorStepWithFaultyDataTest extends AbstractBdok100StepTest {

    private static final String KONTOR_FILE = "bdok100/itest/kontor_with_invalid_test_entries.csv";

    @Test
    public void testLaunchMapperStepWithFaultyData() throws Exception {
        int uniqueValidKontorEntriesInFile = 11;
        JobExecution jobExecution = launchStep("bulkReadNAVKontorFileToMapStep", getDefaultBdok100JobParameters(), getJobExecutionContext());
        assertThat(jobExecution.getExitStatus(), is(ExitStatus.COMPLETED));
        assertThat(kontors.size(), is(uniqueValidKontorEntriesInFile));
        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        assertThat(stepExecution.getFilterCount(), is(1L));
        assertThat(stepExecution.getReadCount() - stepExecution.getFilterCount(), is((long)uniqueValidKontorEntriesInFile));
    }

    @Override
    protected ExecutionContext getJobExecutionContext() throws Exception {
        String vedleggResource = new ClassPathResource(KONTOR_FILE).getFile().getPath();

        ExecutionContext executionContext = getDefaultCommonJobExecutionContext();
        executionContext.put(Bdok100Constants.CURRENT_NAVKONTOR, vedleggResource);
        executionContext.putLong(CommonBatchInputParameters.WORK_UNIT_KEY, 2L);
        return executionContext;
    }

}
