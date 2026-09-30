package no.nav.dokprod_infotrygdbrev.bdok100.domain.validation;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.NAVKontor;
import no.nav.dokprod_infotrygdbrev.common.exception.NAVKontorFormatException;
import no.nav.dokprod_infotrygdbrev.common.support.BeanValidator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit test for {@link NAVKontorValidator}
 *
 */
public class NAVKontorValidatorTest {

	private NAVKontorValidator validator = new NAVKontorValidator();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		validator.setBeanValidator(new BeanValidator());
	}

	@Test
	public void shouldValidateOk() throws Exception {
		NAVKontor validNAVKontor = createNAVKontor().build();
		validator.validate(validNAVKontor);
	}

	@Test
	public void shouldThrowExceptionOnMissingTKName() throws Exception {
		expectedException.expect(NAVKontorFormatException.class);
		NAVKontor validNAVKontor = createNAVKontor().build();
		validNAVKontor.setTKName("");
		validator.validate(validNAVKontor);
	}

	@Test
	public void shouldThrowExceptionOnTooLongOrgNummer() throws Exception {
		expectedException.expect(NAVKontorFormatException.class);
		NAVKontor validNAVKontor = createNAVKontor().build();
		validNAVKontor.setOrgNummer("9876543210");
		validator.validate(validNAVKontor);
	}

	@Test
	public void shouldNotThrowExceptionOnMissingBankGiroRef() throws Exception {
		NAVKontor validNAVKontor = createNAVKontor().build();
		validNAVKontor.setBankGiroRef(null);
		validator.validate(validNAVKontor);
	}

	@Test
	public void shouldThrowExceptionOnMissingPoststed() throws Exception {
		expectedException.expect(NAVKontorFormatException.class);
		NAVKontor validNAVKontor = createNAVKontor().build();
		validNAVKontor.setPoststed("");
		validator.validate(validNAVKontor);
	}

	@Test
	public void shouldNotThrowExceptionOnMissingBesoeksadresse() throws Exception {
		NAVKontor validNAVKontor = createNAVKontor().build();
		validNAVKontor.setBesoeksadresse1("");
		validator.validate(validNAVKontor);
	}

	@Test
	public void shouldThrowExceptionOnInvalidPostNrBesoeksadresse() throws Exception {
		expectedException.expect(NAVKontorFormatException.class);
		NAVKontor validNAVKontor = createNAVKontor().build();
		validNAVKontor.setPostNrBesoeksadresse("123456789");
		validator.validate(validNAVKontor);
	}

	@Test
	public void shouldNotThrowExceptionOnMissingPostStedBesoeksadresse() throws Exception {
		NAVKontor validNAVKontor = createNAVKontor().build();
		validNAVKontor.setPoststedBesoeksadresse("");
		validator.validate(validNAVKontor);
	}

	@Test
	public void shouldNotThrowExceptionOnMissingTelefon() throws Exception {
		NAVKontor validNAVKontor = createNAVKontor().build();
		validNAVKontor.setTelefon(null);
		validator.validate(validNAVKontor);
	}

	@Test
	public void shouldNotThrowExceptionOnMissingTelefaks() throws Exception {
		NAVKontor validNAVKontor = createNAVKontor().build();
		validNAVKontor.setTelefaks(null);
		validator.validate(validNAVKontor);
	}

	private NAVKontor.NAVKontorBuilder createNAVKontor() {
		return NAVKontor.builder()
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
				.aapningstid3("09.00-14.30");
	}

}