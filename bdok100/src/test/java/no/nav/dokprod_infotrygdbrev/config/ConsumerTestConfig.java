package no.nav.dokprod_infotrygdbrev.config;

import no.nav.dokprod_infotrygdbrev.consumer.SakConsumer;
import no.nav.dokprod_infotrygdbrev.consumer.mock.PdlConsumerMock;
import no.nav.dokprod_infotrygdbrev.consumer.mock.SakConsumerMock;
import no.nav.dokprod_infotrygdbrev.consumer.mock.StsConsumerMock;
import no.nav.dokprod_infotrygdbrev.consumer.pdl.PdlConsumer;
import no.nav.dokprod_infotrygdbrev.consumer.sts.StsConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Replaces the real {@link SakConsumer}, {@link PdlConsumer} and {@link StsConsumer} beans (which call out to
 * {@code sak.url}, {@code pdl.url} and {@code securitytokenservice.url} over HTTP, defaulting to
 * {@code http://localhost:8985} in tests) with in-memory mocks, so tests don't depend on an external/mocked HTTP
 * server actually listening on that port.
 */
@Configuration
public class ConsumerTestConfig {

	@Bean
	@Primary
	public SakConsumer sakConsumer() {
		return new SakConsumerMock();
	}

	@Bean
	@Primary
	public PdlConsumer pdlConsumer() {
		return new PdlConsumerMock();
	}

	@Bean
	@Primary
	public StsConsumer stsConsumer() {
		return new StsConsumerMock();
	}
}
