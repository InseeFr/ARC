package fr.insee.arc.core.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.insee.arc.core.service.handler.FormatFichierHandler;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.SecuredSaxParser;


/**
 * Représentation java de l'arbre contenant la hierarchie du fichier.
 * Utiliser pour charger les fichiers clef valeur, et plat avec une hierachie (encore en proto)
 * 
 * L'arbre est représenter sous forme de map <enfant, parent>. Donc une feuille est présente uniquement sous forme de clef, alors
 * qu'une branche est présente sous forme d'une clef, et de une ou plusieurs valeurs
 * 
 * @author S4LWO8
 *
 */
public class ArbreFormat {

    //L'arbre
    private HashMap<String, String> arbreHierachieDuFichier;
    
    //éléments terminaux
    private ArrayList<String> feuilles = new ArrayList<>();
    
    //éléments intermédiaire
    private ArrayList<String> branches = new ArrayList<>();

    public ArbreFormat(Norme aNorme) throws ArcException {
        super();
        
        try {
        // Récupérer et parser le 'format' lié à la norme

        SAXParser saxParser = SecuredSaxParser.buildSecuredSaxParser();
        FormatFichierHandler formatHandler = new FormatFichierHandler();

        saxParser.parse(new InputSource(new StringReader(aNorme.getRegleChargement().getFormat())), formatHandler);

        this.arbreHierachieDuFichier = formatHandler.getArbre();
        calculerFeuilles();
        } catch (SAXException | IOException | ParserConfigurationException e)
        {
        	throw new ArcException(e, ArcExceptionMessage.XML_SAX_PARSING_FAILED,"spécifié dans le champ format des règles de chargement");
        }
        
    }
    
    /**
     * Une feuille est une éléments qui n'est présent que sous la forme de clef dans l'arbre
     */
    private void calculerFeuilles(){
        this.feuilles.clear();
        Collection<String> listeValeur = this.arbreHierachieDuFichier.values();
        for (String element : this.arbreHierachieDuFichier.keySet()) {
            if (!listeValeur.contains(element)) {
                this.feuilles.add(element.toUpperCase());
            } else {
                this.branches.add(element.toUpperCase());
            }
            
        }
        
        
    }
    
    /*
     * Retourne la liste des pères d'un élément
     */
    public ArrayList<String> getPeres (String fils){
        ArrayList<String> listePere = new ArrayList<String>();
        listePere.add(fils);
        String pere = this.arbreHierachieDuFichier.get(fils);
       
        while (pere != null && !pere.equalsIgnoreCase("root")) {
            listePere.add(pere);
            pere = arbreHierachieDuFichier.get(pere);
        }

        return listePere;
    }
    


    
    
    /**
     * @return the arbreFormat
     */
    public HashMap<String, String> getArbreFormat() {
        return arbreHierachieDuFichier;
    }

    /**
     * @param arbreFormat the arbreFormat to set
     */
    public void setArbreFormat(HashMap<String, String> arbreFormat) {
        this.arbreHierachieDuFichier = arbreFormat;
    }

    /**
     * @return the feuilles
     */
    public ArrayList<String> getFeuilles() {
        return feuilles;
    }

    /**
     * @param feuilles the feuilles to set
     */
    public void setFeuilles(ArrayList<String> feuilles) {
        this.feuilles = feuilles;
    }
    
    /**
     * @return the branches
     */
    public ArrayList<String> getBranches() {
        return branches;
    }

    /**
     * @param feuilles the feuilles to set
     */
    public void setBranches(ArrayList<String> branches) {
        this.branches = branches;
    }
    
    
}
