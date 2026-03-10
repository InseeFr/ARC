package fr.insee.arc.core.service.p2chargement.xmlhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser pour lire le fichier contenant le format sous forme xml necessaire pour créer un fichier xml à partir
 * du fichier clef-valeur.
 * Il permet de créer une map de la forme (rubrique, père) qui permet à partir d'une rubrique de remonter jusqu'à son
 * père, et ainsi savoir qu'elles seront les balises à ouvrir et fermer.
 * 
 * Diagramme d'activité : <a href="https://gforge.insee.fr/forum/message.php?msg_id=5232&group_id=865"> ici </a>
 * 
 * @author S4LWO8
 *
 */
public class FormatFichierHandler extends DefaultHandler {
    
    
    //L'arbre de la forme, <enfant;parent>
    private Map<String, String> arbre = new HashMap<>();
    
    //la liste des pères de l'éléments courant
    private List<String> listePere = new ArrayList<>();

    @Override
    public void startDocument() throws SAXException {
    	// no handler action at document start
    }



    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String pere;
        //revient à voir si on est sur la première balise ou non
        //Si oui son père est null, sinon c'est son ... père...
        if (listePere.size()==0) {
            pere = null;
        } else {
            pere = listePere.get(listePere.size()-1);
        }
        // Avant de mettre à jour l'arbre on regarde si la rubrique est déjà présente. Si oui on soulève une erreur car
        // le formatage du fichiers n'est pas le bon car on a soit : des balises à des niveaux différents
        // avec le même nom (!!) soit des balises soeurs avec le même nom pour symboliser que se cas existe.
        // Si le deuxième cas ne va pas poser problème grâce à l'implémentation de la méthode put(K,V), le second lui si
        // Autant soulever une exception est mettre le fichier en erreur.
        if (arbre.containsKey(qName)) {
            throw new SAXParseException("Fichier de format non pris en compte, on retrouve plusieurs fois une même rubrique.","", "", 0, 0);
        }
        arbre.put(qName, pere);
        
        listePere.add(qName);
    }



    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        listePere.remove(qName);
    }



    @Override
    public void endDocument() throws SAXException {
    	// no handler action at document end
    }



	public Map<String, String> getArbre() {
		return arbre;
	}

	public void setArbre(Map<String, String> arbre) {
		this.arbre = arbre;
	}
    
}
