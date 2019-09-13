package fr.insee.arc.core.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.util.XmlConstants;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.insee.arc.core.model.Norme;
import fr.insee.arc.core.service.handler.FormatFichierHandler;


/**
 * java representation of the tree containing the model of the file.
 * Used to load key:value files
 * 
 * The tree is a map<child, parent>. So a terminal leaf is only a key, and branch is a key and a multiple values
 * 
 * @author Rémi Pépin
 *
 */
public class CustomTreeFormat {

    private Map<String, String> theTree;
    
    //éléments terminaux
    private List<String> endLeaves = new ArrayList<>();
    
    //éléments intermédiaire
    private List<String> branches = new ArrayList<>();

    public CustomTreeFormat(Norme aNorme) throws ParserConfigurationException, SAXException, IOException {
        super();
        
        // get the format of the norme
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        // Securing the parsing by disallowing the use of a DTD
        saxParserFactory.setFeature(XmlConstants.FEATURE_DISALLOW_DTD, true);
        XMLParserSecurityUtils.parserSecurityConfiguration(saxParserFactory);
        SAXParser saxParser = saxParserFactory.newSAXParser();
        FormatFichierHandler formatHandler = new FormatFichierHandler();

        formatHandler.nomNorme = aNorme.getIdNorme();
        saxParser.parse(new InputSource(new StringReader(aNorme.getRegleChargement().getFormat())), formatHandler);

        this.theTree = formatHandler.arbre;
        findLeafs();
        
    }
    
    /**
     * A leaf is a element which is only a key in the key:value representation
     */
    private void findLeafs(){
        this.endLeaves.clear();
        Collection<String> listValue = this.theTree.values();
        for (String element : this.theTree.keySet()) {
            if (!listValue.contains(element)) {
                this.endLeaves.add(element.toUpperCase());
            } else {
                this.branches.add(element.toUpperCase());
            }
            
        }
        
        
    }
    
    /*
     * get all the ancestor of an element
     */
    public List<String> getAncestors (String element){
        List<String> listAncestor = new ArrayList<>();
        listAncestor.add(element);
        String parent = this.theTree.get(element);
       
        while (parent != null && !parent.equalsIgnoreCase("root")) {
            listAncestor.add(parent);
            parent = theTree.get(parent);
        }

        return listAncestor;
    }

    public Map<String, String> getTheTree() {
        return theTree;
    }

    public void setTheTree(Map<String, String> theTree) {
        this.theTree = theTree;
    }

    public List<String> getEndLeaves() {
        return endLeaves;
    }

    public void setEndLeaf(List<String> endLeaf) {
        this.endLeaves = endLeaf;
    }

    public List<String> getBranches() {
        return branches;
    }

    public void setBranches(List<String> branches) {
        this.branches = branches;
    }
    

}
