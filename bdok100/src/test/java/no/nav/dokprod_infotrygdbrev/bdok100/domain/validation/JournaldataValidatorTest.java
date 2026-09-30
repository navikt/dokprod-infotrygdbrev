package no.nav.dokprod_infotrygdbrev.bdok100.domain.validation;

import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DistKanal.S;
import static no.nav.dokprod_infotrygdbrev.bdok100.domain.validation.InvalidRecord.INVALID_FORMAT;

import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Journaldata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Linjedata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Adressetype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Brevtype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DistKanal;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.GjelderType;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Spraak;
import no.nav.dokprod_infotrygdbrev.bdok100.support.mappers.JournaldataMapper;
import no.nav.dokprod_infotrygdbrev.common.exception.AvviksfilException;
import no.nav.dokprod_infotrygdbrev.common.exception.BeanValidationException;
import no.nav.dokprod_infotrygdbrev.common.exception.JournaldataException;
import no.nav.dokprod_infotrygdbrev.common.exception.JournaldataFormatException;
import no.nav.dokprod_infotrygdbrev.common.file.FileReader;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.BestillendeFagsystemCode;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.FagomradeCode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Unit test for {@link JournaldataValidator}
 *
 */
public class JournaldataValidatorTest {

	private static final String FOLDER = "bdok100/journaldata/";
	private static final String JOURNALDATA_INVALID_RECORDS = "invalid_records.txt";
	private static final String TKNR_1 = "1111";
	private static final String TKNR_2 = "1111";

	private JournaldataValidator validator = new JournaldataValidator();

