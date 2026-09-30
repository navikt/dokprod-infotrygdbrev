package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Adressetype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DistKanal;
import no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.NorskPostadresse;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.Organisasjon;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.Person;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.ProduserIkkeRedigerbartDokument;
import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.UtenlandskPostadresse;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.SimpleDateFormat;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.FS_22;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.LANDKODE_NORGE;
import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.UKJENT_LANDKODE_UTLAND;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DokumenttypeId._000044;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DokumenttypeId._000045;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DokumenttypeId._000046;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DokumenttypeId._000249;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.GjelderType.PERSON;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.ADRESSE_LINJE_2;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.ADRESSE_LINJE_3;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.ADRESSE_LINJE_4;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.AVSEND_MOTTAK_ID_ORG;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.AVSEND_MOTTAK_NAVN_ORG;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.AVSEND_MOTTAK_NAVN_PERS;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.BESTILLENDE_FAGSYSTEM_CODE;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.DOKUMENT_TILHORENDEFAGOMRAADEKODE;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.GJELDER_ID;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.INNHOLD;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.JOURNALF_ENHET;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.POSTNR;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.POSTSTED;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.SAKSBEHAND_NAVN;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.ProduserIkkeRedigerbartDokumentXmlMapper.BDOK100_PREFIX;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProduserIkkeRedigerbartDokumentXmlMapperTest {

	private static final String UNKNOWN = "Unknown";
	private static final String AVSEND_MOTTAK_ID_PERSON = "01234567895";

	@Mock
	private BrevdataMapper brevdataMapper;

	@InjectMocks
	private ProduserIkkeRedigerbartDokumentXmlMapper mapper;

	@Test
	public void shouldMap() throws Exception {
		when(brevdataMapper.map(any(Bdok100ArbTbl.class))).thenReturn("brevdata");
		Bdok100ArbTbl bdok100ArbTbl = createArbeidstabellRow();
		ProduserIkkeRedigerbartDokument map = mapper.map(bdok100ArbTbl);

		assertThat(map.getDokumentbestillingsinformasjon().getBatchId(), is(BDOK100_PREFIX + new SimpleDateFormat("yyyyMMdd").format(LocalDate.now().toDate())));
		assertThat(map.getDokumentbestillingsinformasjon().getBestillingsId(), is(BDOK100_PREFIX + bdok100ArbTbl.getIdnr()));

		assertThat(map.getDokumentbestillingsinformasjon().getUstrukturertTittel(), is(INNHOLD));
		assertThat(map.getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000044.toString()));
		assertThat(map.getDokumentbestillingsinformasjon().getBestillendeFagsystemkode(), is(BESTILLENDE_FAGSYSTEM_CODE.name()));

		assertThat(map.getDokumentbestillingsinformasjon().getBruker(), instanceOf(Organisasjon.class));
		assertThat(((Organisasjon)map.getDokumentbestillingsinformasjon().getBruker()).getNavn(), is(UNKNOWN));
		assertThat(((Organisasjon) map.getDokumentbestillingsinformasjon().getBruker()).getOrgnummer(), is(GJELDER_ID));

		assertThat(map.getDokumentbestillingsinformasjon().getMottaker(), instanceOf(Organisasjon.class));
		assertThat(((Organisasjon)map.getDokumentbestillingsinformasjon().getMottaker()).getNavn(), is(AVSEND_MOTTAK_NAVN_ORG));
		assertThat(((Organisasjon) map.getDokumentbestillingsinformasjon().getMottaker()).getOrgnummer(), is(AVSEND_MOTTAK_ID_ORG));

		assertThat(map.getDokumentbestillingsinformasjon().getArkivsak().getJournalsakId(), is(bdok100ArbTbl.getSaksID()));
		assertThat(map.getDokumentbestillingsinformasjon().getArkivsak().getSakstilhoerendeFagsystemkode(), is(FS_22));

		assertThat(map.getDokumentbestillingsinformasjon().getDokumenttilhoerendeFagomraadekode(), is(DOKUMENT_TILHORENDEFAGOMRAADEKODE.name()));
		assertThat(map.getDokumentbestillingsinformasjon().getJournalfoerendeEnhet(), is(JOURNALF_ENHET));
		assertThat(map.getDokumentbestillingsinformasjon().getSaksbehandlernavn(), is(SAKSBEHAND_NAVN));

		assertThat(map.getDokumentbestillingsinformasjon().getAdresse(), instanceOf(NorskPostadresse.class));
		assertThat(((NorskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje1(), is(ADRESSE_LINJE_2));
		assertThat(((NorskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje2(), is(ADRESSE_LINJE_3));
		assertThat(((NorskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje3(), is(ADRESSE_LINJE_4));
		assertThat(((NorskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getPoststed(), is(POSTSTED));
		assertThat(((NorskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getPostnummer(), is(POSTNR));
		assertThat(((NorskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getLand(), is(LANDKODE_NORGE));

		assertThat((String)map.getBrevdata(), is("brevdata"));
		verify(brevdataMapper).map(bdok100ArbTbl);
	}

	@Test
	public void shouldMapDoktype44() throws Exception {
		assertThat(mapper.map(createArbeidstabellRow("AA08")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000044.toString()));
		assertThat(mapper.map(createArbeidstabellRow("AG60")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000044.toString()));
		assertThat(mapper.map(createArbeidstabellRow("AG61")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000044.toString()));
		assertThat(mapper.map(createArbeidstabellRow("HT20")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000044.toString()));
		assertThat(mapper.map(createArbeidstabellRow("HT22")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000044.toString()));
		assertThat(mapper.map(createArbeidstabellRow("HT24")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000044.toString()));
		assertThat(mapper.map(createArbeidstabellRow("HT25")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000044.toString()));
		assertThat(mapper.map(createArbeidstabellRow("HT44")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000044.toString()));
	}

	@Test
	public void shouldMapDoktype45() throws Exception {
		assertThat(mapper.map(createArbeidstabellRow("AA07")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000045.toString()));
		assertThat(mapper.map(createArbeidstabellRow("AA09")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000045.toString()));
		assertThat(mapper.map(createArbeidstabellRow("AG63")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000045.toString()));
		assertThat(mapper.map(createArbeidstabellRow("AG50")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000045.toString()));
		assertThat(mapper.map(createArbeidstabellRow("HT21")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000045.toString()));
		assertThat(mapper.map(createArbeidstabellRow("HT26")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000045.toString()));
		assertThat(mapper.map(createArbeidstabellRow("HT43")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000045.toString()));
		assertThat(mapper.map(createArbeidstabellRow("HT45")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000045.toString()));
		assertThat(mapper.map(createArbeidstabellRow("AA10")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000045.toString()));
		assertThat(mapper.map(createArbeidstabellRow("AG59")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000045.toString()));
	}

	@Test
	public void shouldMapDoktype249() {
		assertThat(mapper.map(createArbeidstabellRow("SP02")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP03")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP04")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP05")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP06")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP07")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP08")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP12")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP22")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP23")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP24")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP25")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP50")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP51")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP98")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("SP99")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("S313")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("S322")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("S334")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("S338")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("S340")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("S341")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("S342")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("S352")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("S354")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("OTA6")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("QTKA")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
		assertThat(mapper.map(createArbeidstabellRow("QTK8")).getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000249.toString()));
	}

	@Test
	public void shouldMapDoktype46() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = createArbeidstabellRow();
		bdok100ArbTbl.getJournaldata().setFaktDistrKanal(DistKanal.L);
		ProduserIkkeRedigerbartDokument map = mapper.map(bdok100ArbTbl);
		assertThat(map.getDokumentbestillingsinformasjon().getDokumenttypeId(), is(_000046.toString()));
	}

	@Test
	public void shouldMapPerson() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = createArbeidstabellRow();
		bdok100ArbTbl.getJournaldata().setGjelderType(PERSON);
		bdok100ArbTbl.getJournaldata().setAvsendMottakID(AVSEND_MOTTAK_ID_PERSON);
		bdok100ArbTbl.getJournaldata().setAvsendMottaker(AVSEND_MOTTAK_NAVN_PERS);
		ProduserIkkeRedigerbartDokument map = mapper.map(bdok100ArbTbl);

		assertThat(map.getDokumentbestillingsinformasjon().getBruker(), instanceOf(Person.class));
		assertThat(((Person)map.getDokumentbestillingsinformasjon().getBruker()).getNavn(), is(UNKNOWN));
		assertThat(((Person) map.getDokumentbestillingsinformasjon().getBruker()).getPersonidentifikator(), is(GJELDER_ID));

		assertThat(map.getDokumentbestillingsinformasjon().getMottaker(), instanceOf(Person.class));
		assertThat(((Person)map.getDokumentbestillingsinformasjon().getMottaker()).getNavn(), is(AVSEND_MOTTAK_NAVN_PERS));
		assertThat(((Person) map.getDokumentbestillingsinformasjon().getMottaker()).getPersonidentifikator(), is(AVSEND_MOTTAK_ID_PERSON));
	}

	@Test
	public void shouldMapUtland() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = createArbeidstabellRow();
		bdok100ArbTbl.getLinjedata().setLand("Portugal");
		bdok100ArbTbl.getLinjedata().setAdressetype(Adressetype.UTENLANDSK);
		ProduserIkkeRedigerbartDokument map = mapper.map(bdok100ArbTbl);

		assertThat(map.getDokumentbestillingsinformasjon().getAdresse(), instanceOf(UtenlandskPostadresse.class));
		assertThat(((UtenlandskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje1(), is(ADRESSE_LINJE_2));
		assertThat(((UtenlandskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje2(), is(ADRESSE_LINJE_3));
		assertThat(((UtenlandskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje3(), is(ADRESSE_LINJE_4));
		assertThat(((UtenlandskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getLand(), is("PT"));
	}

	@Test
	public void shouldMapUtlandWhenLandnavnPartofLinjedata() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = createArbeidstabellRow();
		bdok100ArbTbl.getLinjedata().setLand("LT-11111 KAUNAS, LITHUANIA");
		bdok100ArbTbl.getLinjedata().setAdressetype(Adressetype.UTENLANDSK);
		ProduserIkkeRedigerbartDokument map = mapper.map(bdok100ArbTbl);

		assertThat(map.getDokumentbestillingsinformasjon().getAdresse(), instanceOf(UtenlandskPostadresse.class));
		assertThat(((UtenlandskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje1(), is(ADRESSE_LINJE_2));
		assertThat(((UtenlandskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje2(), is(ADRESSE_LINJE_3));
		assertThat(((UtenlandskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje3(), is(ADRESSE_LINJE_4));
		assertThat(((UtenlandskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getLand(), is("LT"));
	}

	@Test
	public void shouldMapUtlandWhenUkjentLand() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = createArbeidstabellRow();
		bdok100ArbTbl.getLinjedata().setLand("Bakvendtland");
		bdok100ArbTbl.getLinjedata().setAdressetype(Adressetype.UTENLANDSK);
		ProduserIkkeRedigerbartDokument map = mapper.map(bdok100ArbTbl);

		assertThat(map.getDokumentbestillingsinformasjon().getAdresse(), instanceOf(UtenlandskPostadresse.class));
		assertThat(((UtenlandskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje1(), is(ADRESSE_LINJE_2));
		assertThat(((UtenlandskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje2(), is(ADRESSE_LINJE_3));
		assertThat(((UtenlandskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getAdresselinje3(), is(ADRESSE_LINJE_4));
		assertThat(((UtenlandskPostadresse) map.getDokumentbestillingsinformasjon().getAdresse()).getLand(), is(UKJENT_LANDKODE_UTLAND));
	}

	public static Bdok100ArbTbl createArbeidstabellRow() {
		return Bdok100TestdataUtil.createArbeidstabellRow().build();
	}

	public static Bdok100ArbTbl createArbeidstabellRow(String brevnavn) {
		Bdok100ArbTbl bdok100ArbTbl = Bdok100TestdataUtil.createArbeidstabellRow().build();
		bdok100ArbTbl.getLinjedata().setBrevnavn(brevnavn);
		return bdok100ArbTbl;
	}
}