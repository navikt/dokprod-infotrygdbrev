package no.nav.dokprod_infotrygdbrev.bdok100.domain.validation;

import static no.nav.dokprod_infotrygdbrev.bdok100.Bdok100Constants.BDOK100_OUTPUT_CHARSET;

import no.nav.dokprod_infotrygdbrev.bdok100.domain.VedleggsListe;
import no.nav.dokprod_infotrygdbrev.bdok100.support.file.VedleggslisteFileReader;
import no.nav.dokprod_infotrygdbrev.common.exception.VedleggFormatException;
import no.nav.dokprod_infotrygdbrev.common.support.BeanValidator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

/**
 * Unit test for {@link VedleggsListeValidator}
 *
 */
public class VedleggsListeValidatorTest {

	private VedleggsListeValidator validator = new VedleggsListeValidator();
	private VedleggslisteFileReader fileReader = new VedleggslisteFileReader();

	private List<VedleggsListe> validRecords;
	private List<VedleggsListe> inValidRecords;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		validator.setBeanValidator(new BeanValidator());
		validRecords = fileReader.readObjectsFromFile("bdok100/vedlegg/valid_infotrygd_vedlegg.txt", BDOK100_OUTPUT_CHARSET);
		inValidRecords = fileReader.readObjectsFromFile("bdok100/vedlegg/invalid_infotrygd_vedlegg.txt", BDOK100_OUTPUT_CHARSET);
	}

	@Test
	public void shouldValidateOk() throws Exception {
		VedleggsListe vedlegg = createValidVedlegg();
		validator.validate(vedlegg);
	}

	@Test
	public void shouldValidateWithNoVedlegg() throws Exception {
		VedleggsListe vedlegg = createValidVedlegg();
		vedlegg.removeAllVedlegg();
		validator.validate(vedlegg);
	}

	@Test
	public void shouldNotValidateWithTooShortBrevkode() throws Exception {
		expectedException.expect(VedleggFormatException.class);
		VedleggsListe vedlegg = createValidVedlegg();
		vedlegg.setBrevkode("X23");
		validator.validate(vedlegg);
	}

	@Test
	public void shouldNotValidateWithTooLongBrevkode() throws Exception {
		expectedException.expect(VedleggFormatException.class);
		VedleggsListe vedlegg = createValidVedlegg();
		vedlegg.setBrevkode("X2373");
		validator.validate(vedlegg);
	}

	@Test
	public void shouldValidateValidRecordsFromFile() throws Exception {
		validator.validate(validRecords);
	}

	@Test
	public void shouldNotValidateInvalidRecordsFromFile() throws Exception {
		expectedException.expect(VedleggFormatException.class);
		validator.validate(inValidRecords);
	}

	private VedleggsListe createValidVedlegg() {
		VedleggsListe liste = new VedleggsListe();
		liste.setBrevkode("SP99");
		liste.addVedlegg("NAV_21-00.00");
		liste.addVedlegg("NAV_21-12.05");

		return liste;
	}
}
