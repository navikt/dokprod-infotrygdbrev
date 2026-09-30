package no.nav.dokprod_infotrygdbrev.bdok100.domain.validation;

public enum InvalidRecord {
	INVALID_FORMAT(0),
	MISSING_FIELDS(1),
	MISSING_ONDEMAND(2),
	MISSING_CONDITIONAL(3),
	INVALID_DATE(4),
	INVALID_BOOLEAN(5);

	private int linenumber;

	InvalidRecord(int linenumber) {
		this.linenumber = linenumber;
	}

	public int getLN() {
		return this.linenumber;
	}
}
