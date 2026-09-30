package no.nav.dokprod_infotrygdbrev.bdok100.support;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.BDOK100_INPUT_CHARSET;

import lombok.SneakyThrows;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Brevtekst;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Journaldata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Linjedata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.NAVKontor;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Adressetype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Brevtype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DistKanal;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.GjelderType;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Spraak;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.BestillendeFagsystemCode;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.FagomradeCode;
import no.nav.dokprod_infotrygdbrev.common.file.FileReader;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Testdata for BDOK100
 *
 */
public class Bdok100TestdataUtil {

	// NB these files do not match testdata in this file
	public static final String LINJEDATA_MED_TOPPTEKST = "med_topptekst.crlf.txt";
	public static final String LINJEDATA_MED_TOPPTEKST_NORGE = "med_topptekst_norge.crlf.txt";
	public static final String LINJEDATA_MED_BREVTEKST2 = "med_brevtekst2.crlf.txt";
	private static final String LINJEDATA_FOLDER = "bdok100/linjedata/";
	private static final String JOURNALDATA_FOLDER = "bdok100/journaldata/";
	public static final String FOR_LITE_MELLOMROM_MELLOM_ADRESSE_OG_BREVTEKST = "med_topptekst_for_lite_mellomrom.crlf.txt";

	public static final String INNHOLD = "innhold-tittel";
	public static final BestillendeFagsystemCode BESTILLENDE_FAGSYSTEM_CODE = BestillendeFagsystemCode.IT01;
	public static final String GJELDER_ID = "123456789";
	public static final String AVSEND_MOTTAK_ID_ORG = "123456788";
	public static final String SAKS_ID = "94845613";
	public static final FagomradeCode DOKUMENT_TILHORENDEFAGOMRAADEKODE = FagomradeCode.AGR;
	public static final String JOURNALF_ENHET = "journalenhet";
	public static final String SAKSBEHAND_NAVN = "saksbehnvn";
	public static final String POSTSTED = "Bærum";
	public static final String POSTNR = "1221";
	public static final String ADRESSE_LINJE_1 = "addr1";
	public static final String ADRESSE_LINJE_2 = "addr2";
	public static final String ADRESSE_LINJE_3 = "addr3";
	public static final String ADRESSE_LINJE_4 = "addr4";
	public static final String AVSEND_MOTTAK_NAVN_ORG = "mottakerorgnavn";
	public static final String AVSEND_MOTTAK_NAVN_PERS = "mottakerpersnavn";
	public static final GjelderType ORGANISASJON = GjelderType.ORGANISASJON;
	public static final DistKanal SENTRALPRINT = DistKanal.S;
	public static final Spraak SPRAAK = Spraak.B;
	public static final String SAKS_NUMMER = "1244672";
	public static final String BREVNAVN = "AA08";
	public static final String ID_NR = "ODIQ013010900049";
	public static final String BREVTEKST_1 = "Brevtekst1";
	public static final String FOOTER = "footer\nwee";
	public static final String FNR = "45678912300";
	public static final String TKNR_1 = "0152";
	public static final String TKNR_2 = "0152";
	public static final String INFOTRYGD_BREVKODE_PAGE = "XXXXX_YY";
	public static final Brevtype BREVTYPE = Brevtype.OK;
	public static final String DAG = "15";
	public static final String MAANED = "04";
	public static final String AAR = "05";

	public static final String T_K_NAME = "HALDEN kontor";
	public static final String ORG_NUMMER_NAV = "987654321";
	public static final String POSTADRESSE_1_NAV = "POSTBOKS 0123";
	public static final String POSTADRESSE_2_NAV = "Postgirobygget";
	public static final String POST_NR_NAV = "1751";
	public static final String POSTSTED_NAV = "HALDEN";
	public static final String BESOEKSADRESSE_1_NAV = "KIRKEGATA 3";
	public static final String BESOEKSADRESSE_2_NAV = "Besøksporten";
	public static final String POST_NR_BESOEKSADRESSE_NAV = "1751";
	public static final String POSTSTED_BESOEKSADRESSE_NAV = "HALDEN";
	public static final String TELEFAKS_NAV = "43215678";
	public static final String TELEFON_NAV = "54321678";
	public static final String AAPNINGSTID_1_NAV = "09.00-14.30";
	public static final String AAPNINGSTID_2_NAV = "TELEFONVAKT";
	public static final String AAPNINGSTID_3_NAV = "09.00-14.30";
	public static final String TOPPARK_INDIKATOR = "Y";
	public static final String TOPPARK_KONTROLLTEGN_1 = "B";
	public static final String TOPPARK_KONTROLLTEGN_2 = "C";
	public static final String TOPPARK_TEMA_FAGOMRAADE = "KE";
	public static final String JOURNAL_STATUS = "D";
	public static final String DOKUMENT_TYPE = "U";
	public static final String KATEGORI = "B";

