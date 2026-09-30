package no.nav.dokprod_infotrygdbrev.xml.validation.support;

import no.nav.dokprod_infotrygdbrev.xml.validation.XmlValidationException;
import no.nav.dokprod_infotrygdbrev.xml.validation.XmlValidator;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Default implementation of XmlValidator
 *
 */
public class DefaultXmlValidator implements XmlValidator, InitializingBean {

	private Resource xsdResource;

	public void setXsdSchema(Resource xsdResource) {
		this.xsdResource = xsdResource;
	}

	@Override
	public void validate(File xmlFile) {
		try (InputStream inputStream = new FileInputStream(xmlFile)) {
			Validator validator = createValidator();
			validator.validate(new StreamSource(inputStream));
		} catch (Exception e) {
			throw new XmlValidationException("Xml validation failed for file:" + xmlFile.getAbsolutePath(), e);
		}
	}

	@Override
	public void validate(String s) {
		try (InputStream inputStream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8))) {
			Validator validator = createValidator();
			validator.validate(new StreamSource(inputStream));
		} catch (Exception e) {
			throw new XmlValidationException("Xml validation failed", e);
		}
	}

	private Validator createValidator() {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		try {
			schema = schemaFactory.newSchema(xsdResource.getURL());
		} catch (SAXException | IOException e) {
			throw new XmlValidationException("Unable to create XML schema validator", e);
		}

		Validator validator = schema.newValidator();
		validator.setErrorHandler(new SimpleSaxErrorHandler(LogFactory.getLog(DefaultXmlValidator.class)));
		return validator;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(xsdResource, "xsdResource can not be null");
	}
}
