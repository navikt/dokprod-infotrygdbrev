package no.nav.dokprod_infotrygdbrev.bdok100.domain.code;

import org.apache.commons.lang3.StringUtils;

/**
 * Distribusjonskanaler
 *
 */
public enum DistKanal {
	/**
	 * Lokalprint
	 */
	L,
	/**
	 * Sentralprint
	 */
	S;

	/**
	 * Helper that maps null and empty string to null
	 *
	 * @param name string representation
	 * @return the DistKanal
	 */
	public static DistKanal map(String name) {
		if (StringUtils.isBlank(name)) {
			return null;
		}
		return valueOf(name);
	}
}
