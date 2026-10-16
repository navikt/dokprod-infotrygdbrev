package no.nav.dokprod.infotrygdbrev.config;

import com.ibm.mq.jakarta.jms.MQConnectionFactory;
import com.ibm.mq.jakarta.jms.MQQueue;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import no.nav.dokprod.infotrygdbrev.bdok100.config.DokprodInfotrygdbrevProperties;
import no.nav.dokprod.infotrygdbrev.util.MDCMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;

@Configuration
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
	public ConnectionFactory connectionFactory(DokprodInfotrygdbrevProperties dokprodInfotrygdbrevProperties) throws JMSException {
		MQConnectionFactory connectionFactory = new MQConnectionFactory();
		connectionFactory.setHostName(dokprodInfotrygdbrevProperties.qdok001().hostname());
		connectionFactory.setPort(dokprodInfotrygdbrevProperties.qdok001().port());
		connectionFactory.setQueueManager(dokprodInfotrygdbrevProperties.qdok001().queueManager());
		connectionFactory.setChannel(dokprodInfotrygdbrevProperties.qdok001().channel());
		connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
		return connectionFactory;
	}

	@Bean
	public Queue qdok001ProduserDokument(DokprodInfotrygdbrevProperties dokprodInfotrygdbrevProperties) throws JMSException {
		return new MQQueue(dokprodInfotrygdbrevProperties.qdok001().queueName());
	}
}
