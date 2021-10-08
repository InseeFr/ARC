package fr.insee.arc.core.service.engine.xsd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import fr.insee.arc.core.service.handler.XSDHandlerParser;

public class XsdParseService {

public void run() throws Exception
{
	
	try(FileInputStream tmpInx=new FileInputStream(new File("my_xsd_file.xsd")))
	{
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
	    XSDHandlerParser handler = new XSDHandlerParser();
	    SAXParser saxParser = saxParserFactory.newSAXParser();
	    saxParser.parse(tmpInx, handler);
	}
}
	
	
}
