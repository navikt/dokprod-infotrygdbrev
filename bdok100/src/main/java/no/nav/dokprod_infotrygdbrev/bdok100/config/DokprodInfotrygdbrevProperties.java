package no.nav.dokprod_infotrygdbrev.bdok100.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("dokprodinfotrygdbrev")
@Validated
public record DokprodInfotrygdbrevProperties(@Valid Endpoints endpoints) {

	public record Endpoints(
		@Valid Endpoint pdl,
		@Valid Endpoint sak
	) {}

	public record Endpoint(
		@NotBlank String url,
		@NotBlank String scope
	) {}
}
