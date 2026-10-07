package no.nav.brevogarkiv.batch.common;

import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.CompletionPolicy;
import org.springframework.batch.infrastructure.repeat.RepeatContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.batch.infrastructure.repeat.policy.SimpleCompletionPolicy;
import org.springframework.util.Assert;

/**
 * Completion policy that uses the workUnit input parameter.
 *
 * @author Thomas Eugen Bj�rge, Visma Sirius
 */
public class ExecutionContextWorkUnitCompletionPolicy implements StepExecutionListener, CompletionPolicy {
	
	private CompletionPolicy delegate;
	private String keyName = "workUnit";

	/**
	 * Setter for the keyName property.
	 *
	 * @param keyName the keyName to set
	 */
	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	/** {@inheritDoc} */
	@Override
	public void beforeStep(StepExecution stepExecution) {
		ExecutionContext jobExecutionContext = stepExecution.getJobExecution().getExecutionContext();
		Assert.state(jobExecutionContext.containsKey(keyName), "JobExecutionContext does not contain Long parameter with key=["
				+ keyName + "]");
		delegate = new SimpleCompletionPolicy((int) jobExecutionContext.getLong(keyName));
	}

	/** {@inheritDoc} */
	@Override
	public boolean isComplete(RepeatContext context) {
		Assert.state(delegate != null, "The delegate resource has not been initialised. "
				+ "Remember to register this object as a StepListener.");
		return delegate.isComplete(context);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isComplete(RepeatContext context, RepeatStatus result) {
		Assert.state(delegate != null, "The delegate resource has not been initialised. "
				+ "Remember to register this object as a StepListener.");
		return delegate.isComplete(context, result);
	}

	/** {@inheritDoc} */
	@Override
	public RepeatContext start(RepeatContext parent) {
		Assert.state(delegate != null, "The delegate resource has not been initialised. "
				+ "Remember to register this object as a StepListener.");
		return delegate.start(parent);
	}

	/** {@inheritDoc} */
	@Override
	public void update(RepeatContext context) {
		Assert.state(delegate != null, "The delegate resource has not been initialised. "
				+ "Remember to register this object as a StepListener.");
		delegate.update(context);
	}

}
