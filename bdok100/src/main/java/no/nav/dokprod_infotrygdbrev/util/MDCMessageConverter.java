package no.nav.dokprod_infotrygdbrev.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;

/**
 * Klasse for å legge på/plukke av callId, userId og consumerId på message.
 */
public class MDCMessageConverter extends SimpleMessageConverter {

	protected static final Logger LOG = LoggerFactory.getLogger(MDCMessageConverter.class);

	@Override
	public Message toMessage(Object object, Session session) throws JMSException {
		Message message = super.toMessage(object, session);
		return addValuesToJMSHeader(message);
	}

	protected Message addValuesToJMSHeader(Message message) throws JMSException {
		addValueJMSHeader(message, MDCOperations.MDC_CALL_ID);
		addValueJMSHeader(message, MDCOperations.MDC_USER_ID);
		addValueJMSHeader(message, MDCOperations.MDC_CONSUMER_ID);
		return message;
	}

	protected Message addValueJMSHeader(Message message, String keyInMDC) throws JMSException {
		String valueInMDC = MDCOperations.getFromMDC(keyInMDC);
		if (valueInMDC == null) {
			throw new RuntimeException(keyInMDC + " skal være tilgjengelig i MDC på dette tidspunkt. Om du er en webapp, må du legge til et MDCFilter i web.xml " +
				"(oppskrift på dette: http://confluence.adeo.no/display/Modernisering/MDCFilter). " +
				"Om du er noe annet må du generere " + keyInMDC + " selv og legge på MDC. Hjelpemetoder finnes i no.nav.modig.common.MDCOperations.");
		}
		message.setStringProperty(keyInMDC, valueInMDC);

		LOG.debug("Add " + keyInMDC + " to JMS message: " + valueInMDC);

		return message;
	}

	@Override
	public Object fromMessage(Message message) throws JMSException {
		String callId = message.getStringProperty(MDCOperations.MDC_CALL_ID);
		MDCOperations.putToMDC(MDCOperations.MDC_CALL_ID, callId);
		String userId = message.getStringProperty(MDCOperations.MDC_USER_ID);
		MDCOperations.putToMDC(MDCOperations.MDC_USER_ID, userId);
		String consumerId = message.getStringProperty(MDCOperations.MDC_CONSUMER_ID);
		MDCOperations.putToMDC(MDCOperations.MDC_CONSUMER_ID, consumerId);

		return message;
	}
}
