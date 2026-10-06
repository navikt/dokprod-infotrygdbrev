package no.nav.dokprod_infotrygdbrev.bdok100.config.bdok100;

import no.nav.dok.meldinger.virksomhet.dokumentproduksjon.ProduserIkkeRedigerbartDokument;
import no.nav.dokprod.batch.bdok100.brevdata44.xml.jaxb2.gen.Brevdata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MarshallingMessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import javax.naming.NamingException;

@Configuration
public class Bdok100JmsConfig {

	@Bean
	public JmsTemplate jmsBdok100Template(ConnectionFactory connectionFactory, Queue qdok001ProduserDokument, MarshallingMessageConverter bdok100JmsMessageConverter) {
		JmsTemplate template = new JmsTemplate();
		template.setConnectionFactory(connectionFactory);
		template.setDefaultDestination(qdok001ProduserDokument);
		template.setPubSubDomain(false);
		template.setMessageConverter(bdok100JmsMessageConverter);
		return template;
	}

	@Bean
	public MarshallingMessageConverter bdok100JmsMessageConverter(Marshaller bdok100WriterMarshaller) {
		MarshallingMessageConverter converter = new MarshallingMessageConverter(bdok100WriterMarshaller);
		converter.setTargetType(MessageType.TEXT);
		return converter;
	}

	@Bean
	public Marshaller bdok100WriterMarshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(ProduserIkkeRedigerbartDokument.class, Brevdata.class);
		return marshaller;
	}

	@Bean
	public Queue qdok001ProduserDokument() {
		return getJndiObject("java:/jboss/qdok001ProduserDokumentLav", Queue.class);
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
