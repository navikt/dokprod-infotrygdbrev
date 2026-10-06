package no.nav.dokprod_infotrygdbrev.bdok100.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("nais")
@Validated
public record NaisProperties (
	// https://doc.nais.io/auth/reference/#texas
	@NotBlank String tokenExchangeEndpoint,

	@NotBlank String tokenEndpoint
 ) {}
