package no.nav.dokprod_infotrygdbrev.bdok100.repo;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Spraak;

/**
 * Holder object for count queries in Bdok100Repo. Holds a field name value and a count.
 *
 */
public class CountPair {

	private String name;
	private long count;

	public CountPair(String name, Spraak spraak, long count) {
		this.name = name + spraak;
		this.count = count;
	}

	public String getName() {
		return name;
	}

	public long getCount() {
		return count;
	}
}
