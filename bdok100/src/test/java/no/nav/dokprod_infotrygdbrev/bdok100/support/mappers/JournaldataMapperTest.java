package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Journaldata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.DistKanal;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.GjelderType;
import no.nav.dokprod_infotrygdbrev.common.exception.JournaldataException;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.BestillendeFagsystemCode;
import no.nav.dokprod_infotrygdbrev.bdok100.kodeverk.FagomradeCode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDate;
import java.util.List;

import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.readJournaldataFile;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit test for {@link JournaldataMapper}
 */
public class JournaldataMapperTest {

	private static final String JOURNALDATA_VALID = "valid_records.txt";
	private static final String JOURNALDATA_INVALID_RECORDS = "invalid_records.txt";

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private JournaldataMapper mapper = new JournaldataMapper();

	private List<String> validRecords;
	private List<String> invalidRecords;

	@Before
	public void setUp() throws Exception {
		validRecords = readJournaldataFile(JOURNALDATA_VALID);
		invalidRecords = readJournaldataFile(JOURNALDATA_INVALID_RECORDS);
	}

	@Test
	public void throwsWhenInvalidDateFormat() throws Exception {

		exception.expect(JournaldataException.class);
		exception.expectMessage("datoFerdig should be on the form yyyy-MM-dd, but was \"2010-10-21\"");

		Bdok100ArbTbl record = Bdok100ArbTbl.builder()
				.journalforingsfil(invalidRecords.get(4))
				.build();

		mapper.map(record);
	}

	@Test
	public void throwsWhenInvalidBoolean() throws Exception {

		exception.expect(JournaldataException.class);
		exception.expectMessage("Could not parse entry 'Falsed' into a valid Boolean");

		Bdok100ArbTbl record = Bdok100ArbTbl.builder()
				.journalforingsfil(invalidRecords.get(5))
				.build();

		mapper.map(record);
	}

	@Test
	public void shouldMapToDummyOrgForPrintDistribusjonWhenBrevkodeStartsWithZY() throws Exception {
		Bdok100ArbTbl record = Bdok100ArbTbl.builder()
				.journalforingsfil(validRecords.get(8))
				.build();
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(record);
		Journaldata journaldata = bdok100ArbTbl.getJournaldata();
		assertThat(journaldata.getOnDemandInstans(), is("INFOT_UT"));
		assertThat(journaldata.getSaksNummer(), is("0106B01"));
		assertThat(journaldata.getBestillendeFagsystemkode(), is(BestillendeFagsystemCode.IT01));
		assertThat(journaldata.getDokumentTilhorendefagomraadekode(), is(FagomradeCode.FOS));
		assertThat(journaldata.getJournalfEnhet(), is("0106"));
		assertThat(journaldata.getSaksbehandlerId(), is("ABC123"));
		assertThat(journaldata.getSaksbehandNavn(), is("Ukjent"));
		assertThat(journaldata.getJournalStatus(), is("A"));
		assertThat(journaldata.getDatoFerdig().equals(LocalDate.parse("2010-10-21").atStartOfDay()), is(true));
		assertThat(journaldata.getGjelderID(), is("11111111111"));
		assertThat(journaldata.getGjelderType(), is(GjelderType.PERSON));
		assertThat(journaldata.getInnhold(), is("Innhold"));

		assertThat(journaldata.getAvsendMottakID(), is("999999999"));

		assertThat(journaldata.getAvsendMottaker(), is("KOPIMOTTAKER"));
		assertThat(journaldata.getDatoDokument(), is(nullValue()));
		assertThat(journaldata.getDokumentType(), is("U"));
		assertThat(journaldata.getKategori(), is("B"));
		assertThat(journaldata.getBrevKode(), is("ZY01"));
		assertThat(journaldata.getBrevGruppe(), is(""));
		assertThat(journaldata.getFaktDistrKanal(), is(DistKanal.S));
		assertThat(journaldata.getSensitivt(), is(true));
		assertThat(journaldata.getElektroniskDistr(), is(false));
		assertThat(journaldata.getDatoJournal().equals(LocalDate.parse("2010-10-05").atStartOfDay()), is(true));
		assertThat(journaldata.getDatoSendtPrint().equals(LocalDate.parse("2010-10-21").atStartOfDay()), is(true));
	}

	@Test
	public void shouldMapToDummyOrgForPrintDistribusjonWhenBrevkodeEqualToSelection() {
		List.of(9, 10, 11).forEach((i) -> {
			Bdok100ArbTbl record = Bdok100ArbTbl.builder()
					.journalforingsfil(validRecords.get(i))
					.build();
			Bdok100ArbTbl bdok100ArbTbl = mapper.map(record);
			Journaldata journaldata = bdok100ArbTbl.getJournaldata();
			assertThat(journaldata.getAvsendMottakID(), is("999999999"));
			assertThat(journaldata.getAvsendMottaker(), is("KOPIMOTTAKER"));
		});
	}

