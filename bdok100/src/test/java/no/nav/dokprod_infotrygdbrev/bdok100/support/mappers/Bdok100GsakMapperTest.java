package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.Journaldata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.GjelderType;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.BestillendeFagsystemCode;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.FagomradeCode;
import no.nav.dokprod_infotrygdbrev.consumer.FinnEllerOpprettSakTo;
import org.junit.Test;

import static no.nav.dokprod_infotrygdbrev.consumer.FinnEllerOpprettSakTo.BrukerType.ORGANISASJON;
import static no.nav.dokprod_infotrygdbrev.consumer.FinnEllerOpprettSakTo.BrukerType.PERSON;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link Bdok100GsakMapper}
 *
 */
public class Bdok100GsakMapperTest {

	private static final String FAGSAKID = "fagsakid";
	private static final String BRUKERID = "brukerid";
	private static final String SAKTYPE = "MFS";
	private static final FagomradeCode FAGOMRADE = FagomradeCode.AAP;
	private static final BestillendeFagsystemCode FAGSYSTEM = BestillendeFagsystemCode.IT01;

	private Bdok100GsakMapper gsakMapper = new Bdok100GsakMapper();

	@Test
	public void shouldMapOrg() throws Exception {
		FinnEllerOpprettSakTo to = gsakMapper.map(createJournaldata().gjelderType(GjelderType.ORGANISASJON).build());

		assertThat(to.getBrukerType(), is(ORGANISASJON));
		assertFields(to);
	}

	@Test
	public void shouldMapPerson() throws Exception {
		FinnEllerOpprettSakTo to = gsakMapper.map(createJournaldata().gjelderType(GjelderType.PERSON).build());

		assertThat(to.getBrukerType(), is(PERSON));
		assertFields(to);
	}

	private void assertFields(FinnEllerOpprettSakTo to) {
		assertThat(to.getFagsystem(), is(FAGSYSTEM));
		assertThat(to.getFagsystemSakId(), is(FAGSAKID));
		assertThat(to.getBrukerId(), is(BRUKERID));
		assertThat(to.getFagomraade(), is(FAGOMRADE));
		assertThat(to.getSakstype(), is(SAKTYPE));
	}

	private Journaldata.JournaldataBuilder createJournaldata() {
		return Journaldata.builder()
				.bestillendeFagsystemkode(FAGSYSTEM)
				.saksNummer(FAGSAKID)
				.gjelderID(BRUKERID)
				.dokumentTilhorendefagomraadekode(FAGOMRADE);
	}
}