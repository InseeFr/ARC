package fr.insee.arc.utils.utils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class SecuredSaxParser {

	private SecuredSaxParser() {
		    throw new IllegalStateException("Utility class");
		  }

	/**
	 * build a secured sax parser external doctypes externe,
	 * external-parameter-entity and external-general-entity are disabled
	 * 
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static SAXParser buildSecuredSaxParser() throws ParserConfigurationException, SAXException {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		saxParserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		return saxParserFactory.newSAXParser();
	}

}
