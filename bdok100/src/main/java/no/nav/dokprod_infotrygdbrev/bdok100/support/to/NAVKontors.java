package no.nav.dokprod_infotrygdbrev.bdok100.support.to;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.NAVKontor;
import no.nav.dokprod_infotrygdbrev.common.exception.NAVKontorFormatException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * TO holder for Nav Kontor
 *
 */
@Component
public class NAVKontors extends ToHolder<String, NAVKontor> {

	private boolean checkForDuplicates = true;

	public String add(NAVKontor kontor) {
		String key = kontor.getTkNr();
		Assert.hasText(key, "nav kontor line can not be empty!");
		if (checkForDuplicates) {
			checkDuplicate(key);
		}
		put(key, kontor);
		return key;
	}

	/**
	 * Get NAVKontor from tkNummer
	 *
	 * @param tkNummer The TK-number for the NAV kontor
	 * @return The NAV kontor if found, null otherwise
	 */
	public NAVKontor getNavKontor(String tkNummer) {
		return getValue(tkNummer);
	}

	private void checkDuplicate(String key) {
		if (getValue(key) != null) {
			throw new NAVKontorFormatException("Duplicate tknr key in kontorfil for " + key);
		}

	}

	public boolean isCheckForDuplicates() {
		return checkForDuplicates;
	}

	public void setCheckForDuplicates(boolean checkForDuplicates) {
		this.checkForDuplicates = checkForDuplicates;
	}

}
