package no.nav.dokprod_infotrygdbrev.bdok100.domain;

import static no.nav.dokprod_infotrygdbrev.Regexes.NOT_CONTROL_CHAR;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Nav Kontor BDOK100
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NAVKontor {

	@NotNull
	@Digits(integer = 4, fraction = 0)
	private String tkNr;

	@NotNull
	@Pattern(regexp = NOT_CONTROL_CHAR + "{1,}")
	private String tKName;

	@Pattern(regexp = NOT_CONTROL_CHAR + "{0,9}")
	private String orgNummer;

	@Pattern(regexp = NOT_CONTROL_CHAR + "{0,11}")
	private String bankGiroRef;

	@Pattern(regexp = NOT_CONTROL_CHAR + "{0,11}")
	private String postGiroRef;

	@Pattern(regexp = NOT_CONTROL_CHAR + "{0,}")
	private String postadresse1;

	@Pattern(regexp = NOT_CONTROL_CHAR + "{0,}")
	private String postadresse2;

	@NotNull
	@Digits(integer = 4, fraction = 0)
	private String postNr;

	@NotNull
	@Pattern(regexp = NOT_CONTROL_CHAR + "{1,}")
	private String poststed;

	@Pattern(regexp = NOT_CONTROL_CHAR + "{0,}")
	private String besoeksadresse1;

	@Pattern(regexp = NOT_CONTROL_CHAR + "{0,}")
	private String besoeksadresse2;

	@Pattern(regexp = NOT_CONTROL_CHAR + "{0,4}")
	private String postNrBesoeksadresse;

	@Pattern(regexp = NOT_CONTROL_CHAR + "{0,}")
	private String poststedBesoeksadresse;

	@Pattern(regexp = NOT_CONTROL_CHAR + "{0,9}")
	private String telefon;

	@Pattern(regexp = NOT_CONTROL_CHAR + "{0,9}")
	private String telefaks;

	@Pattern(regexp = NOT_CONTROL_CHAR + "{0,}")
	private String aapningstid1;

	@Pattern(regexp = NOT_CONTROL_CHAR + "{0,}")
	private String aapningstid2;

	@Pattern(regexp = NOT_CONTROL_CHAR + "{0,}")
	private String aapningstid3;
}
