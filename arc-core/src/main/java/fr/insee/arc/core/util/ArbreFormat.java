package fr.insee.arc.core.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import fr.insee.arc.core.service.handler.FormatFichierHandler;


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
    private HashMap<String, String> arbreFormat;
    
    //éléments terminaux
    private ArrayList<String> feuilles = new ArrayList<String>();
    
    //éléments intermédiaire
    private ArrayList<String> branches = new ArrayList<String>();

    public ArbreFormat(Norme aNorme) throws Exception {
        super();
        
        // Récupérer le 'format' lié à la norme
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        FormatFichierHandler formatHandler = new FormatFichierHandler();

        saxParser.parse(new InputSource(new StringReader(aNorme.getRegleChargement().getFormat())), formatHandler);

        this.arbreFormat = formatHandler.getArbre();
        calculerFeuilles();
        
    }
    
    /**
     * Une feuille est une éléments qui n'est présent que sous la forme de clef dans l'arbre
     */
    private void calculerFeuilles(){
        this.feuilles.clear();
        Collection<String> listeValeur = this.arbreFormat.values();
        for (String element : this.arbreFormat.keySet()) {
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
        String pere = this.arbreFormat.get(fils);
       
        while (pere != null && !pere.equalsIgnoreCase("root")) {
            listePere.add(pere);
            pere = arbreFormat.get(pere);
        }

        return listePere;
    }
    


    
    
    /**
     * @return the arbreFormat
     */
    public HashMap<String, String> getArbreFormat() {
        return arbreFormat;
    }

    /**
     * @param arbreFormat the arbreFormat to set
     */
    public void setArbreFormat(HashMap<String, String> arbreFormat) {
        this.arbreFormat = arbreFormat;
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
