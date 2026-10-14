package no.nav.dokprod_infotrygdbrev.bdok100.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jms.core.JmsOperations;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("dokprod.infotrygdbrev")
@Validated
public record DokprodInfotrygdbrevProperties(@Valid @NotNull Endpoints endpoints, @Valid @NotNull JmsProperties qdok001) {

	public record Endpoints(
		@Valid @NotNull Endpoint pdl,
		@Valid @NotNull Endpoint sak
	) {}

	public record Endpoint(
		@NotBlank String url,
		@NotBlank String scope
	) {}

	@Validated
	public record JmsProperties(
		@NotBlank String hostname,
		@Min(1) @Max(65535) int port,
		@NotBlank String queueManager,
		@NotBlank String channel,
		@NotBlank String queueName
	) {}
}
