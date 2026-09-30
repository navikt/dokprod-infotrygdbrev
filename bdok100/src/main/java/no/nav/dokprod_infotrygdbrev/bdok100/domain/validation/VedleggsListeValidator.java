package no.nav.dokprod_infotrygdbrev.bdok100.domain.validation;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe;
import no.nav.dokprod_infotrygdbrev.common.exception.BeanValidationException;
import no.nav.dokprod_infotrygdbrev.common.exception.VedleggFormatException;
import no.nav.dokprod_infotrygdbrev.common.support.BeanValidator;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * Validator for VedleggsListe
 *
 */
public class VedleggsListeValidator {
	private BeanValidator beanValidator;

	public void validate(VedleggsListe liste) {
		try {
			beanValidator.validate(liste);
		} catch (BeanValidationException exception) {
			throw new VedleggFormatException(exception.getMessage());
		}
	}

	public void validate(List<VedleggsListe> vedleggs) {
		for (VedleggsListe liste : vedleggs) {
			validate(liste);
		}
	}

	@Autowired
	public void setBeanValidator(BeanValidator beanValidator) {
		this.beanValidator = beanValidator;
	}

}
