package fr.insee.arc.core.service.handler;
import java.io.BufferedWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.utils.LoggerHelper;


/**
 * Classe utilisée pour gérer les événement émis par SAX lors du traitement du fichier XML
 */
public class XMLHandlerAno extends org.xml.sax.helpers.DefaultHandler {

    private static final Logger LOGGER = Logger.getLogger(XMLHandlerAno.class);

  public XMLHandlerAno() {
    super();
  }

  public String currentTag;

  public BufferedWriter bw;


  /**
   * boolean qui indique si on anonymise le fichier (true) ou pas
   */
  public Boolean doAnonymiser;


  boolean retourChariot=false;


  /**
   * Actions à réaliser sur les données
   */
  public void characters(char[] caracteres, int debut, int longueur) {
    String donnees = new String(caracteres, debut, longueur);
   if (donnees.getBytes()[0]!=10){

//	   if (currentTag.equals("n4ds:S21_G00_40_006"))
//	   {
//	   System.out.println(currentTag+" = *" + donnees + "* ");
//	   }

//	   donnees=StringEscapeUtils.escapeHtml(donnees);

	   if (donnees.equals("&"))
	   {
		   donnees="&amp;";
	   }

	   if (donnees.equals("<"))
	   {
		   donnees="&lt;";
	   }

	   if (donnees.equals(">"))
	   {
		   donnees="&gt;";
	   }

		try {

			String donneesAno=donnees;


			if (this.doAnonymiser)
			{
			// anonymisation du contact
			if (this.currentTag.equals("n4ds:S10_G00_02_002"))
			{
				donneesAno="NOM PRENOM CONTACT EMMETEUR";
			}

			if (this.currentTag.equals("n4ds:S10_G00_02_004"))
			{
				donneesAno="PRENOM.NOM@CONTACT-EMMETEUR.COM";
			}

			if (this.currentTag.equals("n4ds:S10_G00_02_005"))
			{
				donneesAno="TELEPHONE CONTACT EMMETEUR";
			}

			if (this.currentTag.equals("n4ds:S10_G00_02_006"))
			{
				donneesAno="FAX CONTACT EMMETEUR";
			}


			// anonymisation du contact dsn
			if (this.currentTag.equals("n4ds:S10_G00_03_003"))
			{
				donneesAno="CTA-CONTACTDSN@CONTACT_DSN.COM";
			}

			if (this.currentTag.equals("n4ds:S10_G00_95_001"))
			{
				donneesAno="NOM DSN";
			}

			if (this.currentTag.equals("n4ds:S10_G00_95_002"))
			{
				donneesAno="PRENOM DSN";
			}

			if (this.currentTag.equals("n4ds:S10_G00_95_901"))
			{
				donneesAno="CTA-CONTACTDSN@CONTACT_DSN.COM";
			}


			// anonymisation du contact chez le déclaré
			if (this.currentTag.equals("n4ds:S20_G00_07_001"))
			{
				donneesAno="NOM PRENOM CONTACT DECLARE";
			}

			if (this.currentTag.equals("n4ds:S20_G00_07_002"))
			{
				donneesAno="TELEPHONE CONTACT DECLARE";
			}

			if (this.currentTag.equals("n4ds:S20_G00_07_003"))
			{
				donneesAno="PRENOM.NOM@CONTACT-DECLARE.COM";
			}


			//nir
			if (this.currentTag.equals("n4ds:S21_G00_30_001"))
			{
				donneesAno="NIR#"+Format.getHash(donnees);
			}

			if (this.currentTag.equals("n4ds:S21_G00_30_002"))
			{
				donneesAno="NOM#"+Format.getHash(donnees);
			}

			if (this.currentTag.equals("n4ds:S21_G00_30_003"))
			{
				donneesAno="NOM_USAGE#"+Format.getHash(donnees);
			}

			if (this.currentTag.equals("n4ds:S21_G00_30_004"))
			{
				donneesAno="PRENOM#"+Format.getHash(donnees);
			}


			// anonymisation ancien salarié
			if (this.currentTag.equals("n4ds:S21_G00_31_008"))
			{

				donneesAno="NIR#"+Format.getHash(donnees);
			}

			if (this.currentTag.equals("n4ds:S21_G00_31_009"))
			{
				donneesAno="NOM#"+Format.getHash(donnees);
			}

			if (this.currentTag.equals("n4ds:S21_G00_31_010"))
			{
				donneesAno="PRENOM#"+Format.getHash(donnees);
			}
			}

			this.bw.write(donneesAno);


		} catch (IOException ex) {
			LoggerHelper.error(LOGGER, ex, "index()");
		} catch (NoSuchAlgorithmException ex) {
			LoggerHelper.error(LOGGER, ex, "index()");
		}

   }
  }

  /**
   * Actions à réaliser lors de la fin du document XML.
   */
  public void endDocument() {
//    System.out.println("Fin du document");
    try {
	    this.bw.flush();
    } catch (IOException ex) {
	    LoggerHelper.error(LOGGER, ex, "index()");
    }
  }

  /**
   * Actions à réaliser lors de la détection de la fin d'un élément.
 * @throws SAXParseException
   */
  public void endElement(String uri, String localName, String qName) throws SAXParseException {
	  try {
		this.bw.write("</"+qName+">\n");
		this.retourChariot=true;
	} catch (IOException ex) {
		LoggerHelper.error(LOGGER, ex, "index()");
	}
  }

  /**
   * Actions à réaliser au début du document.
   */
  public void startDocument() {
//    System.out.println("Debut du document");

	try {
//		bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
		this.bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	} catch (IOException ex) {
		LoggerHelper.error(LOGGER, ex, "index()");
	}

  }

  /**
   * Actions à réaliser lors de la détection d'un nouvel élément.
 * @throws SAXParseException
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXParseException {

	  if (this.currentTag!=null && !qName.equals(this.currentTag) && !this.retourChariot)
	  {
		  try {
			  this.bw.write("\n");
		  } catch (IOException ex) {
			  LoggerHelper.error(LOGGER, ex, "index()" );
		  }
	  }

	  this.retourChariot=false;
	  this.currentTag=qName;

	try {
		this.bw.write("<"+this.currentTag);
	} catch (IOException ex) {
		LoggerHelper.error(LOGGER, ex, "index()");
	}

    for (int i = 0; i < attributes.getLength(); i++) {
    	try {
			this.bw.write(" "+attributes.getQName(i)+"=\""+attributes.getValue(i)+"\"");
		} catch (IOException ex) {
			LoggerHelper.error(LOGGER, ex, "index()");
		}
 //  	System.out.println("attribut "+i+" - "+atts.getName(i)+" : "+atts.getValue(i));
      }
    try {
		this.bw.write(">");
	} catch (IOException ex) {
		LoggerHelper.error(LOGGER, ex, "index()");
	}
  }

}