package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Journaldata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Journaldata.JournaldataBuilder;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DistKanal;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.GjelderType;
import no.nav.dokprod_infotrygdbrev.common.exception.JournaldataException;
import no.nav.dokprod_infotrygdbrev.common.exception.JournaldataFormatException;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.BestillendeFagsystemCode;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.FagomradeCode;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.apache.commons.lang3.StringUtils.containsNone;

/**
 * Mapper for Journaldata
 */
public class JournaldataMapper {

	private static final int MAX_SPLIT_OPERATIONS = 30;

	private static final String SEPARATOR = ",";

	// Brevkoder som hindrer digital distribusjon. Kun print distribusjon.
	private static final String KOPIBREV_KODE = "ZY";
	private static final String ZB99 = "ZB99";
	private static final String ZTR6 = "ZTR6";
	private static final String ZJ86 = "ZJ86";
	private static final String KOPIMOTTAKER_ID = "999999999";
	private static final String KOPIMOTTAKER_NAVN = "KOPIMOTTAKER";

	/**
	 * Constructs an instance of {@link Journaldata} and adds to {@link Bdok100ArbTbl} record
	 *
	 * @param record
	 */
	public Bdok100ArbTbl map(Bdok100ArbTbl record) {
		Journaldata journaldata = new Mapper(record.getJournalforingsfil()).map();
		record.setJournaldata(journaldata);
		return record;
	}

	private static class Mapper {
		private JournaldataBuilder builder = Journaldata.builder();
		private String[] entries;

		Mapper(String record) {
			if (containsNone(record, SEPARATOR)) {
				throw new JournaldataFormatException("Invalid formatted record");
			}
			entries = record.trim().split(String.valueOf(SEPARATOR), MAX_SPLIT_OPERATIONS);
		}

		public Journaldata map() {
			int index = 1;

			Journaldata journaldata = builder
					.onDemandInstans(entries[index++])
					.saksNummer(entries[index++])
					.bestillendeFagsystemkode(BestillendeFagsystemCode.valueOf(entries[index++]))
					.dokumentTilhorendefagomraadekode(FagomradeCode.valueOf(entries[index++]))
					.journalfEnhet(entries[index++])
					.saksbehandlerId(entries[index++])
					.saksbehandNavn(entries[index++])
					.journalStatus(entries[index++])
					.datoFerdig(parseDate(entries[index++], "datoFerdig", false))
					.gjelderID(entries[index++])
					.gjelderType(GjelderType.valueOf(entries[index++]))
					.innhold(entries[index++])
					.avsendMottakID(entries[index++])
					.avsendMottaker(entries[index++])
					.datoDokument(parseDate(entries[index++], "datoDokument", true))
					.dokumentType(entries[index++])
					.kategori(entries[index++])
					.brevKode(entries[index++])
					.brevGruppe(entries[index++])
					.faktDistrKanal(DistKanal.map(entries[index++]))
					.sensitivt(parseBoolean(entries[index++]))
					.elektroniskDistr(parseBoolean(entries[index++]))
					.datoJournal(parseDate(entries[index++], "datoJournal", true))
					.datoSendtPrint(parseDate(entries[index], "datoSendtPrint", true))
					.build();

			if (isBrevkodeSomIkkeDistribueresDigitalt(journaldata)) {
				journaldata.setAvsendMottakID(KOPIMOTTAKER_ID);
				journaldata.setAvsendMottaker(KOPIMOTTAKER_NAVN);
			}

			return journaldata;
		}

		// Fiks for problemer med utsending av kopi brev (PK-41955), utvidet i MMA-6206, MMA-6340
		// Konsekvensen av dette er at brevene alltid blir sendt til PRINT i stedet for digitale kanaler
		private boolean isBrevkodeSomIkkeDistribueresDigitalt(Journaldata journaldata) {
			final String brevKode = journaldata.getBrevKode();
			return brevKode.startsWith(KOPIBREV_KODE) ||
					brevKode.equals(ZB99) ||
					brevKode.equals(ZTR6) ||
					brevKode.equals(ZJ86);
		}

		private LocalDateTime parseDate(String dateAsString, String fieldName, boolean acceptNull) {
			if (acceptNull && StringUtils.isBlank(dateAsString)) {
				return null;
			}
			try {
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				Date parsedDate = df.parse(dateAsString);
				return parsedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			} catch (ParseException e) {
				throw new JournaldataException(fieldName + " should be on the form yyyy-MM-dd, but was " + dateAsString);
			}
		}

		private Boolean parseBoolean(String entry) {
			Boolean aBoolean = BooleanUtils.toBooleanObject(entry);
			if (aBoolean == null) {
				throw new JournaldataException("Could not parse entry '" + entry + "' into a valid Boolean");
			}
			return aBoolean;
		}
	}
}
