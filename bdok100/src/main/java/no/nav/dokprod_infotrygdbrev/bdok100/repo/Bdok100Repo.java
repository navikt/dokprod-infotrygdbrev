package no.nav.dokprod_infotrygdbrev.bdok100.repo;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Spring Data JPA Repo for BDOK100
 *
 */
public interface Bdok100Repo extends JpaRepository<Bdok100ArbTbl, String> {

	List<Bdok100ArbTbl> findByStatus(Bdok100Status status);

	long countByLineprintBrevIsNotNull();

	long countByJournalforingsfilIsNotNull();

	long countByStatus(Bdok100Status status);

	@Query("SELECT count(a) from Bdok100ArbTbl a where a.linjedata.tknr1 like '23%'")
	long countByTknr1StartingWith23();

	@Query("SELECT count(a) from Bdok100ArbTbl a where CAST(a.linjedata.tknr1 as integer) between 2400 and 4399 " +
			"or CAST(a.linjedata.tknr1 as integer) between 4500 and 4529 " +
			"or CAST(a.linjedata.tknr1 as integer) between 4531 and 4699 " +
			"or CAST(a.linjedata.tknr1 as integer) > 4899")
	long countInvalidTkNr1();

	@Query("SELECT count(a) from Bdok100ArbTbl a where a.linjedata.brevnavn like 'ZH2%' " +
			"OR a.linjedata.brevnavn like 'ZE2%' " +
			"OR a.linjedata.brevnavn like 'Z10%' " +
			"OR a.linjedata.brevnavn like 'Z20%' " +
			"OR a.linjedata.brevnavn like 'Z30%' " +
			"OR a.linjedata.brevnavn like 'Z40%' " +
			"OR a.linjedata.brevnavn like 'ZH3%' " +
			"OR a.linjedata.brevnavn like 'ZE3%' ")
	long countLocalBrevkode();

	@Query("SELECT count(a) from Bdok100ArbTbl a where a.linjedata.infotrygdBrevkodePage = 'FLXXX_YY'")
	long countByInfotrygdBrevkodePageEqualToFLXXXYY();

	@Query("SELECT count(a) from Bdok100ArbTbl a where a.linjedata.brevtype <> 'OK' and a.lineprintBrev is not null")
	long countByBrevtypeNotOk();

	@Query("SELECT new no.nav.dokprod_infotrygdbrev.bdok100.repo.CountPair(a.linjedata.brevnavn, a.linjedata.spraak, count(a)) " +
			"from Bdok100ArbTbl a where a.lineprintBrev is not null group by a.linjedata.brevnavn, a.linjedata.spraak")
	List<CountPair> countByLinjedataBrevkode();
}
