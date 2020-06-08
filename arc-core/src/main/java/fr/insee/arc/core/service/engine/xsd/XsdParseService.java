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
	
	FileInputStream tmpInx=new FileInputStream(new File("D:\\SauvNT\\EXPERTISE\\SIRENE4\\NormeEDI-CFE\\Message_REGENT_V2008-11.xsd"));
	
	SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    XSDHandlerParser handler = new XSDHandlerParser();
    SAXParser saxParser = saxParserFactory.newSAXParser();
    saxParser.parse(tmpInx, handler);
    tmpInx.close();
}
	
	
}
