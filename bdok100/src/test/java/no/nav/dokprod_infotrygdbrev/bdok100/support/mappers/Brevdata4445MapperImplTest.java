package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.LANDKODE_NORGE;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Spraak.B;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Spraak.E;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Spraak.N;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Spraak.U;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.AAR;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.ADRESSE_LINJE_1;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.ADRESSE_LINJE_2;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.ADRESSE_LINJE_3;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.ADRESSE_LINJE_4;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.AVSEND_MOTTAK_ID_ORG;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.BESOEKSADRESSE_1_NAV;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.BESOEKSADRESSE_2_NAV;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.BREVNAVN;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.BREVTEKST_1;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.DAG;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.FNR;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.FOOTER;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.GJELDER_ID;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.INNHOLD;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.MAANED;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.ORGANISASJON;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.POSTADRESSE_1_NAV;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.POSTADRESSE_2_NAV;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.POSTNR;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.POSTSTED_BESOEKSADRESSE_NAV;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.POSTSTED_NAV;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.POST_NR_BESOEKSADRESSE_NAV;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.POST_NR_NAV;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.SAKSBEHAND_NAVN;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.SAKS_NUMMER;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.SPRAAK;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.TELEFAKS_NAV;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.TELEFON_NAV;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.TKNR_1;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.TOPPARK_KONTROLLTEGN_1;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.TOPPARK_KONTROLLTEGN_2;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.TOPPARK_TEMA_FAGOMRAADE;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.T_K_NAME;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.createArbeidstabellRow;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.createJournaldata;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.createLinjedata;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.createNavKontor;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.BesoksadresseType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.Brevdata;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.FagType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.FellesType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.MottakerType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.MottakerTypeKode;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.PostadresseType;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.ReturadresseType;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe.Vedlegg;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Adressetype;
import no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.VedleggsLister;

/**
 * Unit test for {@link Brevdata4445MapperImpl}
 *
 */
public class Brevdata4445MapperImplTest {

	private static final String VEDLEGG_1 = "vedlegg1";
	private static final String VEDLEGG_2 = "vedlegg2";
	private static final String VEDLEGG_3 = "vedlegg3";
	private static final String BREVNAVN2 = "AA09";

	private Brevdata4445MapperImpl brevdataMapper = new Brevdata4445MapperImpl();

	private NAVKontors navKontors = new NAVKontors();
	private VedleggsLister vedleggsLister = new VedleggsLister();
	private VedleggsListe vedleggs2 = VedleggsListe.builder()
			.brevkode(BREVNAVN2).vedleggs(Lists.<Vedlegg>newArrayList()).build();

	@Before
	public void setUp() throws Exception {
		vedleggsLister.add(VedleggsListe.builder().brevkode(BREVNAVN)
				.vedleggs(Lists.newArrayList(new Vedlegg(VEDLEGG_1), new Vedlegg(VEDLEGG_2), new Vedlegg(VEDLEGG_3)))
				.build());
		vedleggsLister.add(vedleggs2);
		brevdataMapper.setVedleggs(vedleggsLister);

		navKontors.add(createNavKontor().build());
		navKontors.add(createNavKontor().tkNr("2103").build());
		brevdataMapper.setNavKontors(navKontors);
	}

	@Test
	public void shouldMap() throws Exception {
		Brevdata brevdata = brevdataMapper.map(createArbeidstabellRow().build());

		assertFelles(brevdata.getFelles());
		assertFag(brevdata.getFag());
	}

	private void assertFag(FagType fag) {
		assertThat(fag.getBrevDato(), is(DAG + MAANED + AAR));
		assertThat(fag.getTittel(), is(INNHOLD));
		assertThat(fag.getBrevinnholdListe().getBrevinnholds(), hasSize(1));
		assertThat(fag.getBrevinnholdListe().getBrevinnholds().get(0).getBrevtekst(), is(BREVTEKST_1));
		assertThat(fag.getVedleggsliste().getVedlegg1(), is(VEDLEGG_1));
		assertThat(fag.getVedleggsliste().getVedlegg2(), is(VEDLEGG_2));
		assertThat(fag.getVedleggsliste().getVedlegg3(), is(VEDLEGG_3));
		assertThat(fag.getSkanning().getOvreBarkode().getKontrollsiffer(), is(TOPPARK_KONTROLLTEGN_1));
		assertThat(fag.getSkanning().getNedreBarkode().getTemaKode(), is(TOPPARK_TEMA_FAGOMRAADE));
		assertThat(fag.getSkanning().getNedreBarkode().getEnhetId(), is(TKNR_1));
		assertThat(fag.getSkanning().getNedreBarkode().getKontrollsiffer(), is(TOPPARK_KONTROLLTEGN_2));
	}

