package no.nav.dokprod_infotrygdbrev.bdok100;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.BDOK100_AVVIKSRAPPORT_FILENAME_FORMAT;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.BDOK100_AVVIK_JFF_FILENAME_FORMAT;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.BDOK100_AVVIK_LPF_FILENAME_FORMAT;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.BDOK100_KONTROLLRAPPORT_FILENAME_FORMAT;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.BDOK100_OUT_XML_FILENAME_FORMAT;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.FILE_DATE_FORMAT;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.FILE_DATE_FORMAT_MS;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Filename functions in BDOK100
 *
 */
public class FilenameHelper {

	public static final String LINJEDATA_TXT = "linjedata.txt";
	public static final String JOURNALDATA_CSV = "journaldata.csv";
	public static final String VEDLEGG_CSV = "vedlegg.csv";
	public static final String KONTOR_CSV = "kontor.csv";

	public static String getOutXmlFilename(Date startTime) {
		return String.format(BDOK100_OUT_XML_FILENAME_FORMAT, formatDateToSeconds(startTime), formatNow());
	}

	public static String getLPFAvvikFilename(Date startTime) {
		return String.format(BDOK100_AVVIK_LPF_FILENAME_FORMAT, formatDateToSeconds(startTime));
	}

	public static String getJFFAvvikFilename(Date startTime) {
		return String.format(BDOK100_AVVIK_JFF_FILENAME_FORMAT, formatDateToSeconds(startTime));
	}

	public static String getAvviksrapport(Date startTime) {
		return String.format(BDOK100_AVVIKSRAPPORT_FILENAME_FORMAT, formatDateToSeconds(startTime));
	}

	public static String getKontrollrapport(Date startTime) {
		return String.format(BDOK100_KONTROLLRAPPORT_FILENAME_FORMAT, formatDateToSeconds(startTime), formatNow());
	}

	public static String formatDateToSeconds(Date startTime) {
		return new SimpleDateFormat(FILE_DATE_FORMAT).format(startTime);
	}

	public static String formatNow() {
		return new SimpleDateFormat(FILE_DATE_FORMAT_MS).format(new Date());
	}
}
