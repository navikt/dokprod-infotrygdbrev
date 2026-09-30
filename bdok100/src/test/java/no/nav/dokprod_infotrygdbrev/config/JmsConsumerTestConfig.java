package no.nav.dokprod_infotrygdbrev.config;

import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.ProduserIkkeRedigerbartDokument;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.Brevdata;
import no.nav.dokprod_infotrygdbrev.jms.TestQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MarshallingMessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;

@Configuration
public class JmsConsumerTestConfig {

	@Bean
	public JmsTemplate jmsBdok100Template(ConnectionFactory connectionFactory, Queue qdok001ProduserDokument, Marshaller bdok100WriterMarshaller) {
		JmsTemplate template = new JmsTemplate();
		template.setConnectionFactory(connectionFactory);
		template.setDefaultDestination(qdok001ProduserDokument);
		template.setPubSubDomain(false);
		MarshallingMessageConverter converter = new MarshallingMessageConverter(bdok100WriterMarshaller);
		converter.setTargetType(MessageType.TEXT);
		template.setMessageConverter(converter);
		return template;
	}

	@Bean
	public Marshaller bdok100WriterMarshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(ProduserIkkeRedigerbartDokument.class, Brevdata.class);
		return marshaller;
	}

	@Bean
	public Queue qdok001ProduserDokument(@Value("${qdok001ProduserDokument}") String queuename) {
		return new TestQueue(queuename);
	}
}