	private void assertFelles(FellesType felles) {
		assertThat(felles.getSpraakkode(), is(SPRAAK.getSpraakKode()));
		assertThat(felles.getFagsaksnummer(), is(SAKS_NUMMER));
		assertThat(felles.getSignerendeSaksbehandler().getSignerendeSaksbehandlerNavn(), is(SAKSBEHAND_NAVN));
		assertThat(felles.getSakspart().getSakspartId(), is(GJELDER_ID));
		assertThat(felles.getSakspart().getSakspartTypeKode().value(), is(ORGANISASJON.name()));

		assertMottaker(felles);

		assertThat(felles.getNavnAvsenderEnhet(), is(T_K_NAME));
		assertThat(felles.getNummerAvsenderEnhet(), is(TKNR_1));
		assertThat(felles.getKontaktInformasjon().getKontaktTelefonnummer(), is(TELEFON_NAV));
		assertThat(felles.getKontaktInformasjon().getKontaktFaksnummer(), is(TELEFAKS_NAV));

		assertAdresser(felles);

		assertThat(felles.getBunntekst(), is(FOOTER));
	}

	private void assertAdresser(FellesType felles) {
		ReturadresseType returadresse = felles.getKontaktInformasjon().getReturadresse();
		assertThat(returadresse.getNavEnhetsNavn(), is(T_K_NAME));
		assertThat(returadresse.getAdresselinje(), is(POSTADRESSE_1_NAV + ", " + POSTADRESSE_2_NAV));
		assertThat(returadresse.getPostNr(), is(POST_NR_NAV));
		assertThat(returadresse.getPoststed(), is(POSTSTED_NAV));

		PostadresseType postadresse = felles.getKontaktInformasjon().getPostadresse();
		assertThat(postadresse.getNavEnhetsNavn(), is(T_K_NAME));
		assertThat(postadresse.getAdresselinje(), is(POSTADRESSE_1_NAV + ", " + POSTADRESSE_2_NAV));
		assertThat(postadresse.getPostNr(), is(POST_NR_NAV));
		assertThat(postadresse.getPoststed(), is(POSTSTED_NAV));

		BesoksadresseType besoksadresse = felles.getKontaktInformasjon().getBesoksadresse();
		assertThat(besoksadresse.getAdresselinje(), is(BESOEKSADRESSE_1_NAV + ", " + BESOEKSADRESSE_2_NAV));
		assertThat(besoksadresse.getPostNr(), is(POST_NR_BESOEKSADRESSE_NAV));
		assertThat(besoksadresse.getPoststed(), is(POSTSTED_BESOEKSADRESSE_NAV));
	}

	private void assertMottaker(FellesType felles) {
		assertThat(felles.getMottaker().getMottakerId(), is(AVSEND_MOTTAK_ID_ORG));
		assertThat(felles.getMottaker().getMottakerTypeKode(), is(MottakerTypeKode.ORGANISASJON));
		assertThat(felles.getMottaker().getMottakerNavn(), is(ADRESSE_LINJE_1));
		assertThat(felles.getMottaker().getMottakerAdresse().getAdresselinje1(), is(ADRESSE_LINJE_2));
		assertThat(felles.getMottaker().getMottakerAdresse().getAdresselinje2(), is(ADRESSE_LINJE_3));
		assertThat(felles.getMottaker().getMottakerAdresse().getAdresselinje3(), is(ADRESSE_LINJE_4));
		assertThat(felles.getMottaker().getMottakerAdresse().getPostNr(), is(POSTNR));
		assertThat(felles.getMottaker().getMottakerAdresse().getPoststed(), Matchers.is(Bdok100TestdataUtil.POSTSTED));
		assertThat(felles.getMottaker().getMottakerAdresse().getLand(), is(LANDKODE_NORGE));
	}

