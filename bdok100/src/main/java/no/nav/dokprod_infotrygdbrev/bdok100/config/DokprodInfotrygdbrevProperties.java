package no.nav.dokprod_infotrygdbrev.bdok100.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("dokprod.infotrygdbrev")
@Validated
public record DokprodInfotrygdbrevProperties(@Valid @NotNull Endpoints endpoints) {

	public record Endpoints(
		@Valid @NotNull Endpoint pdl,
		@Valid @NotNull Endpoint sak
	) {}

	public record Endpoint(
		@NotBlank String url,
		@NotBlank String scope
	) {}
}
