package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import static no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Adressetype.NORSK;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.FOR_LITE_MELLOMROM_MELLOM_ADRESSE_OG_BREVTEKST;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.LINJEDATA_MED_BREVTEKST2;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.LINJEDATA_MED_TOPPTEKST;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.LINJEDATA_MED_TOPPTEKST_NORGE;
import static no.nav.dokprod_infotrygdbrev.bdok100.support.Bdok100TestdataUtil.readLinjedataFile;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Linjedata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Adressetype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Brevtype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Spraak;
import no.nav.dokprod_infotrygdbrev.common.support.BeanValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Unit test for {@link LinjedataMapper}
 *
 */
public class LinjedataMapperTest {

	private static final String BREVTEKST_BT2 = readLinjedataFile("med_brevtekst2_brevtekst.crlf.txt");
	private static final String FOOTER_BT2 = readLinjedataFile("med_brevtekst2_footer.crlf.txt");
	private static final String BREVTEKST2_BT2 = readLinjedataFile("med_brevtekst2_brevtekst2.crlf.txt");

	private static final String BREVTEKST_TOPP = readLinjedataFile("med_topptekst_brevtekst.crlf.txt");
	private static final String FOOTER_TOPP = readLinjedataFile("med_topptekst_footer.crlf.txt");

