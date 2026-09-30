package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.CRLF;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.POSTNR_FALLBACK;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Brevtekst;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Linjedata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Linjedata.LinjedataBuilder;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Adressetype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Brevtype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Spraak;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Maps one row of linjedata to domain object
 *
 */
public class LinjedataMapper {

	private static final int MINIMUM_ROWS_PER_LINJEDATA = 2;
	private static final int LINE1_MIN_SIZE = 125;
	private static final int MAX_N_ADRESS_LINES = 5;

	private static final int LINE_1 = 0;
	private static final int[] PURRE_ADDRESS_START_LINE = {1, 2, 3, 4, 5, 6, 7, 8};
	private static final int ADDRESS_MAX_START_LINE = 9;
	private static final int PREFIX_LENGTH = 1;
	private static final char LINJEDATA_PREFIX = '1';
	private static final String FOOTER_PREFIX = "2";
	private static final char NEW_PAGE_PREFIX = 'N';
	private static final String NORGE = "NORGE";

	private static final String BREVKODE_MAP_TO_BREVTYPE_OK = "AG60";

	public Bdok100ArbTbl map(Bdok100ArbTbl bdok100ArbTbl) {
		Linjedata linjedata = new Mapper(bdok100ArbTbl).map();
		bdok100ArbTbl.setLinjedata(linjedata);
		cleanUpValues(linjedata);
		return bdok100ArbTbl;
	}

	private void cleanUpValues(Linjedata linjedata) {
		if (BREVKODE_MAP_TO_BREVTYPE_OK.equals(linjedata.getBrevnavn())) {
			linjedata.setBrevtype(Brevtype.OK);
		}
	}

	private static class Mapper {

		private final Bdok100ArbTbl bdok100ArbTbl;
		private LinjedataBuilder builder = Linjedata.builder();
		private List<String> lines;
		private String line1;
		private Adressetype adressetype;

		Mapper(Bdok100ArbTbl bdok100ArbTbl) {
			this.bdok100ArbTbl = bdok100ArbTbl;
			lines = Lists.newArrayList(Splitter.on(CRLF)
					.split(bdok100ArbTbl.getLineprintBrev()).iterator());

			Assert.isTrue(lines.size() > MINIMUM_ROWS_PER_LINJEDATA - 1,
					"Linjedata is too small, minimum " + MINIMUM_ROWS_PER_LINJEDATA + " - " + bdok100ArbTbl.getLineprintBrev());

			line1 = lines.get(LINE_1);
		}

		public Linjedata map() {
			parseLine1();
			int addressEnd = parseAdresse();

			List<List<String>> brevtekster = parseBrevtekst(addressEnd);
			int brevtekstSize = 0;
			for (List<String> brevtekst : brevtekster) {
				brevtekstSize += brevtekst.size();
			}
			int brevEnd = addressEnd + brevtekstSize;
			int footEnd = parseFooter(brevEnd);

			brevtekster.addAll(parseBrevtekst(footEnd));
			stripEmptyLinesFromEndsAndRemoveEmptyTekster(brevtekster);
			List<Bdok100Brevtekst> brevteksterList = new ArrayList<>();
			if (!brevtekster.isEmpty()) {
				int rekkefolge = 0;
				for (List<String> brevtekst : brevtekster) {
					final String brevtekstJoined = Joiner.on(CRLF).join(brevtekst);
					brevteksterList.add(Bdok100Brevtekst.builder()
							.rekkefolge(rekkefolge)
							.innhold(brevtekstJoined)
							.build());
					rekkefolge++;
				}
			}
			bdok100ArbTbl.setBrevtekster(brevteksterList);

			return builder.build();
		}

		private static void stripEmptyLinesFromEndsAndRemoveEmptyTekster(List<List<String>> brevtekster) {
			for (Iterator<List<String>> iterator = brevtekster.iterator(); iterator.hasNext(); ) {
				List<String> brevtekst = iterator.next();
				while (!brevtekst.isEmpty() && StringUtils.isBlank(brevtekst.get(0))) {
					brevtekst.remove(0);
				}
				int n = brevtekst.size() - 1;
				while (n > 0 && StringUtils.isBlank(brevtekst.get(n))) {
					brevtekst.remove(n--);
				}
				if (brevtekst.isEmpty()) {
					iterator.remove();
				}
			}
		}

		private int parseFooter(int brevEnd) {
			List<String> footer = Lists.newArrayList();
			int n = brevEnd;
			while (n < lines.size() && lines.get(n).startsWith(FOOTER_PREFIX)) {
				String line = lines.get(n++);
				footer.add(line.substring(PREFIX_LENGTH));
			}
			builder.footer(Joiner.on(CRLF).join(footer));
			return n;
		}

		private List<List<String>> parseBrevtekst(int start) {
			List<List<String>> brevtekster = Lists.newArrayList();
			List<String> brevtekst = Lists.newArrayList();
			int n = start;
			while (n < lines.size() && !lines.get(n).startsWith(FOOTER_PREFIX)) {
				String line = lines.get(n++);
				if (line.length() > 0) {
					if (line.charAt(0) == NEW_PAGE_PREFIX && !brevtekst.isEmpty()) {
						brevtekster.add(brevtekst);
						brevtekst = Lists.newArrayList();
					}
					line = line.substring(PREFIX_LENGTH);
				}
				brevtekst.add(line);
			}
			brevtekster.add(brevtekst);
			return brevtekster;
		}

