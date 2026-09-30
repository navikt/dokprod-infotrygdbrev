package no.nav.dokprod_infotrygdbrev.bdok100.kodeverk;

/**
 * Kodeverk for BestillendeFagsystem
 *
 * Kopiert fra Dokprod
 */
public enum BestillendeFagsystemCode {

	AO01("ARENA"),
	IT01("IT"),
	FS36("FS36"),
	FS22("GOSYS"),
	OB30("RAY"),
	OB36("UR"),
	AT05("PESYS"),
	FS38("MELOSYS"),
	BD11("VEILARB"),
	K9("K9");

	/**
	 * The corresponding code in dokdist, dokdist has different representation
	 * of the same code (will be changed when we start using 'felles kodeverk')
	 */
	private String dokdistCode;

	BestillendeFagsystemCode(String dokdistCode) {
		this.dokdistCode = dokdistCode;
	}

	public String getDokdistCode() {
		return dokdistCode;
	}

}
