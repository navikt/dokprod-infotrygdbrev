package no.nav.dokprod.infotrygdbrev.config;

import no.nav.dokprod.infotrygdbrev.consumer.SakConsumer;
import no.nav.dokprod.infotrygdbrev.consumer.mock.PdlConsumerMock;
import no.nav.dokprod.infotrygdbrev.consumer.mock.SakConsumerMock;
import no.nav.dokprod.infotrygdbrev.consumer.pdl.PdlConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
}
