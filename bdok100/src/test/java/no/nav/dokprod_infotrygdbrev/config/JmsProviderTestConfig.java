package no.nav.dokprod_infotrygdbrev.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;

import jakarta.jms.ConnectionFactory;

@Configuration
public class JmsProviderTestConfig {

	@Bean
	public ConnectionFactory connectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory =
				new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false&broker.useJmx=false");
		return new CachingConnectionFactory(activeMQConnectionFactory);
	}
}
