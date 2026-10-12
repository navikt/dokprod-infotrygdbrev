package no.nav.dokprod_infotrygdbrev.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("dokprod.infotrygdbrev.jms")
@Validated
public record JmsProperties(
		@NotBlank String hostname,
		@Min(1) @Max(65535) int port,
		@NotBlank String queueManager,
		@NotBlank String channel,
		@NotBlank String queueName
) {}
