package fr.insee.arc.core.service.handler;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import fr.insee.arc.utils.format.Format;

/**
 * Classe utilisée pour gérer les événement émis par SAX lors du traitement du fichier XML
 */
public class XSDHandlerParser extends org.xml.sax.helpers.DefaultHandler {

    private static final Logger LOGGER = LogManager.getLogger(XSDHandlerParser.class);

  public XSDHandlerParser() {
    super();
  }

  public String currentTag;
  public String parentTag;
  public int order=0;
  public ArrayList<String> stack=new ArrayList<String>();
  MultiMap t = new MultiValueMap();

  HashSet<String> alreadySet= new HashSet<String>();
  
  
  /**
   * Actions à réaliser sur les données
   */
  public void characters(char[] caracteres, int debut, int longueur) {

//	  if (currentTag.equals("xs:element"))
//	  {
//		  String donnees = new String(caracteres, debut, longueur);
//		  System.out.println(">"+donnees);
//	  }
	  
//	  System.out.println(caracteres);
  
  }

  /**
   * Actions à réaliser lors de la fin du document XML.
   */
  public void endDocument() {
    System.out.println("Fin du document");
    System.out.println(t);
    
    
    String root="REGENT-XML";
    String bdRoot=Format.toBdRaw(root).toLowerCase();
    alreadySet.add(bdRoot);
    
    System.out.println("-- GO!!!");

    // CLEAN
  	System.out.println("delete from arc.ihm_controle_regle where version='v003'; commit;");

    // ALIAS
    System.out.println("insert into arc.ihm_controle_regle (id_norme,periodicite,validite_inf,validite_sup,version,id_classe,rubrique_pere,rubrique_fils,borne_inf,borne_sup,condition) values ('v2008','A','2020-01-01','2100-01-01','v003','ALIAS','v_"+bdRoot+"',null,null,null,'"+root+"'); COMMIT;");

    insertRule(root,bdRoot);
    
  }
  
  public void insertRule(String parent, String bdParent)
  {
	  if (t.get(parent)!=null)
	  { 
		  for (String u:(ArrayList<String>) t.get(parent))
			{
			  	this.order++;
//			  	String bdCurrent=Format.toBdRaw(u).toLowerCase()+"__"+Format.toBdRaw(parent).toLowerCase();

			  	String bdCurrent=Format.toBdRaw(u).toLowerCase();
			  	
			    if (!alreadySet.contains(bdCurrent))
			    {
			    // ALIAS
			    System.out.println("insert into arc.ihm_controle_regle (id_norme,periodicite,validite_inf,validite_sup,version,id_classe,rubrique_pere,rubrique_fils,borne_inf,borne_sup,condition) values ('v2008','A','2020-01-01','2100-01-01','v003','ALIAS','v_"+bdCurrent+"',null,null,null,'"+u+"'); COMMIT;");
			    }
			    
			    // CARDINALITY
			    if (!alreadySet.contains(bdCurrent+"#"+bdParent))
			    {
			    // pere fils
			    System.out.println("insert into arc.ihm_controle_regle (id_norme,periodicite,validite_inf,validite_sup,version,id_classe,rubrique_pere,rubrique_fils,borne_inf,borne_sup,condition) values ('v2008','A','2020-01-01','2100-01-01','v003','CARDINALITE','i_"+bdParent+"','i_"+bdCurrent+"',0,null,'"+this.order+"'); COMMIT;");
			    // fils pere
			    System.out.println("insert into arc.ihm_controle_regle (id_norme,periodicite,validite_inf,validite_sup,version,id_classe,rubrique_pere,rubrique_fils,borne_inf,borne_sup,condition) values ('v2008','A','2020-01-01','2100-01-01','v003','CARDINALITE','i_"+bdCurrent+"','i_"+bdParent+"',1,1,null); COMMIT;");
			    alreadySet.add(bdCurrent+"#"+bdParent);
			    }
			    
			    
			    
			    insertRule(u,bdCurrent);
			}
	  }
	  
	  else
	  // cas ou la rubrique est une feuille
	  {
		  	String bdCurrent=Format.toBdRaw(parent).toLowerCase();

		    // TYPE
		    System.out.println("insert into arc.ihm_controle_regle (id_norme,periodicite,validite_inf,validite_sup,version,id_classe,rubrique_pere,rubrique_fils,borne_inf,borne_sup,condition) values ('v2008','A','2020-01-01','2100-01-01','v003','ALPHANUM','v_"+bdCurrent+"',null,null,null,null); COMMIT;");
	  }
  }
  
  
  
  
  

  /**
   * Actions à réaliser lors de la détection de la fin d'un élément.
 * @throws SAXParseException
   */
  public void endElement(String uri, String localName, String qName) throws SAXParseException {

	  if (qName.equals("xs:element"))
	  {
		  stack.remove(stack.size()-1);
		  
	  }
	  
	  
//		System.out.println("</"+qName+">\n");

  }

  /**
   * Actions à réaliser au début du document.
   */
  public void startDocument() {
    System.out.println("Debut du document");
  }

  /**
   * Actions à réaliser lors de la détection d'un nouvel élément.
 * @throws SAXParseException
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXParseException {

	  
	  
	  
	  this.currentTag=qName;

//	  System.out.println(currentTag);

	  if (currentTag.equals("xs:element"))
	  {
		  String val=attributes.getValue("name")==null?attributes.getValue("ref"):attributes.getValue("name");
		  
		  if (stack.size()>0)
		  {
			  t.put(stack.get(stack.size()-1),val);
		  }
		  
		  stack.add(val);

		  System.out.println(stack);
	  }
  }

}