	private List<String> invalidRecords;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		invalidRecords = readLines(JOURNALDATA_INVALID_RECORDS, Bdok100Constants.BDOK100_INPUT_CHARSET);
	}

	@Test
	public void shouldValidate() throws Exception {
		validator.validate(createEntries());
	}

	@Test
	public void shouldValidateIfUtenlandskAdresslinje2NullOgLocalPrint() throws Exception {
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(DistKanal.L).build())
				.linjedata(createLinjedataUtenlandsk().build()).build());
	}

	@Test
	public void throwWhenUtenlandskAdresslinje2NullOgNotLocalPrint() {
		exception.expect(AvviksfilException.class);
		exception.expectMessage("Utenlandsk adresse mangler adresselinje");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(DistKanal.S).build())
				.linjedata(createLinjedataUtenlandsk().build()).build());
	}


	@Test
	public void throwsWhenInvalidFormat() throws Exception {
		exception.expect(JournaldataFormatException.class);
		exception.expectMessage("Invalid formatted record");

		validator.validate(getFailingEntries(INVALID_FORMAT.getLN()));
	}

	@Test
	public void throwsForAvbrutt() throws Exception {
		exception.expect(JournaldataException.class);
		exception.expectMessage("Brev skal ikke legges til brevbestilling");

		Bdok100ArbTbl bdok100ArbTbl = createEntries();
		bdok100ArbTbl.getJournaldata().setJournalStatus("A");
		validator.validate(bdok100ArbTbl);
	}

	@Test
	public void throwsForFagsystemNotIT01() throws Exception {
		exception.expect(JournaldataException.class);
		exception.expectMessage("Wrong Bestillendefagsystemkode: AO01");

		Bdok100ArbTbl bdok100ArbTbl = createEntries();
		bdok100ArbTbl.getJournaldata().setBestillendeFagsystemkode(BestillendeFagsystemCode.AO01);
		validator.validate(bdok100ArbTbl);
	}



	@Test
	public void throwsForKategoriNotB() throws Exception {
		exception.expect(JournaldataException.class);
		exception.expectMessage("Wrong Kategori: F");

		Bdok100ArbTbl bdok100ArbTbl = createEntries();
		bdok100ArbTbl.getJournaldata().setKategori("F");
		validator.validate(bdok100ArbTbl);
	}

	@Test
	public void throwsForDokumentTypeNotU() throws Exception {
		exception.expect(JournaldataException.class);
		exception.expectMessage("Wrong DokumentType: I");

		Bdok100ArbTbl bdok100ArbTbl = createEntries();
		bdok100ArbTbl.getJournaldata().setDokumentType("I");
		validator.validate(bdok100ArbTbl);
	}

	@Test
	public void throwsMissingSaksNummer() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("SaksNummer");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).saksNummer(null).build()).build());
	}

	@Test
	public void throwsMissingBestFag() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("BestillendeFagsystemkode");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).bestillendeFagsystemkode(null).build()).build());
	}

	@Test
	public void throwsMissingFagomraade() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("DokumentTilhorendefagomraadekode");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).dokumentTilhorendefagomraadekode(null).build()).build());
	}

	@Test
	public void throwsMissingJournEnhet() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("JournalfEnhet");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).journalfEnhet(null).build()).build());
	}

	@Test
	public void throwsMissingSaksbehNavn() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("SaksbehandNavn");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).saksbehandNavn(null).build()).build());
	}

	@Test
	public void throwsMissingJournalstatus() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("JournalStatus");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).journalStatus(null).build()).build());
	}

	@Test
	public void throwsMissingGjelderId() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("GjelderID");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).gjelderID(null).build()).build());
	}

	@Test
	public void throwsMissingGjelderType() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("GjelderType");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).gjelderType(null).build()).build());
	}

	@Test
	public void throwsMissingAvsMotId() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("AvsendMottakID");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).avsendMottakID(null).build()).build());
	}

	@Test
	public void throwsMissingAvsMot() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("AvsendMottaker");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).avsendMottaker(null).build()).build());
	}

	@Test
	public void throwsMissingDokType() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("DokumentType");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).dokumentType(null).build()).build());
	}

	@Test
	public void throwsMissingKategori() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("Kategori");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).kategori(null).build()).build());
	}

	@Test
	public void throwsMissingDistKanal() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("FaktDistrKanal");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).faktDistrKanal(null).build()).build());
	}

	@Test
	public void throwsWhenBrevtittelNull() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("BrevTittel");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).innhold(null).build()).build());
	}

	@Test
	public void throwsWhenBrevtittelEmptyText() throws Exception {
		exception.expect(BeanValidationException.class);
		exception.expectMessage("BrevTittel");
		validator.validate(Bdok100ArbTbl.builder().journaldata(journaldata(S).innhold("").build()).build());
	}

	private Journaldata.JournaldataBuilder journaldata(DistKanal distKanal) {
		return Journaldata.builder()
				.onDemandInstans("INFOT_UT")
				.saksNummer("0106B01")
				.bestillendeFagsystemkode(BestillendeFagsystemCode.IT01)
				.dokumentTilhorendefagomraadekode(FagomradeCode.FOS)
				.journalfEnhet("0106")
				.saksbehandlerId("ABC123")
				.saksbehandNavn("Ukjent")
				.journalStatus("FS")
				.gjelderID("11111111111")
				.gjelderType(GjelderType.PERSON)
				.innhold("Innhold")
				.avsendMottakID("11111111111")
				.avsendMottaker("TESTESEN")
				.dokumentType("U")
				.kategori("B")
				.brevKode("F003")
				.faktDistrKanal(distKanal)
				.sensitivt(true)
				.elektroniskDistr(false);
	}

	private Linjedata.LinjedataBuilder createLinjedataUtenlandsk() {
		return Linjedata.builder()
				.tknr1(TKNR_1)
				.fnr("12345678901")
				.aar("01").maaned("04").dag("14")
				.postnr("0000")
				.poststed("horten")
				.brevtype(Brevtype.OK)
				.topparkIndikator("A").topparkKontrolltegn1("1").topparkKontrolltegn2("2").topparkTemaFagomraade("KE")
				.brevnavn("KF10")
				.tknr2(TKNR_2)
				.spraak(Spraak.E)
				.infotrygdBrevkodePage("XXXXX_YY")
				.adressetype(Adressetype.UTENLANDSK)
				.adresseLinje1("TEST")
				.adresseLinje4("USA")
				.footer("Footy");
	}

	private Bdok100ArbTbl createEntries() {
		return Bdok100ArbTbl.builder()
				.journaldata(journaldata(S).build())
				.linjedata(Linjedata.builder().adressetype(Adressetype.NORSK).build()).build();
	}

	private Bdok100ArbTbl getFailingEntries(int linenumber) {
		Bdok100ArbTbl arbTbl = Bdok100ArbTbl.builder()
				.idnr("ODIQ210102100001")
				.journalforingsfil(invalidRecords.get(linenumber))
				.build();
		return new JournaldataMapper().map(arbTbl);
	}

	private static List<String> readLines(String file, Charset charset) throws IOException {
		return FileReader.readLines(new ClassPathResource(FOLDER + file).getFile(), charset);
	}
}
