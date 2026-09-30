package no.nav.dokprod_infotrygdbrev.common.support;

import lombok.AllArgsConstructor;
import no.nav.dokprod_infotrygdbrev.common.exception.BeanValidationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import jakarta.validation.constraints.NotNull;

/**
 * Unit test for {@link BeanValidator}
 *
 */
public class BeanValidatorTest {

	private BeanValidator validator = new BeanValidator();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void shouldValidateOk() throws Exception {
		validator.validate(new TestObject("ok"));
	}

	@Test
	public void shouldValidateNotOk() throws Exception {
		expectedException.expect(BeanValidationException.class);
		expectedException.expectMessage("TestObject is not valid: value <null> - must not be null");
		validator.validate(new TestObject(null));
	}

	@AllArgsConstructor
	private class TestObject {
		@NotNull
		private final String value;
	}
}