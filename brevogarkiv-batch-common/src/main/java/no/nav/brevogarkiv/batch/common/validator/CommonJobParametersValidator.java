package no.nav.brevogarkiv.batch.common.validator;

import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParametersValidator;
import org.springframework.batch.core.job.parameters.DefaultJobParametersValidator;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A job parameter validator that validates type conversion from string input to configured types. It delegates validation of
 * required and optional parameters to {@link DefaultJobParametersValidator}. Implements
 * {@link JobExecutionListener#beforeJob(JobExecution)} in order to put validated parameters on
 * the job execution context.
 * <p/>
 * Copied from Stelvio.
 */
public class CommonJobParametersValidator implements JobParametersValidator, JobExecutionListener,
		InitializingBean {
	private DefaultJobParametersValidator defaultValidator;
	private List<? extends StringJobParameter> requiredParameters = new ArrayList<>();
	private List<? extends StringJobParameter> optionalParameters = new ArrayList<>();
	private Map<String, StringJobParameter> allParameters;
	private ExecutionContext jobExecutionContext;

	@Override
	public void validate(final JobParameters parameters) throws InvalidJobParametersException {
		defaultValidator.validate(parameters);
		validateTypeConversions(parameters);
	}

	private void validateTypeConversions(final JobParameters parameters) throws InvalidJobParametersException {
		for (JobParameter<?> jobParameter : parameters) {
			validateParameter(jobParameter);
		}
	}

	private void validateParameter(final JobParameter<?> jobParameter)
			throws InvalidJobParametersException {
		StringJobParameter parameter = allParameters.get(jobParameter.name());
		if (jobParameter.type() != String.class) {
			throw new InvalidJobParametersException("The input JobParameters must be of type String");
		}
		parameter.validateTypeConversion((String) jobParameter.value());
	}

	/**
	 * Base class for type conversion validation.
	 */
	public abstract static class StringJobParameter {
		private final String key;

		public StringJobParameter(final String key) {
			this.key = key;
		}

		public abstract void validateTypeConversion(String jobParameterValue) throws InvalidJobParametersException;

		/**
		 * @return the key
		 */
		public String getKey() {
			return key;
		}

		public abstract Object getValue();
	}

	/**
	 * Defines type conversion from string to string for a job parameter.
	 */
	public static class StringStringJobParameter extends StringJobParameter {
		private String value;

		public StringStringJobParameter(final String key) {
			super(key);
		}

		@Override
		public void validateTypeConversion(final String jobParameterValue) throws InvalidJobParametersException {
			this.value = jobParameterValue;
		}

		@Override
		public String getValue() {
			return value;
		}
	}

	/**
	 * Defines type conversion from string to File for a job parameter. Checks if inputString is a file, if not throws
	 * {@link org.springframework.batch.core.job.parameters.InvalidJobParametersException}
	 */
	public static class StringFileJobParameter extends StringJobParameter {
		private String fileLocation;

		public StringFileJobParameter(final String key) {
			super(key);
		}

		@Override
		public void validateTypeConversion(final String jobParameterValue) throws InvalidJobParametersException {
			this.fileLocation = jobParameterValue;
			File file = new File(fileLocation);
			if (file.isDirectory()) {
				throw new InvalidJobParametersException("Jobparameter " + jobParameterValue + " is not a file");
			}
		}

		@Override
		public Object getValue() {
			return fileLocation;
		}

	}

	/**
	 * Defines type conversion from string to date for a job parameter.
	 */
	public static class StringDateJobParameter extends StringJobParameter {
		private final DateFormat dateformat;
		private Date value;

		public StringDateJobParameter(final String key, final String pattern) {
			super(key);
			this.dateformat = new SimpleDateFormat(pattern);
			dateformat.setLenient(false);
		}

		@Override
		public void validateTypeConversion(final String jobParameterValue) throws InvalidJobParametersException {
			try {
				this.value = dateformat.parse(jobParameterValue);
				if (!dateformat.format(getValue()).equals(jobParameterValue)) {
					throwValidationException(jobParameterValue);
				}
			} catch (ParseException e) {
				throwValidationException(jobParameterValue);
			}
		}

		private void throwValidationException(final String jobParameterValue) throws InvalidJobParametersException {
			throw new InvalidJobParametersException("Invalid date input for parameter:"
					+ getKey() + ", value=" + jobParameterValue);
		}

		@Override
		public Date getValue() {
			Date copy = new Date(value.getTime());
			return copy;
		}
	}

	/**
	 * Defines type conversion from string to long for a job parameter.
	 */
	public static class StringLongJobParameter extends StringJobParameter {
		private long value;

		public StringLongJobParameter(final String key) {
			super(key);
		}

		@Override
		public void validateTypeConversion(final String jobParameterValue) throws InvalidJobParametersException {
			try {
				this.value = Long.parseLong(jobParameterValue);
			} catch (NumberFormatException e) {
				throwValidationException(e);
			}
		}

		private void throwValidationException(NumberFormatException e) throws InvalidJobParametersException {
			throw new InvalidJobParametersException("Invalid long input for parameter:" + getKey() +
					" exception: " + e.getMessage());
		}

		@Override
		public Long getValue() {
			return value;
		}
	}

	public static class StringShortJobParameter extends StringJobParameter {
		private Short value;

		public StringShortJobParameter(final String key) {
			super(key);
		}

		@Override
		public void validateTypeConversion(final String jobParameterValue) throws InvalidJobParametersException {
			try {
				this.value = Short.parseShort(jobParameterValue);
			} catch (NumberFormatException e) {
				throwValidationException(e);
			}
		}

		private void throwValidationException(NumberFormatException e) throws InvalidJobParametersException {
			throw new InvalidJobParametersException("Invalid short input for parameter:" + getKey() +
					" exception: " + e.getMessage());
		}

		@Override
		public Short getValue() {
			return value;
		}
	}

	/**
	 * Defines type conversion from string to boolean for a job parameter.
	 */
	public static class StringBooleanJobParameter extends StringJobParameter {
		private boolean value;

		public StringBooleanJobParameter(final String key) {
			super(key);
		}

		@Override
		public void validateTypeConversion(final String jobParameterValue) throws InvalidJobParametersException {
			if (!isBooleanString(jobParameterValue)) {
				throw new InvalidJobParametersException("Invalid boolean input for parameter:" + getKey());
			}
			this.value = Boolean.parseBoolean(jobParameterValue);
		}

		private boolean isBooleanString(final String jobParameterValue) {
			return jobParameterValue.equalsIgnoreCase(Boolean.TRUE.toString())
					|| jobParameterValue.equalsIgnoreCase(Boolean.FALSE.toString());
		}

		@Override
		public Boolean getValue() {
			return value;
		}
	}

	@SuppressWarnings("rawtypes")
	public static class StringEnumJobParameter extends StringJobParameter {

		private Enum value;
		private Class enumType;

		public StringEnumJobParameter(final String key, final Class enumType) {
			super(key);
			this.enumType = enumType;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void validateTypeConversion(final String jobParameterValue) throws InvalidJobParametersException {
			try {
				this.value = Enum.valueOf(enumType, jobParameterValue);
			} catch (IllegalArgumentException e) {
				throwValidationException(e);
			}
		}

		private void throwValidationException(IllegalArgumentException e) throws InvalidJobParametersException {
			throw new InvalidJobParametersException("Invalid Enum input for parameter:" + getKey() +
					" exception: " + e.getMessage());
		}

		@Override
		public Enum getValue() {
			return value;
		}
	}

	/**
	 * Defines type conversion from string to URI for a job parameter.
	 * Note that the URI is set as a String on the jobContext.
	 */
	public static class StringUriJobParameter extends StringJobParameter {

		private String uri;

		public StringUriJobParameter(final String key) {
			super(key);
		}

		@Override
		public String getValue() {
			return uri;
		}

		@Override
		public void validateTypeConversion(final String jobParameterValue) throws InvalidJobParametersException {

			try {
				this.uri = new URI(jobParameterValue).toString();
			} catch (URISyntaxException e) {
				throwValidationException(e);
			}
		}

		private void throwValidationException(URISyntaxException e) throws InvalidJobParametersException {
			throw new InvalidJobParametersException("Invalid URI input for parameter: " + getKey() +
					" exception: " + e.getMessage());
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		createDefaultValidator();
		createAllParameters();

	}

	private void createAllParameters() {
		allParameters = new HashMap<>();
		for (StringJobParameter parameter : Stream.concat(requiredParameters.stream(),
				optionalParameters.stream()).collect(Collectors.toList())) {
			allParameters.put(parameter.getKey(), parameter);
		}
	}

	private void createDefaultValidator() {
		defaultValidator = new DefaultJobParametersValidator();
		defaultValidator.setRequiredKeys(getRequiredKeys());
		defaultValidator.setOptionalKeys(getOptionalKeys());
		defaultValidator.afterPropertiesSet();
	}

	private String[] getOptionalKeys() {
		return getKeys(optionalParameters);
	}

	private String[] getRequiredKeys() {
		return getKeys(requiredParameters);
	}

	private String[] getKeys(final List<? extends StringJobParameter> parameters) {
		String[] keys = new String[parameters.size()];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = parameters.get(i).getKey();
		}
		return keys;
	}

	@Override
	public void beforeJob(final JobExecution jobExecution) {
		jobExecutionContext = jobExecution.getExecutionContext();
		putParametersOnContext(jobExecution.getJobParameters());
	}

	private void putParametersOnContext(final JobParameters jobParameters) {
		for (JobParameter<?> jobParameter : jobParameters) {
			StringJobParameter stringParameter = allParameters.get(jobParameter.name());
			jobExecutionContext.put(stringParameter.getKey(), stringParameter.getValue());
		}
	}

	/**
	 * Get the required parameters
	 *
	 * @return the required parameters
	 */
	public List<? extends StringJobParameter> getRequiredParameters() {
		return requiredParameters;
	}

	/**
	 * Get the optional parameters
	 *
	 * @return the optional parameters
	 */
	public List<? extends StringJobParameter> getOptionalParameters() {
		return optionalParameters;
	}

	/**
	 * Sets required parameters. Batch will not start if a required parameter is missing.
	 *
	 * @param requiredParameters the requiredParameters to set
	 */
	public void setRequiredParameters(final List<? extends StringJobParameter> requiredParameters) {
		this.requiredParameters = requiredParameters;
	}

	/**
	 * Adds required parameters. Used by batches to define both a common and job-specific validator setup.
	 *
	 * @param requiredParameters the requiredParameters to add to the already existing ones
	 */
	@SuppressWarnings("unchecked")
	public void addRequiredParameters(final List<? extends StringJobParameter> requiredParameters) {
		this.requiredParameters = Stream.concat(this.requiredParameters.stream(), requiredParameters.stream()).collect(Collectors.toList());
	}

	/**
	 * Sets optional parameters. Batch can start if an optional parameter is missing.
	 *
	 * @param optionalParameters the optionalParameters to set
	 */
	public void setOptionalParameters(final List<? extends StringJobParameter> optionalParameters) {
		this.optionalParameters = optionalParameters;
	}

}

