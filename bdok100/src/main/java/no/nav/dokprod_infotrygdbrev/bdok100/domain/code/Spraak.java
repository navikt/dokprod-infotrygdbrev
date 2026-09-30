package no.nav.dokprod_infotrygdbrev.bdok100.domain.code;

/**
 * Spraakkoder
 *
 */
public enum Spraak {
	B("NB"),
	N("NN"),
	E("EN"),
	U("NB");

	private String spraakKode;

	Spraak(String spraakKode) {
		this.spraakKode = spraakKode;
	}

	public String getSpraakKode() {
		return spraakKode;
	}
}
