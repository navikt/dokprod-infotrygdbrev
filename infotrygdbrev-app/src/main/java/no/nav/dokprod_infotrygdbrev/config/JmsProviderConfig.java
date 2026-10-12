package no.nav.dokprod_infotrygdbrev.config;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.jakarta.jms.MQQueue;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import no.nav.dokprod_infotrygdbrev.util.MDCMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;

@Configuration
@EnableConfigurationProperties(JmsProperties.class)
public class JmsProviderConfig {

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
	public ConnectionFactory connectionFactory(JmsProperties properties) throws JMSException {
		MQConnectionFactory connectionFactory = new MQConnectionFactory();
		connectionFactory.setHostName(properties.hostname());
		connectionFactory.setPort(properties.port());
		connectionFactory.setQueueManager(properties.queueManager());
		connectionFactory.setChannel(properties.channel());
		connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
		return connectionFactory;
	}

	@Bean
	public Queue qdok001ProduserDokument(JmsProperties properties) throws JMSException {
		return new MQQueue(properties.queueName());
	}
}