	@Test
	public void shouldMap() throws Exception {
		Bdok100ArbTbl record = Bdok100ArbTbl.builder()
				.journalforingsfil(validRecords.get(5))
				.build();
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(record);
		Journaldata journaldata = bdok100ArbTbl.getJournaldata();

		assertThat(journaldata.getOnDemandInstans(), is("INFOT_UT"));
		assertThat(journaldata.getSaksNummer(), is("0106B01"));
		assertThat(journaldata.getBestillendeFagsystemkode(), is(BestillendeFagsystemCode.AO01));
		assertThat(journaldata.getDokumentTilhorendefagomraadekode(), is(FagomradeCode.FOS));
		assertThat(journaldata.getJournalfEnhet(), is("0106"));
		assertThat(journaldata.getSaksbehandlerId(), is("ABC123"));
		assertThat(journaldata.getSaksbehandNavn(), is("Ukjent"));
		assertThat(journaldata.getJournalStatus(), is("FS"));
		assertThat(journaldata.getDatoFerdig().equals(LocalDate.parse("2010-10-21").atStartOfDay()), is(true));
		assertThat(journaldata.getGjelderID(), is("11111111111"));
		assertThat(journaldata.getGjelderType(), is(GjelderType.PERSON));
		assertThat(journaldata.getInnhold(), is("Innhold"));
		assertThat(journaldata.getAvsendMottakID(), is("11111111111"));
		assertThat(journaldata.getAvsendMottaker(), is("TESTESEN"));
		assertThat(journaldata.getDatoDokument(), is(nullValue()));
		assertThat(journaldata.getDokumentType(), is("U"));
		assertThat(journaldata.getKategori(), is("B"));
		assertThat(journaldata.getBrevKode(), is("FO03"));
		assertThat(journaldata.getBrevGruppe(), is(""));
		assertThat(journaldata.getFaktDistrKanal(), is(DistKanal.S));
		assertThat(journaldata.getSensitivt(), is(true));
		assertThat(journaldata.getElektroniskDistr(), is(false));
		assertThat(journaldata.getDatoJournal().equals(LocalDate.parse("2010-10-05").atStartOfDay()), is(true));
		assertThat(journaldata.getDatoSendtPrint().equals(LocalDate.parse("2010-10-21").atStartOfDay()), is(true));
	}

	@Test
	public void shouldMapJournalStatusA() throws Exception {
		Bdok100ArbTbl record = Bdok100ArbTbl.builder()
				.journalforingsfil(validRecords.get(6))
				.build();
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(record);
		Journaldata journaldata = bdok100ArbTbl.getJournaldata();

		assertThat(journaldata.getOnDemandInstans(), is("INFOT_UT"));
		assertThat(journaldata.getSaksNummer(), is("0106B01"));
		assertThat(journaldata.getBestillendeFagsystemkode(), is(BestillendeFagsystemCode.AO01));
		assertThat(journaldata.getDokumentTilhorendefagomraadekode(), is(FagomradeCode.FOS));
		assertThat(journaldata.getJournalfEnhet(), is("0106"));
		assertThat(journaldata.getSaksbehandlerId(), is("ABC123"));
		assertThat(journaldata.getSaksbehandNavn(), is("Ukjent"));
		assertThat(journaldata.getJournalStatus(), is("A"));
		assertThat(journaldata.getDatoFerdig().equals(LocalDate.parse("2010-10-21").atStartOfDay()), is(true));
		assertThat(journaldata.getGjelderID(), is("11111111111"));
		assertThat(journaldata.getGjelderType(), is(GjelderType.PERSON));
		assertThat(journaldata.getInnhold(), is("Innhold"));
		assertThat(journaldata.getAvsendMottakID(), is("11111111111"));
		assertThat(journaldata.getAvsendMottaker(), is("TESTESEN"));
		assertThat(journaldata.getDatoDokument(), is(nullValue()));
		assertThat(journaldata.getDokumentType(), is("U"));
		assertThat(journaldata.getKategori(), is("B"));
		assertThat(journaldata.getBrevKode(), is("FO03"));
		assertThat(journaldata.getBrevGruppe(), is(""));
		assertThat(journaldata.getFaktDistrKanal(), nullValue());
		assertThat(journaldata.getSensitivt(), is(true));
		assertThat(journaldata.getElektroniskDistr(), is(false));
		assertThat(journaldata.getDatoJournal(), is(nullValue()));
		assertThat(journaldata.getDatoSendtPrint(), is(nullValue()));
	}
}