package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.NAVKontor;
import no.nav.dokprod_infotrygdbrev.bdok100.domain.validation.NAVKontorValidator;
import no.nav.dokprod_infotrygdbrev.bdok100.support.file.NAVKontorFileReader;
import no.nav.dokprod_infotrygdbrev.common.support.BeanValidator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

/**
 * Unit test for Nav Kontor Mapper
 *
 */
public class NAVKontorMapperTest {

	private NAVKontor built;

	private NAVKontorValidator validator = new NAVKontorValidator();
	private NAVKontorFileReader fileReader = new NAVKontorFileReader();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void Setup() throws Exception {
		validator.setBeanValidator(new BeanValidator());

		built = NAVKontor.builder()
				.tkNr("0101")
				.tKName("HALDEN")
				.orgNummer("987654321")
				.bankGiroRef("0")
				.postGiroRef("0")
				.postadresse1("POSTBOKS 0123")
				.postadresse2("")
				.postNr("1751")
				.poststed("HALDEN")
				.besoeksadresse1("KIRKEGATA 3")
				.besoeksadresse2("")
				.postNrBesoeksadresse("1751")
				.poststedBesoeksadresse("HALDEN")
				.telefon("54321678")
				.telefaks("43215678")
				.aapningstid1("09.00-14.30")
				.aapningstid2("TELEFONVAKT")
				.aapningstid3("09.00-14.30")
				.build();
	}

	@Test
	public void testMap() throws Exception {
		List<NAVKontor> navKontors = fileReader.readObjectsFromFile("bdok100/kontor/FF01_ref_dynamisk_kontor_info_infotryd.dat", Bdok100Constants.BDOK100_INPUT_CHARSET);
		for (NAVKontor kontor : navKontors) {
			validator.validate(kontor);
		}
	}

	@Test
	public void shouldMapValidKontorFor8100MissingEverything() throws Exception {
		NAVKontor mapped = NAVKontorMapper.map("8100;;;;;;;;;;;;;;;;;;;;;");
		assertThat(mapped, isA(NAVKontor.class));
	}

	@Test
	public void testThatMapperReturnsNullForInvalidString() throws Exception {
		NAVKontor mapped = NAVKontorMapper.map("0101;HALDEN;987654321;0;0;POSTBOKS 0123;;1751;HALDEN;KIRKEGATA 3;;1751;HALDEN;54321678;43215678;09.00-14.30;TELEFONVAKT;09.00-14.30");
		assertThat(mapped, is(nullValue()));
	}

	@Test
	public void testThatMapperReturnsExpectedObject() throws Exception {
		NAVKontor mapped = NAVKontorMapper.map("0101;HALDEN;987654321;0;0;B;DU;POSTBOKS 0123;;1751;HALDEN;KIRKEGATA 3;;1751;HALDEN;54321678;43215678;09.00-14.30;TELEFONVAKT;09.00-14.30;WWW.NAV.NO;");
		assertEquals(mapped, built);
	}

}