	private BeanValidator validator = new BeanValidator();
	private LinjedataMapper mapper = new LinjedataMapper();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldFailWhenSpaceBetweenAdresseAndBrevtekstIsLessThan2() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Too many address lines");
		mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile(FOR_LITE_MELLOMROM_MELLOM_ADRESSE_OG_BREVTEKST)).build());

	}

	@Test
	public void shouldMapLinjedataWithTopptekst() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile(LINJEDATA_MED_TOPPTEKST)).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		assertThat(bdok100ArbTbl.getIdnr(), is("ODIQ026082400020"));
		assertThat(linjedata.getTknr1(), is("0189"));
		assertThat(linjedata.getFnr(), is("11111111111"));
		assertThat(linjedata.getAar(), is("26"));
		assertThat(linjedata.getMaaned(), is("08"));
		assertThat(linjedata.getDag(), is("24"));
		assertThat(linjedata.getLegeFnr(), nullValue());
		assertThat(linjedata.getPostnr(), is("0001"));
		assertThat(linjedata.getBrevtype(), is(Brevtype.OK));

		assertThat(linjedata.getTopparkIndikator(), is("T"));
		assertThat(linjedata.getTopparkPostboksMottat(), is("1431"));
		assertThat(linjedata.getTopparkKontrolltegn1(), is("B"));
		assertThat(linjedata.getTopparkTemaFagomraade(), is("19"));
		assertThat(linjedata.getTopparkKontrolltegn2(), is("S"));

		assertThat(linjedata.getBrevnavn(), is("SP10"));
		assertThat(linjedata.getSpraak(), is(Spraak.B));
		assertThat(linjedata.getTknr2(), is("0101"));
		assertThat(linjedata.getInfotrygdBrevkodePage(), is("XXXXX_YY"));

		assertThat(linjedata.getAdresseLinje1(), is("FIRMA AS"));
		assertThat(linjedata.getAdresseLinje2(), is("Postboks 1"));
		assertThat(linjedata.getAdresseLinje3(), nullValue());
		assertThat(linjedata.getPoststed(), is("Oslo"));
		assertThat(linjedata.getPostnr(), is("0001"));
		assertNull(linjedata.getLand());
		assertThat(bdok100ArbTbl.getBrevtekster().get(0).getInnhold(), is(BREVTEKST_TOPP));
		assertThat(bdok100ArbTbl.getBrevtekster().get(0).getRekkefolge(), is(0));

		assertThat(linjedata.getFooter(), is(FOOTER_TOPP));
		assertThat(bdok100ArbTbl.getBrevtekster(), hasSize(1));
	}

	@Test
	public void shouldMapLinjedataWithNorge() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile(LINJEDATA_MED_TOPPTEKST_NORGE)).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		assertThat(linjedata.getAdresseLinje1(), is("FIRMA AS"));
		assertThat(linjedata.getAdresseLinje2(), is("Postboks 1"));
		assertThat(linjedata.getAdresseLinje3(), nullValue());
		assertThat(linjedata.getPoststed(), is("Oslo"));
		assertThat(linjedata.getPostnr(), is("0001"));
		assertNull(linjedata.getLand());
		assertThat(linjedata.getAdressetype(), is(NORSK));

		assertThat(bdok100ArbTbl.getBrevtekster(), hasSize(1));
	}

	@Test
	public void shouldMapLinjedataWithUnknownLanguageCodeToNB() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile("linjedata_med_ukjent_spraakkode.crlf.txt")).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();
		assertThat(linjedata.getSpraak(), is(Spraak.U));
	}

	@Test
	public void shouldMapLinjedataWithEnglishCodeToEN() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile("linjedata_med_engelsk_spraakkode.crlf.txt")).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();
		assertThat(linjedata.getSpraak(), is(Spraak.E));
	}

	@Test
	public void shouldMapLinjedataWithBrevtekst2() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile(LINJEDATA_MED_BREVTEKST2)).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		assertThat(bdok100ArbTbl.getIdnr(), is("ODIQ026082400020"));
		assertThat(linjedata.getTknr1(), is("0189"));
		assertThat(linjedata.getFnr(), is("11111111111"));
		assertThat(linjedata.getAar(), is("26"));
		assertThat(linjedata.getMaaned(), is("08"));
		assertThat(linjedata.getDag(), is("24"));
		assertThat(linjedata.getLegeFnr(), nullValue());
		assertThat(linjedata.getPostnr(), is("0001"));
		assertThat(linjedata.getBrevtype(), is(Brevtype.OK));

		assertThat(linjedata.getTopparkIndikator(), nullValue());
		assertThat(linjedata.getTopparkPostboksMottat(), nullValue());
		assertThat(linjedata.getTopparkKontrolltegn1(), nullValue());
		assertThat(linjedata.getTopparkTemaFagomraade(), nullValue());
		assertThat(linjedata.getTopparkKontrolltegn2(), nullValue());

		assertThat(linjedata.getBrevnavn(), is("BA33"));
		assertThat(linjedata.getSpraak(), is(Spraak.B));
		assertThat(linjedata.getTknr2(), is("0101"));
		assertThat(linjedata.getInfotrygdBrevkodePage(), is("XXXXX_YY"));

		assertThat(linjedata.getAdresseLinje1(), is("TEST TESTESEN"));
		assertThat(linjedata.getAdresseLinje2(), is("Testeveien 18"));
		assertThat(linjedata.getAdresseLinje3(), nullValue());
		assertThat(linjedata.getPoststed(), is("Oslo"));
		assertThat(linjedata.getPostnr(), is("0001"));
		assertNull(linjedata.getLand());
		assertThat(bdok100ArbTbl.getBrevtekster().get(0).getInnhold(), is(BREVTEKST_BT2));
		assertThat(bdok100ArbTbl.getBrevtekster().get(0).getRekkefolge(), is(0));
		assertThat(linjedata.getFooter(), is(FOOTER_BT2));
		assertThat(bdok100ArbTbl.getBrevtekster().get(1).getInnhold(), is(BREVTEKST2_BT2));
		assertThat(bdok100ArbTbl.getBrevtekster().get(1).getRekkefolge(), is(1));
	}

	@Test
	public void shouldMapUtenlandsk5Adresselinjer() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile("utenlandsk_5_adresselinjer.crlf.txt")).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		assertThat(linjedata.getAdresseLinje1(), is("MAX TEST"));
		assertThat(linjedata.getAdresseLinje2(), is("C/O BJARNE TEST"));
		assertThat(linjedata.getAdresseLinje3(), is("POSTBOKS 26/4"));
		assertThat(linjedata.getAdresseLinje4(), is("PL-12345 WARSAWA"));
		assertThat(linjedata.getPostnr(), is("0000"));
		assertThat(linjedata.getPoststed(), nullValue());
		assertThat(linjedata.getLand(), is("POLEN"));
	}

	@Test
	public void shouldMapUtenlandsk4Adresselinjer() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile("utenlandsk_4_adresselinjer.crlf.txt")).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		assertThat(linjedata.getAdresseLinje1(), is("MAX TEST"));
		assertThat(linjedata.getAdresseLinje2(), is("C/O BJARNE TEST"));
		assertThat(linjedata.getAdresseLinje3(), is("PL-000-0000 WARSAWA"));
		assertThat(linjedata.getAdresseLinje4(), nullValue());
		assertThat(linjedata.getPostnr(), is("0000"));
		assertThat(linjedata.getPoststed(), nullValue());
		assertThat(linjedata.getLand(), is("POLEN"));
	}

	@Test
	public void shouldMapPurreBrev() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile("purring.crlf.txt")).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		assertThat(linjedata.getAdresseLinje1(), is("LEGE DR. TEST"));
		assertThat(linjedata.getAdresseLinje2(), is("Testgata 100"));
		assertThat(linjedata.getAdresseLinje3(), nullValue());
		assertThat(linjedata.getAdresseLinje4(), nullValue());
		assertThat(linjedata.getPoststed(), is("GAMLE OSLO"));
		assertThat(linjedata.getPostnr(), is("0010"));
	}

	@Test
	public void shouldMapWithSpaceInAddress() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile("med_space_adresse.crlf.txt")).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		assertThat(linjedata.getAdresseLinje1(), is("TEST TESTESEN"));
		assertThat(linjedata.getAdresseLinje2(), is("C/O TESTINE TESTESEN"));
		assertThat(linjedata.getAdresseLinje3(), is(""));
		assertThat(linjedata.getAdresseLinje4(), is("EKSTRA LINJE"));
		assertThat(linjedata.getPoststed(), is("Oslo"));
		assertThat(linjedata.getPostnr(), is("0001"));
	}

	@Test
	public void shouldMapWithOtherDateFormatAndStartOnLine10() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile("annet_dato_format.crlf.txt")).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		assertThat(linjedata.getAdresseLinje1(), is("TEST TESTESEN"));
		assertThat(linjedata.getAdresseLinje2(), is("Testeveien 18"));
		assertThat(linjedata.getAdresseLinje3(), nullValue());
		assertThat(linjedata.getAdresseLinje4(), nullValue());
		assertThat(linjedata.getPoststed(), is("Oslo"));
		assertThat(linjedata.getPostnr(), is("0001"));
	}

	@Test
	public void shouldMapWithToUtenlandsWhenNorskInFirstLine() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile("med_feil_landkode_forste_linje.crlf.txt")).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		assertThat(linjedata.getAdresseLinje1(), is("TEST TESTESEN"));
		assertThat(linjedata.getAdresseLinje2(), is("C/O TESTINE TESTESEN"));
		assertThat(linjedata.getAdresseLinje3(), is("Testgaten 1/2"));
		assertThat(linjedata.getAdresseLinje4(), is("PL-12345 WARSAWA"));
		assertThat(linjedata.getPostnr(), is("0000"));
		assertThat(linjedata.getPoststed(), nullValue());
		assertThat(linjedata.getLand(), is("POLEN"));
		assertThat(linjedata.getAdressetype(), is(Adressetype.UTENLANDSK));
	}

	@Test
	public void shouldMapWithEmptyPostalInFirstLine() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile("med_tom_landkode_forste_linje.crlf.txt")).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		assertThat(linjedata.getAdresseLinje1(), is("TEST TESTESEN"));
		assertThat(linjedata.getAdresseLinje2(), is("C/O TESTINE TESTESEN"));
		assertThat(linjedata.getAdresseLinje3(), is("Testgaten 1/2"));
		assertThat(linjedata.getAdresseLinje4(), is("PL-12345 WARSAWA"));
		assertThat(linjedata.getPostnr(), is("0000"));
		assertThat(linjedata.getPoststed(), nullValue());
		assertThat(linjedata.getLand(), is("POLEN"));
		assertThat(linjedata.getAdressetype(), is(Adressetype.UTENLANDSK));
	}

	@Test
	public void shouldMapToNorskWhenUtenlandsInFirstLine() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile("NorskMenUtenlandsk.crlf.txt")).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		assertThat(linjedata.getAdresseLinje1(), is("TEST TESTESEN"));
		assertThat(linjedata.getAdresseLinje2(), is("Testveien 1A"));
		assertThat(linjedata.getAdresseLinje3(), nullValue());
		assertThat(linjedata.getAdresseLinje4(), nullValue());
		assertThat(linjedata.getPostnr(), is("0001"));
		assertThat(linjedata.getPoststed(), is("Oslo"));
		assertThat(linjedata.getAdressetype(), is(NORSK));
	}

	@Test
	public void shouldMapMultiplePagesPerSection() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile("3pages.crlf.txt")).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		assertThat(linjedata.getFooter(), is(readLinjedataFile("3pages_footer.crlf.txt")));
		assertThat(bdok100ArbTbl.getBrevtekster(), hasSize(3));
		assertThat(bdok100ArbTbl.getBrevtekster().get(0).getInnhold(), is(readLinjedataFile("3pages_p1.crlf.txt")));
		assertThat(bdok100ArbTbl.getBrevtekster().get(0).getRekkefolge(), is(0));
		assertThat(bdok100ArbTbl.getBrevtekster().get(1).getInnhold(), is(readLinjedataFile("3pages_p2.crlf.txt")));
		assertThat(bdok100ArbTbl.getBrevtekster().get(1).getRekkefolge(), is(1));
		assertThat(bdok100ArbTbl.getBrevtekster().get(2).getInnhold(), is(readLinjedataFile("3pages_p3.crlf.txt")));
		assertThat(bdok100ArbTbl.getBrevtekster().get(2).getRekkefolge(), is(2));
	}

	@Test
	public void shouldRemoveEmptyPages() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = mapper.map(Bdok100ArbTbl.builder()
				.lineprintBrev(readLinjedataFile("4pages_1empty.crlf.txt")).build());
		Linjedata linjedata = bdok100ArbTbl.getLinjedata();

		assertThat(linjedata.getFooter(), is(readLinjedataFile("3pages_footer.crlf.txt")));
		assertThat(bdok100ArbTbl.getBrevtekster(), hasSize(3));
		assertThat(bdok100ArbTbl.getBrevtekster().get(0).getInnhold(), is(readLinjedataFile("3pages_p1.crlf.txt")));
		assertThat(bdok100ArbTbl.getBrevtekster().get(0).getRekkefolge(), is(0));
		assertThat(bdok100ArbTbl.getBrevtekster().get(1).getInnhold(), is(readLinjedataFile("3pages_p2.crlf.txt")));
		assertThat(bdok100ArbTbl.getBrevtekster().get(1).getRekkefolge(), is(1));
		assertThat(bdok100ArbTbl.getBrevtekster().get(2).getInnhold(), is(readLinjedataFile("3pages_p3.crlf.txt")));
		assertThat(bdok100ArbTbl.getBrevtekster().get(2).getRekkefolge(), is(2));
	}

	@Test
	public void shouldMapAllEntries() throws Exception {
		mapAndValidate("full.crlf.txt");
	}

	@Test
	public void shouldFailReasonablyWhenInvalid() throws Exception {
		String linjedataString = readLinjedataFile("3pages.crlf.txt");
		for (int length = linjedataString.length(); length > 0; length--) {
			try {
				map(linjedataString.substring(0, length));
			} catch (IllegalArgumentException e) {
				assertThat(e.getMessage(), containsString("Linjedata is too small"));
			}
		}
	}

	@Test
	public void shouldNotFailBPBrev() throws Exception {
		map(readLinjedataFile("bpbrev.crlf.txt"));
	}

	@Test
	public void shouldMapAG60ToOKBrevtype() throws Exception {
		Bdok100ArbTbl bdok100ArbTbl = map(readLinjedataFile("bpbrev.crlf.txt").replaceFirst("0PL1", "AG60"));
		assertThat(bdok100ArbTbl.getLinjedata().getBrevtype(), is(Brevtype.OK));
	}

	private Bdok100ArbTbl map(String substring) {
		return mapper.map(Bdok100ArbTbl.builder().lineprintBrev(substring).build());
	}

	private void mapAndValidate(String file) {
		String s = readLinjedataFile(file);
		Iterable<String> split = Splitter.on(Pattern.compile("((\\r\\n)|^)1")).omitEmptyStrings().split(s);
		List<Bdok100ArbTbl> bdok100ArbTbls = Lists.newArrayList();
		for (String linjedata : split) {
			bdok100ArbTbls.add(map("1" + linjedata));
		}
		for (Bdok100ArbTbl bdok100ArbTbl : bdok100ArbTbls) {
			validator.validate(bdok100ArbTbl.getLinjedata());
		}
	}

}