	@Test
	public void shouldMapMinimalAdresser() throws Exception {
		Brevdata brevdata = brevdataMapper.map(createArbeidstabellRow()
				.linjedata(createLinjedata()
						.adresseLinje3(null)
						.adresseLinje4(null)
						.build())
				.build());

		assertThat(brevdata.getFelles().getMottaker().getMottakerAdresse().getAdresselinje2(), nullValue());
		assertThat(brevdata.getFelles().getMottaker().getMottakerAdresse().getAdresselinje3(), nullValue());
	}

	@Test
	public void shouldMapNavAdresseIngenAdresse() throws Exception {
		navKontors.getNavKontor(TKNR_1).setPostadresse1(null);
		navKontors.getNavKontor(TKNR_1).setPostadresse2(null);
		navKontors.getNavKontor(TKNR_1).setBesoeksadresse1(null);
		navKontors.getNavKontor(TKNR_1).setBesoeksadresse2(null);
		Brevdata brevdata = brevdataMapper.map(createArbeidstabellRow().build());

		assertThat(brevdata.getFelles().getKontaktInformasjon().getPostadresse().getAdresselinje(), nullValue());
		assertThat(brevdata.getFelles().getKontaktInformasjon().getReturadresse().getAdresselinje(), nullValue());
		assertThat(brevdata.getFelles().getKontaktInformasjon().getBesoksadresse().getAdresselinje(), nullValue());
	}

	@Test
	public void shouldMapNavAdresseIngenAdresse1() throws Exception {
		navKontors.getNavKontor(TKNR_1).setPostadresse1(null);
		navKontors.getNavKontor(TKNR_1).setBesoeksadresse1(null);
		Brevdata brevdata = brevdataMapper.map(createArbeidstabellRow().build());

		assertThat(brevdata.getFelles().getKontaktInformasjon().getPostadresse().getAdresselinje(), is(POSTADRESSE_2_NAV));
		assertThat(brevdata.getFelles().getKontaktInformasjon().getReturadresse().getAdresselinje(), is(POSTADRESSE_2_NAV));
		assertThat(brevdata.getFelles().getKontaktInformasjon().getBesoksadresse().getAdresselinje(), is(BESOEKSADRESSE_2_NAV));
	}

	@Test
	public void shouldMapNavAdresseIngenAdresse2() throws Exception {
		navKontors.getNavKontor(TKNR_1).setPostadresse2(null);
		navKontors.getNavKontor(TKNR_1).setBesoeksadresse2(null);
		Brevdata brevdata = brevdataMapper.map(createArbeidstabellRow().build());

		assertThat(brevdata.getFelles().getKontaktInformasjon().getPostadresse().getAdresselinje(), is(POSTADRESSE_1_NAV));
		assertThat(brevdata.getFelles().getKontaktInformasjon().getReturadresse().getAdresselinje(), is(POSTADRESSE_1_NAV));
		assertThat(brevdata.getFelles().getKontaktInformasjon().getBesoksadresse().getAdresselinje(), is(BESOEKSADRESSE_1_NAV));
	}

	@Test
	public void shouldMapIngenVedlegg() throws Exception {
		Brevdata brevdata = brevdataMapper.map(createArbeidstabellRow().linjedata(createLinjedata().brevnavn(BREVNAVN2).build()).build());

		assertThat(brevdata.getFag().getVedleggsliste().getVedlegg1(), nullValue());
		assertThat(brevdata.getFag().getVedleggsliste().getVedlegg2(), nullValue());
		assertThat(brevdata.getFag().getVedleggsliste().getVedlegg3(), nullValue());
	}

