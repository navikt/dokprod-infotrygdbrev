package no.nav.dokprod_infotrygdbrev.xml.validation;

import org.springframework.core.io.Resource;

import java.io.File;

/**
 * Validates an XML
 *
 */
public interface XmlValidator {

	/**
	 * set xsdResource
	 *
	 * @param xsdResource the XSDResource
	 */
	void setXsdSchema(Resource xsdResource);

	/**
	 * Validates an XML file
	 *
	 * @param xmlFile the xml to runValidation
	 */
	void validate(File xmlFile);

	/**
	 * Validates an XML String
	 *
	 * @param string the xml to runValidation
	 */
	void validate(String string);
}
