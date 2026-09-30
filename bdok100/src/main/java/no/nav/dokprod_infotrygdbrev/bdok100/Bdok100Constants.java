package no.nav.dokprod_infotrygdbrev.bdok100;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Constants for BDOK100
 *
 */
public final class Bdok100Constants {

	private Bdok100Constants() {
	}

	public static final Charset BDOK100_OUTPUT_CHARSET = StandardCharsets.UTF_8;
	public static final Charset BDOK100_INPUT_CHARSET = StandardCharsets.ISO_8859_1;

	public static final String STATIC_INPUT_FILE_LOCATION_KEY = "staticInputFileLocation";

	public static final String BDOK100_OUT_XML_FILENAME_FORMAT = "BDOK100_output_%s_%s.xml";
	public static final String BDOK100_KONTROLLRAPPORT_FILENAME_FORMAT = "BDOK100_kontrollrapport_%s_%s.txt";

	public static final String BDOK100_AVVIK_LPF_FILENAME_FORMAT = "BDOK100_avvik_LPF_%s.txt";
	public static final String BDOK100_AVVIK_JFF_FILENAME_FORMAT = "BDOK100_avvik_JFF_%s.txt";
	public static final String BDOK100_AVVIKSRAPPORT_FILENAME_FORMAT = "BDOK100_avviksrapport_%s.txt";

	public static final String CRLF = "\r\n";
	public static final String FILE_DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss";
	public static final String FILE_DATE_FORMAT_MS = FILE_DATE_FORMAT + "-SSS";

	public static final String INFOTRYGD_BREVKODE_PAGE = "XXXXX_YY";
	public static final String POSTNR_FALLBACK = "0000";
	// Definert i Doksys og dokumentdistribusjon: Ukjent land
	public static final String UKJENT_LANDKODE_UTLAND = "XX";
	public static final String LANDKODE_NORGE = "NO";
	public static final String FS_22 = "FS22";

	public static final String CURRENT_JOURNALDATA = "currentJournaldata";
	public static final String CURRENT_LINJEDATA = "currentLinjedata";
	public static final String CURRENT_NAVKONTOR = "currentNAVKontor";
	public static final String CURRENT_VEDLEGG = "currentVedlegg";

	public static final String IS_WARNING_EXIT = "isWarningExit";
	public static final String BEHANDLET_COUNT = "behandletCount";
}
