package fr.insee.arc.core.util;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class XMLParserSecurityUtils{
    
    private static final String HTTP_APACHE_ORG_XML_FEATURES_NONVALIDATING_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    private static final String HTTP_XML_ORG_SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    private static final String HTTP_XML_ORG_SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";

    private XMLParserSecurityUtils() {
	  throw new IllegalStateException("Utility class");
    }
    
    /**
     * Security configuration (protection against XXE)
     * 
     * @param saxParserFactory
     * @return
     * @throws ParserConfigurationException
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @throws SAXException
     */
    public static void parserSecurityConfiguration(SAXParserFactory saxParserFactory)
	    throws ParserConfigurationException, SAXException {
	saxParserFactory.setFeature(HTTP_XML_ORG_SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES, false);
	saxParserFactory.setFeature(HTTP_XML_ORG_SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES, false);
	saxParserFactory.setFeature(HTTP_APACHE_ORG_XML_FEATURES_NONVALIDATING_LOAD_EXTERNAL_DTD, false);
    }
    
    
}
