package no.nav.dokprod_infotrygdbrev.bdok100.domain.code;

/**
 * DokumenttypeId for BDOK100
 *
 */
public enum DokumenttypeId {
	_000044,
	_000045,
	_000046,
	_000249;

	@Override
	public String toString() {
		return name().substring(1);
	}
}
