package no.nav.dokprod_infotrygdbrev;

import java.util.regex.Pattern;

public class Regexes {
	public static final String NOT_CONTROL_CHAR = "[^\\p{C}]";
	public static final Pattern NOT_CONTROL_CHAR_PATTERN = Pattern.compile(NOT_CONTROL_CHAR);
}
