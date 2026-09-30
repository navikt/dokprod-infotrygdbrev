package no.nav.dokprod_infotrygdbrev.bdok100.support.itest;

import no.nav.brevogarkiv.batch.common.CommonBatchInputParameters;
import no.nav.dokprod_infotrygdbrev.util.MDCOperations;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.slf4j.MDC;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowJob;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.util.ReflectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Baste Step test for BDOK100
 *
 */
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
		StepScopeTestExecutionListener.class})
public abstract class AbstractBdok100StepTest extends AbstractBdok100BatchTest {

	@Autowired
	private FlowJob bdok100Job;

	@Before
	public void setUpBdok100Itest() {
		MDC.put(MDCOperations.MDC_CALL_ID, "itestcall");
		MDC.put(MDCOperations.MDC_USER_ID, "itestuser");
		MDC.put(MDCOperations.MDC_CONSUMER_ID, "itestconsumer");
		invokeAfterPropertiesSetOnFlowJob();
	}

	@After
	public void tearDown() {
		MDC.remove(MDCOperations.MDC_CALL_ID);
		MDC.remove(MDCOperations.MDC_USER_ID);
		MDC.remove(MDCOperations.MDC_CONSUMER_ID);
	}

	/**
	 * The Spring Batch flow builder for the Java configuration does not call afterPropertiesSet
	 * on the SimpleFlow after the FlowJob has been constructed. Thus, the Job does not know of any steps
	 * until starts to run.
	 * <p>
	 * afterPropertiesSet is called in the SimpleFlowFactoryBean, which is used when configuring
	 * Spring Batch with XML.
	 * <p>
	 * We use reflection to explicitly call afterPropertiesSet and create the steps in the job to test
	 * steps individually in integration tests.
	 *
	 * @see org.springframework.batch.core.job.flow.support.SimpleFlow
	 * @see org.springframework.batch.core.configuration.xml.SimpleFlowFactoryBean
	 * @see org.springframework.batch.core.job.builder.FlowBuilder
	 */
	private void invokeAfterPropertiesSetOnFlowJob() {
		Field field = ReflectionUtils.findField(FlowJob.class, "flow");
		field.setAccessible(true);
		Flow flow = (Flow) ReflectionUtils.getField(field, bdok100Job);
		Method method = ReflectionUtils.findMethod(SimpleFlow.class, "afterPropertiesSet");
		ReflectionUtils.invokeMethod(method, flow);
	}

	protected ExecutionContext getJobExecutionContext() throws Exception {
		ExecutionContext executionContext = getDefaultCommonJobExecutionContext();
		executionContext.putLong(CommonBatchInputParameters.WORK_UNIT_KEY, 2L);
		executionContext.putString(CommonBatchInputParameters.OUTPUT_FILE_LOCATION_KEY, FilenameUtils.separatorsToUnix(tmpOutFileFolder.toString()));
		executionContext.putLong(CommonBatchInputParameters.MAX_FAILURES, 2L);

		return executionContext;
	}

}