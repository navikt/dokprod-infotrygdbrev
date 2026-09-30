package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.NAVKontor;
import org.apache.commons.lang3.StringUtils;

/**
 * Mapper for Nav Kontor CSV file
 *
 */
public class NAVKontorMapper {

	private static final String SEPARATOR = ";";
	private static final int NUMBER_OF_COLUMNS = 22;

	public static NAVKontor map(String vedleggLine) {
		String[] columns = vedleggLine.split(SEPARATOR, NUMBER_OF_COLUMNS);

		if (columns.length < NUMBER_OF_COLUMNS) {
			return null;
		}

		for (int i = 0; i < columns.length; i++) {
			columns[i] = StringUtils.strip(columns[i]);
		}

		return NAVKontor.builder()
				.tkNr(columns[0])
				.tKName(columns[1])
				.orgNummer(columns[2])
				.bankGiroRef(columns[3])
				.postGiroRef(columns[4])
				// two unmapped columns
				.postadresse1(columns[7])
				.postadresse2(columns[8])
				.postNr(columns[9])
				.poststed(columns[10])
				.besoeksadresse1(columns[11])
				.besoeksadresse2(columns[12])
				.postNrBesoeksadresse(columns[13])
				.poststedBesoeksadresse(columns[14])
				.telefon(columns[15])
				.telefaks(columns[16])
				.aapningstid1(columns[17])
				.aapningstid2(columns[18])
				.aapningstid3(columns[19])
				// two unmapped columns
				.build();
	}

}