	@Test
	public void shouldMap1Vedlegg() throws Exception {
		vedleggs2.getVedleggs().add(new Vedlegg(VEDLEGG_1));
		Brevdata brevdata = brevdataMapper.map(createArbeidstabellRow().linjedata(createLinjedata().brevnavn(BREVNAVN2).build()).build());

		assertThat(brevdata.getFag().getVedleggsliste().getVedlegg1(), is(VEDLEGG_1));
		assertThat(brevdata.getFag().getVedleggsliste().getVedlegg2(), nullValue());
		assertThat(brevdata.getFag().getVedleggsliste().getVedlegg3(), nullValue());
	}

	@Test
	public void shouldMap2Vedlegg() throws Exception {
		vedleggs2.getVedleggs().add(new Vedlegg(VEDLEGG_1));
		vedleggs2.getVedleggs().add(new Vedlegg(VEDLEGG_2));
		Brevdata brevdata = brevdataMapper.map(createArbeidstabellRow().linjedata(createLinjedata().brevnavn(BREVNAVN2).build()).build());

		assertThat(brevdata.getFag().getVedleggsliste().getVedlegg1(), is(VEDLEGG_1));
		assertThat(brevdata.getFag().getVedleggsliste().getVedlegg2(), is(VEDLEGG_2));
		assertThat(brevdata.getFag().getVedleggsliste().getVedlegg3(), nullValue());
	}

	@Test
	public void shouldMapSpraak() throws Exception {
		assertThat(brevdataMapper.map(createArbeidstabellRow().linjedata(createLinjedata()
				.spraak(B).build()).build()).getFelles().getSpraakkode(), is("NB"));
		assertThat(brevdataMapper.map(createArbeidstabellRow().linjedata(createLinjedata()
				.spraak(N).build()).build()).getFelles().getSpraakkode(), is("NN"));
		assertThat(brevdataMapper.map(createArbeidstabellRow().linjedata(createLinjedata()
				.spraak(E).build()).build()).getFelles().getSpraakkode(), is("EN"));
		assertThat(brevdataMapper.map(createArbeidstabellRow().linjedata(createLinjedata()
				.spraak(U).build()).build()).getFelles().getSpraakkode(), is("NB"));
	}

	@Test
	public void shouldSkipSkanningIfNoTopparkIndikator() throws Exception {
		Brevdata brevdata = brevdataMapper.map(createArbeidstabellRow().linjedata(createLinjedata()
				.topparkIndikator(null).build()).build());
		assertThat(brevdata.getFag().getSkanning(), nullValue());
	}

	@Test
	public void shouldSkipSkanningIfTKnr2103() throws Exception {
		Brevdata brevdata = brevdataMapper.map(createArbeidstabellRow().linjedata(createLinjedata()
				.tknr1("2103").build()).build());
		assertThat(brevdata.getFag().getSkanning(), nullValue());
	}

	@Test
	public void shouldMapUtland() throws Exception {
		Brevdata brevdata = brevdataMapper.map(createArbeidstabellRow().linjedata(createLinjedata()
						.land("Zimbabwe")
				.adressetype(Adressetype.UTENLANDSK).build()).build());
		MottakerType mottaker = brevdata.getFelles().getMottaker();

		assertThat(mottaker.getMottakerNavn(), is(ADRESSE_LINJE_1));
		assertThat(mottaker.getMottakerAdresse().getAdresselinje1(), is(ADRESSE_LINJE_2));
		assertThat(mottaker.getMottakerAdresse().getAdresselinje2(), is(ADRESSE_LINJE_3));
		assertThat(mottaker.getMottakerAdresse().getAdresselinje3(), is(ADRESSE_LINJE_4));
		assertThat(mottaker.getMottakerAdresse().getPostNr(), nullValue());
		assertThat(mottaker.getMottakerAdresse().getPoststed(), nullValue());
		assertThat(mottaker.getMottakerAdresse().getLand(), is("Zimbabwe"));
	}

	@Test
	public void shouldMapPerson() throws Exception {
		Brevdata brevdata = brevdataMapper.map(createArbeidstabellRow().journaldata(createJournaldata()
				.avsendMottakID(FNR).build()).build());
		MottakerType mottaker = brevdata.getFelles().getMottaker();

		assertThat(mottaker.getMottakerId(), is(FNR));
		assertThat(mottaker.getMottakerTypeKode(), is(MottakerTypeKode.PERSON));
	}
}