package fr.insee.arc.core.serviceinteractif.ddi;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import fr.insee.arc.utils.exception.ArcException;

public class DDIParser {

	private DDIParser() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * parse the stream from a ddi input file
	 * 
	 * @param ddiFileInputStream
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static DDIModeler parse(InputStream ddiFileInputStream) throws ArcException {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser;
		try {
			
			// security "Disable access to external entities in XML parsing"
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			saxParser = factory.newSAXParser();

			DDIHandler ddiHandler = new DDIHandler();

			saxParser.parse(ddiFileInputStream, ddiHandler); // parser lisant le fichier

			DDIModeler ddiModeler = new DDIModeler();

			ddiModeler.model(ddiHandler); // modeler valorisant le modèle parsé pour pouvoir l'insérer dans ARC

			return ddiModeler;

		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new ArcException("DDI parsing failed");
		}

	}

}
