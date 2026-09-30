package no.nav.dokprod_infotrygdbrev.support;

import com.google.common.math.IntMath;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * String utilities
 *
 */
public final class StrUtils {

	private static final int BITS_PER_CHAR = 5;
	private static final int CHAR_BASE = IntMath.pow(2, BITS_PER_CHAR);

	private StrUtils() {
	}

	private static final SecureRandom RANDOM = new SecureRandom();

	public static String createRandomId(int len) {
		if (len <= 0) {
			return "";
		}
		String id = new BigInteger(len * BITS_PER_CHAR, RANDOM).toString(CHAR_BASE);
		return StringUtils.leftPad(id, len, "0");
	}
}