		private int parseAdresse() {
			int n = ADDRESS_MAX_START_LINE;
			// Sjekker om adresse starter på linje 2-7 ettersom purrebrev typisk har start
			// på linje 2-4 og det finnes tilfeller der adressen starter på linje 5-7
			for (int lineNum : PURRE_ADDRESS_START_LINE) {
				if (lineNum < lines.size() && StringUtils.isNotBlank(lines.get(lineNum)) && ignoreDateLine(lines.get(lineNum))) {
					n = lineNum;
					break;
				}
			}

			List<String> adresseLinjer = Lists.newArrayList();
			while (n < lines.size() && StringUtils.isNotBlank(lines.get(n))) {
				adresseLinjer.add(lines.get(n++).trim());
				if (n + 1 < lines.size() && StringUtils.isBlank(lines.get(n)) && StringUtils.isNotBlank(lines.get(n + 1))) {
					adresseLinjer.add("");
					n++;
				}
			}

			Assert.isTrue(adresseLinjer.size() <= MAX_N_ADRESS_LINES, "Too many address lines");

			while (n < lines.size() && StringUtils.isBlank(lines.get(n))) {
				n++;
			}

			mapAdresse(adresseLinjer);
			return n;
		}

		// Sjekk for dato som ignoreres
		private boolean ignoreDateLine(String line) {
			return line.indexOf(line.trim()) <= 5;
		}

		// Eksempel norsk adresse
		// adresseLinje(0) - "MAX MEKKER"
		// adresseLinje(1) - "SESAM STASJON 1"
		// adresseLinje(2) - "1461 LØRENSKOG"
		private void mapAdresse(List<String> adresseLinjer) {
			if (!adresseLinjer.isEmpty()) {
				String last = adresseLinjer.get(adresseLinjer.size() - 1);
				Adressetype adressetype = checkAdressetypeInAdresselinje(last);
				builder.adressetype(adressetype);
				if (adressetype == Adressetype.NORSK) {
					if (last.equalsIgnoreCase(NORGE)) {
						last = adresseLinjer.get(adresseLinjer.size() - 2);
						adresseLinjer.remove(adresseLinjer.size() - 1);
					}
					builder.postnr(last.substring(0, 4).trim());
					builder.poststed(last.substring(4).trim());
					// Fjerner siste adresselinje slik at man ikke får postnr poststed to ganger etter hverandre i dokumentet.
					adresseLinjer.remove(adresseLinjer.size() - 1);
				} else {
					builder.postnr(POSTNR_FALLBACK);
					builder.land(last);
					// Fjerner siste adresselinje slik at man ikke får land to ganger etter hverandre i dokumentet.
					// Metada om land blir flettet inn i utenlandske dokument. Se Brevdata4445MapperImpl.java
					adresseLinjer.remove(adresseLinjer.size() - 1);
				}
			}

			// Check since it might be empty after removal last item above
			if (!adresseLinjer.isEmpty()) {
				builder.adresseLinje1(adresseLinjer.get(0));
				if (adresseLinjer.size() > 1) {
					builder.adresseLinje2(adresseLinjer.get(1));
					if (adresseLinjer.size() > 2) {
						builder.adresseLinje3(adresseLinjer.get(2));
						if (adresseLinjer.size() > 3) {
							builder.adresseLinje4(adresseLinjer.get(3));
						}
					}
				}
			}
		}

		// Ekstra sjekk for å sette AdresseType ettersom det finnes tilfeller med mismatch i første linje
		private Adressetype checkAdressetypeInAdresselinje(String adresseLinje) {
			return (adresseLinje.matches("^\\d{4}.+") || adresseLinje.equalsIgnoreCase(NORGE)) ? Adressetype.NORSK : Adressetype.UTENLANDSK;
		}

		private void parseLine1() {
			Assert.isTrue(line1.length() >= LINE1_MIN_SIZE, "line1 too short");
			Assert.isTrue(line1.charAt(0) == LINJEDATA_PREFIX, "linjedata doesnt start with proper prefix" + LINJEDATA_PREFIX);

			String postnr = read(51, 55);
			if (StringUtils.isBlank(postnr) || !postnr.matches("\\d{4}") || POSTNR_FALLBACK.equals(postnr)) {
				adressetype = Adressetype.UTENLANDSK;
				builder.postnr(POSTNR_FALLBACK);
			} else {
				adressetype = Adressetype.NORSK;
				builder.postnr(postnr);
			}
			builder
					.aar(read(6, 8))
					.maaned(read(8, 10))
					.dag(read(10, 12))
					.tknr1(read(17, 21))
					.fnr(read(21, 32))
					.legeFnr(read(40, 51))
					.adressetype(adressetype)
					.brevtype(Brevtype.valueOf(read(55, 57)))

					.topparkIndikator(read(85, 86))
					.topparkPostboksMottat(read(86, 90))
					.topparkKontrolltegn1(read(90, 91))
					.topparkTemaFagomraade(read(91, 93))
					.topparkKontrolltegn2(read(93, 94))

					.brevnavn(read(94, 98))
					.spraak(readLanguage(98, 99))
					.tknr2(read(112, 116))
					.infotrygdBrevkodePage(read(117, 125))
			;
			if (StringUtils.isBlank(bdok100ArbTbl.getIdnr())) {
				bdok100ArbTbl.setIdnr(read(1, 17));
			}
		}

		private Spraak readLanguage(int beginIndex, int endIndex) {
			String languageCode = read(beginIndex, endIndex);

			if (StringUtils.isNotEmpty(languageCode)) {
				for (Spraak spraak : Spraak.values()) {
					if (spraak.toString().equals(languageCode)) {
						return spraak;
					}
				}
			}
			return Spraak.U;
		}

		private String read(int beginIndex, int endIndex) {
			return read(line1, beginIndex, endIndex);
		}

		private static String read(String string, int beginIndex, int endIndex) {
			String value = string.substring(beginIndex, endIndex);
			return StringUtils.isBlank(value) ? null : value;
		}
	}

}
