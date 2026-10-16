package no.nav.dokprod_infotrygdbrev.bdok100.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import no.nav.dok.validators.Exists;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@ConfigurationProperties("dokprod.infotrygdbrev")
@Validated
public record DokprodInfotrygdbrevProperties(@Valid @NotNull Endpoints endpoints,
											 @Valid @NotNull JmsProperties qdok001,
											 @Valid @NotNull SftpProperties sftp) {

	public record Endpoints(
		@Valid @NotNull Endpoint pdl,
		@Valid @NotNull Endpoint sak
	) {
	}

	public record Endpoint(
		@NotBlank String url,
		@NotBlank String scope
	) {
	}

	@Validated
	public record JmsProperties(
		@NotBlank String hostname,
		@Min(1) @Max(65535) int port,
		@NotBlank String queueManager,
		@NotBlank String channel,
		@NotBlank String queueName
	) {
	}

	@Validated
	public record SftpProperties(
		@NotBlank String host,
		@Min(1) @Max(65535) int port,
		@NotBlank String username,
		@Exists @NotBlank String privateKeyFile,
		@NotBlank String privateKeyPassphrase,
		@Exists @NotBlank String knownHostsFile,
		@NotBlank String remoteDirectory,
		@NotNull Duration timeout
	) {

		@Override
		public String toString() {
			return "SftpProperties[host=%s, port=%d, username=%s, privateKeyFile=%s, privateKeyPassphrase=********, "
				.concat("knownHostsFile=%s, remoteDirectory=%s, timeout=%s]")
				.formatted(host, port, username, privateKeyFile, knownHostsFile, remoteDirectory, timeout);
		}
	}
}
