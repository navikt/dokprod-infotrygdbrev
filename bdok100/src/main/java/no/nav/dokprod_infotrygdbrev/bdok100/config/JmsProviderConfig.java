package no.nav.dokprod_infotrygdbrev.bdok100.config;

import no.nav.dokprod_infotrygdbrev.util.MDCMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;

import jakarta.jms.ConnectionFactory;

import javax.naming.NamingException;

@Configuration
public class JmsProviderConfig {

	@Bean
	public JmsTemplate jmsQueueTemplate() {
		JmsTemplate template = new JmsTemplate();
		template.setConnectionFactory(connectionFactory());
		template.setDestinationResolver(jmsDestinationResolver());
		template.setPubSubDomain(false);
		template.setReceiveTimeout(20000L);
		template.setMessageConverter(mdcMessageConverter());
		return template;
	}

	@Bean
	public DynamicDestinationResolver jmsDestinationResolver() {
		return new DynamicDestinationResolver();
	}

	@Bean
	public MDCMessageConverter mdcMessageConverter() {
		return new MDCMessageConverter();
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		ConnectionFactory connectionFactory = getJndiObject("java:/jboss/mqConnectionFactory", ConnectionFactory.class);
		return connectionFactory;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getJndiObject(String jndiName, Class<T> expectedType) {
		JndiObjectFactoryBean factory = new JndiObjectFactoryBean();
		factory.setJndiName(jndiName);
		factory.setExpectedType(expectedType);
		try {
			factory.afterPropertiesSet();
		} catch (IllegalArgumentException | NamingException e) {
			throw new RuntimeException(e);
		}
		return (T) factory.getObject();
	}
}
