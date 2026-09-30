package no.nav.dokprod_infotrygdbrev.bdok100.domain.validation;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.NAVKontor;
import no.nav.dokprod_infotrygdbrev.common.exception.BeanValidationException;
import no.nav.dokprod_infotrygdbrev.common.exception.NAVKontorFormatException;
import no.nav.dokprod_infotrygdbrev.common.support.BeanValidator;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Validator for {@link NAVKontor}
 *
 */
public class NAVKontorValidator {

	private BeanValidator beanValidator;

	public void validate(NAVKontor kontor) {
		try {
			if(Integer.valueOf(kontor.getTkNr()) < LinjedataValidator.TKNR_INVALID_GROUP_9_START) {
				beanValidator.validate(kontor);
			}
		} catch (BeanValidationException exception) {
			throw new NAVKontorFormatException(exception.getMessage());
		}
	}

	@Autowired
	public void setBeanValidator(BeanValidator beanValidator) {
		this.beanValidator = beanValidator;
	}

}