	public static Linjedata.LinjedataBuilder createLinjedata() {
		return Linjedata.builder()
				.aar(AAR)
				.maaned(MAANED)
				.dag(DAG)
				.infotrygdBrevkodePage(INFOTRYGD_BREVKODE_PAGE)
				.brevtype(BREVTYPE)
				.tknr1(TKNR_1)
				.footer(FOOTER)
				.fnr(FNR)
				.brevnavn(BREVNAVN)
				.tknr2(TKNR_2)
				.spraak(SPRAAK)
				.adresseLinje1(ADRESSE_LINJE_1)
				.adresseLinje2(ADRESSE_LINJE_2)
				.adresseLinje3(ADRESSE_LINJE_3)
				.adresseLinje4(ADRESSE_LINJE_4)
				.adressetype(Adressetype.NORSK)
				.postnr(POSTNR)
				.poststed(POSTSTED)
				.topparkIndikator(TOPPARK_INDIKATOR)
				.topparkKontrolltegn1(TOPPARK_KONTROLLTEGN_1)
				.topparkTemaFagomraade(TOPPARK_TEMA_FAGOMRAADE)
				.topparkKontrolltegn2(TOPPARK_KONTROLLTEGN_2);
	}

	public static Bdok100ArbTbl.Bdok100ArbTblBuilder createArbeidstabellRow(String id) {
		return Bdok100ArbTbl.builder()
				.idnr(id)
				.saksID(SAKS_ID)
				.brevtekst(Bdok100Brevtekst.builder().rekkefolge(0).innhold(BREVTEKST_1).build())
				.linjedata(createLinjedata().build())
				.journaldata(createJournaldata().build());
	}

	public static Bdok100ArbTbl.Bdok100ArbTblBuilder createArbeidstabellRow() {
		return createArbeidstabellRow(ID_NR);
	}

	public static Journaldata.JournaldataBuilder createJournaldata() {
		return Journaldata.builder()
				.faktDistrKanal(SENTRALPRINT)
				.innhold(INNHOLD)
				.bestillendeFagsystemkode(BESTILLENDE_FAGSYSTEM_CODE)
				.dokumentTilhorendefagomraadekode(DOKUMENT_TILHORENDEFAGOMRAADEKODE)
				.gjelderType(ORGANISASJON)
				.gjelderID(GJELDER_ID)
				.avsendMottakID(AVSEND_MOTTAK_ID_ORG)
				.avsendMottaker(AVSEND_MOTTAK_NAVN_ORG)
				.journalfEnhet(JOURNALF_ENHET)
				.saksbehandNavn(SAKSBEHAND_NAVN)
				.saksNummer(SAKS_NUMMER)
				.journalStatus(JOURNAL_STATUS)
				.dokumentType(DOKUMENT_TYPE)
				.kategori(KATEGORI);
	}


	public static NAVKontor.NAVKontorBuilder createNavKontor() {
		return NAVKontor.builder()
				.tkNr(TKNR_1)
				.tKName(T_K_NAME)
				.orgNummer(ORG_NUMMER_NAV)
				.postadresse1(POSTADRESSE_1_NAV)
				.postadresse2(POSTADRESSE_2_NAV)
				.postNr(POST_NR_NAV)
				.poststed(POSTSTED_NAV)
				.besoeksadresse1(BESOEKSADRESSE_1_NAV)
				.besoeksadresse2(BESOEKSADRESSE_2_NAV)
				.postNrBesoeksadresse(POST_NR_BESOEKSADRESSE_NAV)
				.poststedBesoeksadresse(POSTSTED_BESOEKSADRESSE_NAV)
				.telefon(TELEFON_NAV)
				.telefaks(TELEFAKS_NAV)
				.aapningstid1(AAPNINGSTID_1_NAV)
				.aapningstid2(AAPNINGSTID_2_NAV)
				.aapningstid3(AAPNINGSTID_3_NAV);
	}

	public static String readLinjedataFile(String file) {
		return readFile(LINJEDATA_FOLDER + file, BDOK100_INPUT_CHARSET);
	}

	public static List<String> readJournaldataFile(String file) {
		return readLines(JOURNALDATA_FOLDER + file, BDOK100_INPUT_CHARSET);
	}

	@SneakyThrows
	public static String readFile(String file, Charset charset) {
		Path path = new ClassPathResource(file).getFile().toPath();
		return new String(Files.readAllBytes(path), charset);
	}

	@SneakyThrows
	public static List<String> readLines(String file, Charset charset) {
		return FileReader.readLines(new ClassPathResource(file).getFile(), charset);
	}
}
