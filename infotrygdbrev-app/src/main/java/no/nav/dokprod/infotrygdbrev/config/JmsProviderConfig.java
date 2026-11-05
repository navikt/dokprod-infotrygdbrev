package no.nav.dokprod.infotrygdbrev.config;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.jakarta.jms.MQQueue;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokprod.infotrygdbrev.bdok100.config.DokprodInfotrygdbrevProperties;
import no.nav.dokprod.infotrygdbrev.util.MDCMessageConverter;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.JmsTemplate;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;

import javax.net.ssl.SSLSocketFactory;

@Configuration
@Slf4j
public class JmsProviderConfig {

	private static final int UTF_8_WITH_PUA = 1208;
	private static final String ANY_TLS13_OR_HIGHER = "*TLS13ORHIGHER";
	private final int MAX_ATTEMPTS = 20;

	@Bean
	public JmsTemplate jmsQueueTemplate(ConnectionFactory connectionFactory) {
		JmsTemplate template = new JmsTemplate();
		template.setConnectionFactory(connectionFactory);
		template.setPubSubDomain(false);
		template.setReceiveTimeout(20000L);
		template.setMessageConverter(mdcMessageConverter());
		return template;
	}

	@Bean
	public MDCMessageConverter mdcMessageConverter() {
		return new MDCMessageConverter();
	}

	@Bean
	public ConnectionFactory connectionFactory(DokprodInfotrygdbrevProperties dokprodInfotrygdbrevProperties) throws JMSException {
		MQConnectionFactory connectionFactory = new MQConnectionFactory();
		connectionFactory.setHostName(dokprodInfotrygdbrevProperties.qdok001().hostname());
		connectionFactory.setPort(dokprodInfotrygdbrevProperties.qdok001().port());
		connectionFactory.setQueueManager(dokprodInfotrygdbrevProperties.qdok001().queueManager());
		connectionFactory.setChannel(dokprodInfotrygdbrevProperties.qdok001().channel());
		connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
		connectionFactory.setCCSID(UTF_8_WITH_PUA);
		connectionFactory.setIntProperty(WMQConstants.JMS_IBM_ENCODING, MQConstants.MQENC_NATIVE);
		connectionFactory.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
		connectionFactory.setSSLFipsRequired(false);
		connectionFactory.setSSLCipherSuite(ANY_TLS13_OR_HIGHER);
		connectionFactory.setSSLSocketFactory(SSLSocketFactory.getDefault());

		UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
		adapter.setTargetConnectionFactory(connectionFactory);
		adapter.setUsername(dokprodInfotrygdbrevProperties.serviceuser().username());
		adapter.setPassword(dokprodInfotrygdbrevProperties.serviceuser().password());

		JmsPoolConnectionFactory pooledFactory = new JmsPoolConnectionFactory();
		pooledFactory.setConnectionFactory(adapter);
		pooledFactory.setMaxConnections(10);
		pooledFactory.setMaxSessionsPerConnection(10);
		return pooledFactory;
	}

	@Bean
	public Queue qdok001ProduserDokument(DokprodInfotrygdbrevProperties dokprodInfotrygdbrevProperties) throws JMSException {
		return new MQQueue(dokprodInfotrygdbrevProperties.qdok001().queueName());
	}

	@Bean
	ApplicationRunner warmupJms(ConnectionFactory connectionFactory) {
		return _ -> {
			int attempts = 0;
			do {
				try (var connection = connectionFactory.createConnection()) {
					connection.start();
					break;
				} catch (JMSException e) {
					++attempts;
					log.error("Failed to warm up JMS-connection (attempt {}/{}): {}", attempts, MAX_ATTEMPTS, e.getMessage(), e);
				}
			} while (attempts < MAX_ATTEMPTS);
		};
	}
}
