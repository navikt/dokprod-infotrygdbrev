package no.nav.dokprod_infotrygdbrev.bdok100.support.mappers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import java.util.Optional;

public class Iso3611Alpha2Test {
	@Test
	public void shouldMapToUnknownWhenNullInput() {
		assertThat(Iso3611Alpha2.fromLand(null), is(Optional.empty()));
	}

	@Test
	public void shouldMapToUnknownWhenBlankInput() {
		assertThat(Iso3611Alpha2.fromLand(""), is(Optional.empty()));
	}

	@Test
	public void shouldMapToKodeWhenLandInputUppercase() {
		assertThat(Iso3611Alpha2.fromLand("SVERIGE"), is(Optional.of("SE")));
	}

	@Test
	public void shouldMapToKodeWhenLandInputNormalcase() {
		assertThat(Iso3611Alpha2.fromLand("Sverige"), is(Optional.of("SE")));
	}

	@Test
	public void shouldMapToKodeWhenLandIsFirstWordOfInput() {
		assertThat(Iso3611Alpha2.fromLand("Sweden, Kalmar"), is(Optional.of("SE")));
	}

	@Test
	public void shouldMapToKodeWhenLandIsLastWordOfInput() {
		assertThat(Iso3611Alpha2.fromLand("1234 Stockholm, Sverige"), is(Optional.of("SE")));
	}

	@Test
	public void shouldMapToKodeWhenLandIsLastWordWithPunctuationOfInput() {
		assertThat(Iso3611Alpha2.fromLand("1234 Stockholm, Sverige."), is(Optional.of("SE")));
		assertThat(Iso3611Alpha2.fromLand("1234 Stockholm, Sverige,"), is(Optional.of("SE")));
		assertThat(Iso3611Alpha2.fromLand("1234 Stockholm, Sverige!"), is(Optional.of("SE")));
		assertThat(Iso3611Alpha2.fromLand("1234 Stockholm, Sverige?"), is(Optional.of("SE")));
	}
}