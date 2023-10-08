package fr.insee.arc.core.service.p2chargement.xmlhandler;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.insee.arc.core.model.XMLConstant;
import fr.insee.arc.core.service.p2chargement.bo.FileIdCard;
import fr.insee.arc.core.service.p2chargement.bo.NormeRules;
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
    private Map<String, String> arbreHierachieDuFichier;
    
    //éléments terminaux
    private List<String> feuilles = new ArrayList<>();
    
    //éléments intermédiaire
    private List<String> branches = new ArrayList<>();

    public ArbreFormat(FileIdCard fileIdCard) throws ArcException {
        super();
        
        try {
        // Récupérer et parser le 'format' lié à la norme

        SAXParser saxParser = SecuredSaxParser.buildSecuredSaxParser();
        FormatFichierHandler formatHandler = new FormatFichierHandler();

        saxParser.parse(new InputSource(new StringReader(fileIdCard.getRegleChargement().getFormat())), formatHandler);

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
    public List<String> getPeres (String fils){
        List<String> listePere = new ArrayList<>();
        listePere.add(fils);
        String pere = this.arbreHierachieDuFichier.get(fils);
       
        while (pere != null && !pere.equalsIgnoreCase(XMLConstant.ROOT)) {
            listePere.add(pere);
            pere = arbreHierachieDuFichier.get(pere);
        }

        return listePere;
    }

    
    /**
     * @return the arbreFormat
     */
    public Map<String, String> getArbreFormat() {
        return arbreHierachieDuFichier;
    }

    /**
     * @param arbreFormat the arbreFormat to set
     */
    public void setArbreFormat(Map<String, String> arbreFormat) {
        this.arbreHierachieDuFichier = arbreFormat;
    }

    /**
     * @return the feuilles
     */
    public List<String> getFeuilles() {
        return feuilles;
    }

    /**
     * @param feuilles the feuilles to set
     */
    public void setFeuilles(List<String> feuilles) {
        this.feuilles = feuilles;
    }
    
    /**
     * @return the branches
     */
    public List<String> getBranches() {
        return branches;
    }

    /**
     * @param feuilles the feuilles to set
     */
    public void setBranches(List<String> branches) {
        this.branches = branches;
    }
    
    
}
