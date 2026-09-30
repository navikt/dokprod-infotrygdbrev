package no.nav.dokprod_infotrygdbrev.bdok100.support;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;

/**
 * JPA2 implementation of PingRepository
 *
 */
public class Jpa2PingRepository /* implements PingRepository */ {

	@Autowired
	private EntityManager entityManager;

	// @Override
	public Long countRowsInBatchJobInstance() {
		Number number = (Number) entityManager
				.createNativeQuery("select count(*) from BATCH_JOB_INSTANCE")
				.getSingleResult();
		return number.longValue();
	}
}
