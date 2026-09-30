package no.nav.dokprod_infotrygdbrev.bdok100.repo;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Status;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Brevtype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Spraak;
import no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil;
import no.nav.dokprod_infotrygdbrev.config.BatchTestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * Itest for {@link Bdok100Repo}
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {BatchTestConfig.class})
@ActiveProfiles("itest")
@Transactional
public class Bdok100RepoTest {

	@Autowired
	private Bdok100Repo repo;

	@Before
	public void setUp() throws Exception {
		repo.deleteAll();
	}

	@Test
	public void shouldSaveAndFind() throws Exception {
		Bdok100ArbTbl beforeSave = Bdok100TestdataUtil
				.createArbeidstabellRow()
				.build();
		repo.saveAndFlush(beforeSave);

		Bdok100ArbTbl afterSave = repo.findById(Bdok100TestdataUtil.ID_NR).get();
		assertThat(afterSave, notNullValue());

		assertThat(afterSave, equalTo(beforeSave));
		assertThat(afterSave.getBrevtekster(), hasSize(1));
	}

	@Test
	public void shouldCountLineprintBrev() throws Exception {
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow()
				.lineprintBrev(null)
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900050")
				.lineprintBrev("test")
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900051")
				.lineprintBrev("test")
				.build());

		assertThat(repo.countByLineprintBrevIsNotNull(), equalTo(2L));
	}

	@Test
	public void shouldCountJournalforingsfilBrev() throws Exception {
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow()
				.journalforingsfil(null)
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900050")
				.journalforingsfil("test")
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900051")
				.journalforingsfil("test")
				.build());

		assertThat(repo.countByJournalforingsfilIsNotNull(), equalTo(2L));
	}

	@Test
	public void shouldCountByStatusBehandlet() throws Exception {
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow()
				.status(Bdok100Status.BEHANDLET)
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900050")
				.status(Bdok100Status.BEHANDLET)
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900051")
				.status(Bdok100Status.FEILET_GSAK)
				.build());

		assertThat(repo.countByStatus(Bdok100Status.BEHANDLET), equalTo(2L));
	}

	@Test
	public void shouldCountByStatusFeilet() throws Exception {
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow()
				.status(Bdok100Status.UNDER_INNLESNING_JFF)
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900050")
				.status(Bdok100Status.FEILET_GSAK)
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900051")
				.status(Bdok100Status.FEILET_GSAK)
				.build());

		assertThat(repo.countByStatus(Bdok100Status.FEILET_GSAK), equalTo(2L));
	}

	@Test
	public void shouldCountByTknr23() throws Exception {
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow()
				.linjedata(Bdok100TestdataUtil.createLinjedata()
						.tknr1("2321")
						.build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900050")
				.linjedata(Bdok100TestdataUtil.createLinjedata()
						.tknr1("2421")
						.build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900051")
				.linjedata(Bdok100TestdataUtil.createLinjedata()
						.tknr1("2321")
						.build())
				.build());

		assertThat(repo.countByTknr1StartingWith23(), equalTo(2L));
	}

	@Test
	public void shouldCountInvalidTknr1() throws Exception {
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow()
				.linjedata(Bdok100TestdataUtil.createLinjedata()
						.tknr1("4529")
						.build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900050")
				.linjedata(Bdok100TestdataUtil.createLinjedata()
						.tknr1("4530")
						.build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900051")
				.linjedata(Bdok100TestdataUtil.createLinjedata()
						.tknr1("4531")
						.build())
				.build());

		assertThat(repo.countInvalidTkNr1(), equalTo(2L));
	}

	@Test
	public void shouldCountByBrevkode() throws Exception {
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow()
				.linjedata(Bdok100TestdataUtil.createLinjedata().brevnavn("A001").spraak(Spraak.B).build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow()
				.lineprintBrev("test")
				.linjedata(Bdok100TestdataUtil.createLinjedata().brevnavn("A001").spraak(Spraak.B).build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900050")
				.lineprintBrev("test")
				.linjedata(Bdok100TestdataUtil.createLinjedata().brevnavn("A001").spraak(Spraak.B).build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900051")
				.lineprintBrev("test")
				.linjedata(Bdok100TestdataUtil.createLinjedata().brevnavn("A001").spraak(Spraak.N).build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900052")
				.lineprintBrev("test")
				.linjedata(Bdok100TestdataUtil.createLinjedata().brevnavn("B001").spraak(Spraak.B).build())
				.build());

		List<CountPair> countPairs = repo.countByLinjedataBrevkode();
		assertThat(countPairs.size(), equalTo(3));

		CountPair a001b = countPairs.get(0);
		assertThat(a001b.getName(), is("A001B"));
		assertThat(a001b.getCount(), equalTo(2L));

		CountPair a001n = countPairs.get(1);
		assertThat(a001n.getName(), is("A001N"));
		assertThat(a001n.getCount(), equalTo(1L));

		CountPair b001b = countPairs.get(2);
		assertThat(b001b.getName(), is("B001B"));
		assertThat(b001b.getCount(), equalTo(1L));
	}

	@Test
	public void shouldCountByBrevtypeNotOk() throws Exception {
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow()
				.lineprintBrev("test")
				.linjedata(Bdok100TestdataUtil.createLinjedata().brevtype(Brevtype.OK).build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900050")
				.lineprintBrev("test")
				.linjedata(Bdok100TestdataUtil.createLinjedata().brevtype(Brevtype.BP).build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900051")
				.lineprintBrev("test")
				.linjedata(Bdok100TestdataUtil.createLinjedata().brevtype(Brevtype.OK).build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900052")
				.linjedata(Bdok100TestdataUtil.createLinjedata().brevtype(Brevtype.OK).build())
				.build());

		assertThat(repo.countByBrevtypeNotOk(), equalTo(1L));
	}

	@Test
	public void shouldCountByInfotrygdBrevkodePageEqualToFLXXXYY() throws Exception {
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow()
				.linjedata(Bdok100TestdataUtil.createLinjedata().infotrygdBrevkodePage("FLXXX_YY").build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900050")
				.linjedata(Bdok100TestdataUtil.createLinjedata().infotrygdBrevkodePage("AK000_02").build())
				.build());
		repo.saveAndFlush(Bdok100TestdataUtil
				.createArbeidstabellRow("ODIQ013010900051")
				.linjedata(Bdok100TestdataUtil.createLinjedata().infotrygdBrevkodePage("FLXXX_YY").build())
				.build());

		assertThat(repo.countByInfotrygdBrevkodePageEqualToFLXXXYY(), equalTo(2L));
	}
}