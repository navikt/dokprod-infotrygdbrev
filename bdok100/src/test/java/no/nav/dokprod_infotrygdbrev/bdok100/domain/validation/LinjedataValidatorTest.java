package no.nav.dokprod_infotrygdbrev.bdok100.domain.validation;

import com.google.common.collect.Lists;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100ArbTbl;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Bdok100Brevtekst;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Journaldata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.Linjedata;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.NAVKontor;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Adressetype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Brevtype;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.code.Spraak;
import no.nav.dokprod_infotrygdbrev.bdok100.support.to.NAVKontors;
import no.nav.dokprod_infotrygdbrev.common.exception.AvviksfilException;
import no.nav.dokprod_infotrygdbrev.common.exception.KontrollRapportException;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link LinjedataValidator}
 *
 */
public class LinjedataValidatorTest {

    private static final String TKNR_1 = "1111";
    private static final String TKNR_2 = "1111";
    private LinjedataValidator validator = new LinjedataValidator();

    private NAVKontors navKontors = new NAVKontors();

    @Before
    public void setUp() throws Exception {
        navKontors.add(NAVKontor.builder().tkNr(TKNR_2).build());
        validator.setNavKontors(navKontors);
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldValidateOk() throws Exception {
        validate(createLinjedata().build());
        validate(createLinjedata().tknr1("0000").build());
        validate(createLinjedata().tknr1("0100").build());
        validate(createLinjedata().tknr1("2030").build());
        validate(createLinjedata().tknr1("2089").build());
        validate(createLinjedata().tknr1("2300").build());
        validate(createLinjedata().tknr1("2398").build());
        validate(createLinjedata().tknr1("2821").build());
        validate(createLinjedata().tknr1("3000").build());
        validate(createLinjedata().tknr1("3099").build());
        validate(createLinjedata().tknr1("3400").build());
        validate(createLinjedata().tknr1("3499").build());
        validate(createLinjedata().tknr1("3800").build());
        validate(createLinjedata().tknr1("3899").build());
        validate(createLinjedata().tknr1("4200").build());
        validate(createLinjedata().tknr1("4299").build());
        validate(createLinjedata().tknr1("4401").build());
        validate(createLinjedata().tknr1("4499").build());
        validate(createLinjedata().tknr1("4530").build());
        validate(createLinjedata().tknr1("4601").build());
        validate(createLinjedata().tknr1("4700").build());
        validate(createLinjedata().tknr1("4800").build());
        validate(createLinjedata().tknr1("4862").build());
        validate(createLinjedata().tknr1("5000").build());
        validate(createLinjedata().tknr1("5301").build());
        validate(createLinjedata().tknr1("5303").build());
        validate(createLinjedata().tknr1("5400").build());
        validate(createLinjedata().tknr1("5700").build());
        validate(createLinjedata().tknr1("5703").build());
        validate(createLinjedata().tknr1("5998").build());
    }

    @Test
    public void shouldNotValidateOk() throws Exception {
        try {
            validate(createLinjedata().tknr1("2550").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 2550"));
        }
        try {
            validate(createLinjedata().tknr1("2399").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 2399"));
        }
        try {
            validate(createLinjedata().tknr1("2820").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 2820"));
        }
        try {
            validate(createLinjedata().tknr1("2822").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 2822"));
        }
        try {
            validate(createLinjedata().tknr1("2999").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 2999"));
        }
        try {
            validate(createLinjedata().tknr1("3100").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 3100"));
        }
        try {
            validate(createLinjedata().tknr1("3399").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 3399"));
        }

        try {
            validate(createLinjedata().tknr1("3500").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 3500"));
        }

        try {
            validate(createLinjedata().tknr1("3799").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 3799"));
        }

        try {
            validate(createLinjedata().tknr1("3900").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 3900"));
        }

        try {
            validate(createLinjedata().tknr1("4199").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 4199"));
        }

        try {
            validate(createLinjedata().tknr1("4300").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 4300"));
        }

        try {
            validate(createLinjedata().tknr1("4400").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 4400"));
        }

        try {
            validate(createLinjedata().tknr1("4500").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 4500"));
        }

        try {
            validate(createLinjedata().tknr1("2550").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 2550"));
        }

        try {
            validate(createLinjedata().tknr1("4529").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 4529"));
        }

        try {
            validate(createLinjedata().tknr1("4531").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 4531"));
        }

        try {
            validate(createLinjedata().tknr1("4600").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 4600"));
        }

        try {
            validate(createLinjedata().tknr1("5999").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 5999"));
        }

        try {
            validate(createLinjedata().tknr1("8888").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 8888"));
        }

        try {
            validate(createLinjedata().tknr1("9401").build());
            fail("Did not throw");
        } catch (AvviksfilException exception) {
            assertThat(exception, isA(AvviksfilException.class));
            assertThat(exception.getMessage(), containsString("Ugyldig TKNR 9401"));
        }
    }

    @Test
    public void shouldValidateTknr4530() throws Exception {
        Linjedata.LinjedataBuilder linjedata = createLinjedata();
        linjedata.tknr1("4530");
        validate(linjedata.build());
    }

    @Test
    public void shouldFailAvviksFil_AGNAVNADR() throws Exception {
        expectedException.expect(AvviksfilException.class);
        expectedException.expectMessage("Forekomst av <AGNAVNADR> i brevtekst");
        validate(createLinjedata().build(), Lists.newArrayList(Bdok100Brevtekst.builder().innhold("test <AGNAVNADR> tekst").build()));
    }

    @Test
    public void shouldFailAvviksFil_MissingKontor() throws Exception {
        expectedException.expect(AvviksfilException.class);
        expectedException.expectMessage("Kunne ikke finne gitt NAV-kontor i kildefil 4450");
        validate(createLinjedata().tknr2("4450").build());
    }

    @Test
    public void shouldFailAvviksFil_MOTT() throws Exception {
        expectedException.expect(AvviksfilException.class);
        expectedException.expectMessage("Forekomst av <MOTT> i brevtekst");
        validate(createLinjedata().build(), Lists.newArrayList(Bdok100Brevtekst.builder().innhold("test <MOTT> tekst").build()));
    }


    @Test
    public void shouldFailAvviksFil_NorskManglerPoststed() throws Exception {
        expectedException.expect(AvviksfilException.class);
        expectedException.expectMessage("Norsk adresse mangler poststed, må legges til manuelt");
        validate(createLinjedata().poststed(null).build());
    }

    @Test
    public void shouldFailLogging_InvalidTKnrRanges() throws Exception {
        navKontors.add(NAVKontor.builder().tkNr("2299").build());
        navKontors.add(NAVKontor.builder().tkNr("4400").build());
        navKontors.add(NAVKontor.builder().tkNr("4499").build());
        navKontors.add(NAVKontor.builder().tkNr("4700").build());
        navKontors.add(NAVKontor.builder().tkNr("4899").build());

        validate(createLinjedata().tknr1("2299").build());
        failLinjedata(createLinjedata().tknr1("2400").build(), AvviksfilException.class, "Ugyldig TKNR 2400");
        failLinjedata(createLinjedata().tknr1("2450").build(), AvviksfilException.class, "Ugyldig TKNR 2450");
        failLinjedata(createLinjedata().tknr1("2500").build(), AvviksfilException.class, "Ugyldig TKNR 2500");
        failLinjedata(createLinjedata().tknr1("3999").build(), AvviksfilException.class, "Ugyldig TKNR 3999");
        failLinjedata(createLinjedata().tknr1("4399").build(), AvviksfilException.class, "Ugyldig TKNR 4399");
        validate(createLinjedata().tknr1("4401").build());

        validate(createLinjedata().tknr1("4499").build());
        failLinjedata(createLinjedata().tknr1("4500").build(), AvviksfilException.class, "Ugyldig TKNR 4500");
        failLinjedata(createLinjedata().tknr1("4600").build(), AvviksfilException.class, "Ugyldig TKNR 4600");
        validate(createLinjedata().tknr1("4700").build());
        validate(createLinjedata().tknr1("4899").build());
        failLinjedata(createLinjedata().tknr1("6000").build(), AvviksfilException.class, "Ugyldig TKNR 6000");
        failLinjedata(createLinjedata().tknr1("9999").build(), AvviksfilException.class, "Ugyldig TKNR 9999");
    }

    @Test
    public void shouldFailLogging_LokalBrevkode() throws Exception {
        validate(createLinjedata().brevnavn("ZH10").build());
        failLinjedata(createLinjedata().brevnavn("ZH20").build(), KontrollRapportException.class, "Brevnavn som begynner med ZH2 fjernes fra dokumentbestillingen");
        failLinjedata(createLinjedata().brevnavn("ZE20").build(), KontrollRapportException.class, "Brevnavn som begynner med ZE2 fjernes fra dokumentbestillingen");
        failLinjedata(createLinjedata().brevnavn("Z100").build(), KontrollRapportException.class, "Brevnavn som begynner med Z10 fjernes fra dokumentbestillingen");
        failLinjedata(createLinjedata().brevnavn("Z200").build(), KontrollRapportException.class, "Brevnavn som begynner med Z20 fjernes fra dokumentbestillingen");
        failLinjedata(createLinjedata().brevnavn("Z300").build(), KontrollRapportException.class, "Brevnavn som begynner med Z30 fjernes fra dokumentbestillingen");
        failLinjedata(createLinjedata().brevnavn("Z400").build(), KontrollRapportException.class, "Brevnavn som begynner med Z40 fjernes fra dokumentbestillingen");
        validate(createLinjedata().brevnavn("Z500").build());
        failLinjedata(createLinjedata().brevnavn("ZH30").build(), KontrollRapportException.class, "Brevnavn som begynner med ZH3 fjernes fra dokumentbestillingen");
        failLinjedata(createLinjedata().brevnavn("ZE30").build(), KontrollRapportException.class, "Brevnavn som begynner med ZE3 fjernes fra dokumentbestillingen");
        validate(createLinjedata().brevnavn("ZE40").build());
    }

    @Test
    public void shouldFailAvviksFil_TKnr23High() throws Exception {
        expectedException.expect(AvviksfilException.class);
        expectedException.expectMessage("Ugyldig TKNR 2399");
        validate(createLinjedata().tknr1("2399").build());
    }

    @Test
    public void shouldFailAvviksFil_TKnr23Low() throws Exception {
        expectedException.expect(AvviksfilException.class);
        expectedException.expectMessage("Ugyldig TKNR 2550");
        validate(createLinjedata().tknr1("2550").build());
    }

    @Test
    public void shouldFailIgnore_FLXXX_YY() throws Exception {
        expectKontrollRapportException("FLXXX_YY",
                createLinjedata().infotrygdBrevkodePage("FLXXX_YY").build());
    }

    @Test
    public void shouldFailIgnore_BrevtypeBP() throws Exception {
        expectKontrollRapportException("BP", createLinjedata().brevtype(Brevtype.BP).build());
    }

    @Test
    public void shouldFailIgnore_BrevtypeFP() throws Exception {
        expectKontrollRapportException("FP", createLinjedata().brevtype(Brevtype.FP).build());
    }

    /* "bean" validations */

    @Test
    public void shouldFailInvalid_tknr1() throws Exception {
        assertFail(createLinjedata().tknr1("123a"), "For input string:");
        assertFail(createLinjedata().tknr1("123"), "Tknr1");
    }

    @Test
    public void shouldFailInvalid_Aar() throws Exception {
        assertFail(createLinjedata().aar("123"), "Aar");
        assertFail(createLinjedata().aar("abcd"), "Aar");
        assertFail(createLinjedata().aar(null), "Aar");
    }

    @Test
    public void shouldFailInvalid_maaned() throws Exception {
        assertFail(createLinjedata().maaned("ab"), "Maaned");
        assertFail(createLinjedata().maaned("1"), "Maaned");
        assertFail(createLinjedata().maaned(null), "Maaned");
    }

    @Test
    public void shouldFailInvalid_dag() throws Exception {
        assertFail(createLinjedata().dag("ab"), "Dag");
        assertFail(createLinjedata().dag("1"), "Dag");
        assertFail(createLinjedata().dag(null), "Dag");
    }

    @Test
    public void shouldFailInvalid_LegeFnr() throws Exception {
        assertFail(createLinjedata().legeFnr("1234567890A"), "LegeFnr");
        assertFail(createLinjedata().legeFnr("1234567890"), "LegeFnr");
    }

    @Test
    public void shouldFailInvalid_Postnr() throws Exception {
        assertFail(createLinjedata().postnr("abcd"), "Postnr");
        assertFail(createLinjedata().postnr("012"), "Postnr");
        assertFail(createLinjedata().postnr("012a"), "Postnr");
    }

    @Test
    public void shouldFailInvalid_Brevtype() throws Exception {
        assertFail(createLinjedata().brevtype(null), "Brevtype");
    }

    @Test
    public void shouldIgnoreEmptyKontrolltegn1() throws Exception {
        validate(createLinjedata().topparkKontrolltegn1(null).build());
    }

    @Test
    public void shouldFailInvalid_Toppark() throws Exception {
        assertFail(createLinjedata().topparkKontrolltegn2(null), "TopparkKontrolltegn2");
        assertFail(createLinjedata().topparkTemaFagomraade(null), "TopparkTemaFagomraade");

        assertFail(createLinjedata().topparkKontrolltegn2(null)
                .topparkTemaFagomraade(null), "TopparkKontrolltegn2 TopparkTemaFagomraade");

        // All null is ok
        validator.validate(Bdok100ArbTbl.builder().linjedata(createLinjedata().topparkIndikator(null)
                .topparkKontrolltegn1(null).topparkKontrolltegn2(null)
                .topparkTemaFagomraade(null).build())
                .build());
    }


    @Test
    public void shouldFailInvalid_Brevnavn() throws Exception {
        assertFail(createLinjedata().brevnavn("AAA"), "Brevnavn");
        assertFail(createLinjedata().brevnavn(null), "Brevnavn");
    }

    @Test
    public void shouldFailInvalid_Spraak() throws Exception {
        assertFail(createLinjedata().spraak(null), "Spraak");
    }


    @Test
    public void shouldFailInvalid_Tknr2() throws Exception {
        assertFail(createLinjedata().tknr2("123"), "Tknr2");
    }

    @Test
    public void shouldFailInvalid_InfotrygdBrevkodePage() throws Exception {
        assertFail(createLinjedata().infotrygdBrevkodePage("XYZZZYY"), "Infotrygd_BrevkodePage");
    }

    private void assertFail(Linjedata.LinjedataBuilder linjedataBuilder, String message) {
        try {
            validator.validate(Bdok100ArbTbl.builder().linjedata(linjedataBuilder.build()).build());
            fail("should fail with error containing " + message);
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString(message));
        }
    }

    private void validate(Linjedata linjedata, List<Bdok100Brevtekst> brevtekster) {
        validator.validate(Bdok100ArbTbl.builder()
                .linjedata(linjedata)
                .brevtekster(brevtekster)
                .journaldata(Journaldata.builder().build())
                .build());
    }

    private void validate(Linjedata linjedata) {
        validate(linjedata, Lists.newArrayList(Bdok100Brevtekst.builder().innhold("brevtekst").build(),
                Bdok100Brevtekst.builder().innhold("brevtekst2").build()));
    }

    private void expectKontrollRapportException(String message, Linjedata linjedata) {
        try {
            validate(linjedata);
            fail("Should fail with message: " + message);
        } catch (KontrollRapportException e) {
            assertThat(e.getMessage(), containsString(message));
        }
    }

    private void failLinjedata(Linjedata linjedata, Class<? extends Exception> exceptionClass, String message) {
        try {
            validate(linjedata);
            fail("shouldFail");
        } catch (Exception e) {
            assertThat(e, CoreMatchers.instanceOf(exceptionClass));
            assertThat(e.getMessage(), containsString(message));
        }
    }

    private Linjedata.LinjedataBuilder createLinjedata() {
        return Linjedata.builder()
                .tknr1(TKNR_1)
                .fnr("11111111111")
                .aar("01").maaned("04").dag("14")
                .postnr("0000")
                .poststed("horten")
                .brevtype(Brevtype.OK)
                .topparkIndikator("A").topparkKontrolltegn1("1").topparkKontrolltegn2("2").topparkTemaFagomraade("KE")
                .brevnavn("KF10")
                .tknr2(TKNR_2)
                .spraak(Spraak.B)
                .infotrygdBrevkodePage("XXXXX_YY")
                .adressetype(Adressetype.NORSK)
                .adresseLinje1("TESTESEN")
                .adresseLinje2("CITY 1")
                .adresseLinje3("0123 OSLO")
                .adresseLinje4("NORGE")
                .footer("Footy");
    }

}