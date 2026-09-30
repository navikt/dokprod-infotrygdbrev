package no.nav.dokprod_infotrygdbrev.common.support;

import no.nav.dokprod_infotrygdbrev.common.exception.BeanValidationException;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Iterator;
import java.util.Set;

/**
 * General purpose Bean Validator
 * NB not to be used to validate objects that need to be persisted as is, as they will fail there
 *
 */
public class BeanValidator {

	private Validator validator;

	public BeanValidator() {
		LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
		validatorFactoryBean.afterPropertiesSet();
		validator = validatorFactoryBean.getValidator();
	}

	public void validate(Object bean) {
		Set<ConstraintViolation<Object>> result = this.validator.validate(bean);
		if (!result.isEmpty()) {
			StringBuilder sb = new StringBuilder(bean.getClass().getSimpleName() + " is not valid: ");
			for (Iterator<ConstraintViolation<Object>> it = result.iterator(); it.hasNext(); ) {
				ConstraintViolation<Object> violation = it.next();
				sb.append(violation.getPropertyPath()).append(" <").append(violation.getInvalidValue()).append("> - ").append(violation.getMessage());
				if (it.hasNext()) {
					sb.append("; ");
				}
			}
			throw new BeanValidationException(sb.toString());
		}
	}
}
