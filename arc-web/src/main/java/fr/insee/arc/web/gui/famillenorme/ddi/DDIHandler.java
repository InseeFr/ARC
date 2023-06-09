package fr.insee.arc.web.gui.famillenorme.ddi;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.insee.arc.core.model.ddi.DDIDatabase;
import fr.insee.arc.core.model.ddi.DDIRepresentedVariable;
import fr.insee.arc.core.model.ddi.DDITable;
import fr.insee.arc.core.model.ddi.DDIVariable;
import fr.insee.arc.core.model.ddi.DDIVariableOfTable;

/**
 * Classe handler intégrant un modèle de données décrit dans un XML DDI dans le modèle Java {@code ddi}
 * 
 * @author Z84H10
 *
 */
public class DDIHandler extends DefaultHandler {

	/**
	 * Liste alimentée des modèles de données lues par le handler.
	 */
    private final List<DDIDatabase> listDDIDatabases = new ArrayList<>();
    /**
     * Liste alimentée des tables de données lues par le handler.
     */
    private final List<DDITable> listDDITables = new ArrayList<>();
    /**
     * Liste alimentée des liens variable-table lues par le handler.
     */
    private final List<DDIVariableOfTable> listDDIVariableOfTables = new ArrayList<>();
    /**
     * Liste alimentée des variables lues par le handler.
     */
    private final List<DDIVariable> listDDIVariables = new ArrayList<>();
    /**
     * Liste alimentée des concepts de variables lues par le handler.
     */
    private final List<DDIRepresentedVariable> listDDIRepresentedVariables = new ArrayList<>();
    private DDIDatabase constructedDDIDatabase;
    private DDITable constructedDDITable;
    private DDIVariableOfTable constructedDDIVariableOfTable;
    private DDIVariable constructedDDIVariable;
    private DDIRepresentedVariable constructedDDIRepresentedVariable;
    private boolean bDatabase = false;
    private boolean bTable = false;
    private boolean bVariableOfTable = false;
    private boolean bVariable = false;
    private boolean bRepresentedVariableReference = false;
    private boolean bRepresentedVariable = false;
    private boolean bDescription = false;
    private String content;

    @Override
    public void startDocument ()
    {
      // Do nothing because nothing is needed.
    }

    @Override
    public void startElement(String uri, String localName,
                             String qName, Attributes attributes) throws SAXException
    {
        switch (qName) {
            case "DataRelationship":
                bDatabase = true;
                constructedDDIDatabase = new DDIDatabase();
                break;
            case "LogicalRecord":
                bTable = true;
                constructedDDITable = new DDITable();
                break;
            case "VariableUsedReference":
                bVariableOfTable = true;
                constructedDDIVariableOfTable = new DDIVariableOfTable();
                break;
            case "Variable":
                bVariable = true;
                constructedDDIVariable = new DDIVariable();
                break;
            case "RepresentedVariableReference":
            	bRepresentedVariableReference = true;
            	break;
            case "RepresentedVariable":
                bRepresentedVariable = true;
                constructedDDIRepresentedVariable = new DDIRepresentedVariable();
                break;
            case "r:Description":
            	bDescription = true;
            	break;
            default :
        }
    }

    @Override
    public void characters(char[] ch, int start,
                           int length) throws SAXException
    {
        this.content = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName,
                           String qName) throws SAXException
    {
        switch (qName) {
            case "r:ID": // Identifiants
                if (bDatabase && constructedDDIDatabase.getId() == null && !bTable && !bVariableOfTable) { // ID d'une database
                    this.constructedDDIDatabase.setId(content);
                } else if (bDatabase && bTable && !bVariableOfTable) { // ID d'une table + ajout de l'ID de sa database
                    this.constructedDDITable.setIdTable(content);
                    this.constructedDDITable.setIdDatabase(this.constructedDDIDatabase.getId());
                } else if (bDatabase && bTable) { // construction de variable of table avec ID variable + ID table
                    this.constructedDDIVariableOfTable.setIdVariable(content);
                    this.constructedDDIVariableOfTable.setIdTable(this.constructedDDITable.getIdTable());
                } else if (bVariable && !bRepresentedVariableReference) { // ID d'une variable
                    this.constructedDDIVariable.setIdVariable(content);
                } else if (bVariable) { // ID de la represented variable d'une variable
                    this.constructedDDIVariable.setIdRepresentedVariable(content);
                } else if (bRepresentedVariable && constructedDDIRepresentedVariable.getId() == null) { // ID d'une represented variable
                    this.constructedDDIRepresentedVariable.setId(content);
                }
                break;
            case "r:Content": // Labels et descriptions
                if (!bDescription) { // Labels
                    if (bDatabase && constructedDDIDatabase.getLabel() == null && !bTable) { // label d'une database
                        this.constructedDDIDatabase.setLabel(content);
                    } else if (bDatabase && bTable) { // label d'une table
                        this.constructedDDITable.setLabel(content);
                    } else if (bVariable) { // label d'une variable
                        this.constructedDDIVariable.setLabel(content);
                    } else if (bRepresentedVariable) { // label d'une represented variable
                        this.constructedDDIRepresentedVariable.setLabel(content);
                    }
                } else { // Descriptions
                    if (bTable) { // description d'une table
                        this.constructedDDITable.setDescription(content);
                    } else if (bRepresentedVariable) { // description d'une represented variable
                        this.constructedDDIRepresentedVariable.setDescription(content);
                    }
                }
                break;
            case "r:String": // Noms des objets
                if (bDatabase && constructedDDIDatabase.getDbName() == null && !bTable) { // nom d'une database
                    this.constructedDDIDatabase.setDbName(content);
                } else if (bDatabase && bTable) { // nom d'une table
                    this.constructedDDITable.setTableName(content);
                } else if (bVariable) { // nom d'une variable
                    this.constructedDDIVariable.setVariableName(content);
                } else if (bRepresentedVariable) { // nom d'une represented variable
                    this.constructedDDIRepresentedVariable.setRepresentedVariableName(content);
                }
                break;
            case "r:TextRepresentation": // represented variable de type text
                this.constructedDDIRepresentedVariable.setType("Text");
                break;
            case "r:NumericTypeCode": // le contenu de r:NumericTypeCode détermine si le nombre est entier ou flottant
                this.constructedDDIRepresentedVariable.setType(content);
                break;
            case "r:CodeRepresentation": // represented variable de type code
                this.constructedDDIRepresentedVariable.setType("Code");
                break;
            case "r:DateTimeRepresentation": // represented variable de type datetime
                this.constructedDDIRepresentedVariable.setType("DateTime");
                break;
            case "DataRelationship": // fin de construction d'une database
                bDatabase = false;
                this.listDDIDatabases.add(constructedDDIDatabase);
                break;
            case "LogicalRecord": // fin de construction d'une table
                bTable = false;
                this.listDDITables.add(constructedDDITable);
                break;
            case "VariableUsedReference": // fin de construction d'une variable of table
                bVariableOfTable = false;
                this.listDDIVariableOfTables.add(constructedDDIVariableOfTable);
                break;
            case "Variable": // fin de construction d'une variable
                bVariable = false;
                this.listDDIVariables.add(constructedDDIVariable);
                break;
            case "RepresentedVariableReference": // fin de represented variable reference dans une variable
                bRepresentedVariableReference = false;
                break;
            case "RepresentedVariable": // fin de construction d'une represented variable
                bRepresentedVariable = false;
                this.listDDIRepresentedVariables.add(constructedDDIRepresentedVariable);
                break;
            case "r:Description": // fin de description
                bDescription = false;
                break;
            default :
        }
    }

    @Override
    public void endDocument ()
    {
    	// Do nothing because nothing is needed.
    }

    public List<DDIDatabase> getListDDIDatabases() {
        return listDDIDatabases;
    }

    public List<DDITable> getListDDITables() {
        return listDDITables;
    }

    public List<DDIVariableOfTable> getListDDIVariableOfTables() {
        return listDDIVariableOfTables;
    }

    public List<DDIVariable> getListDDIVariables() {
        return listDDIVariables;
    }

    public List<DDIRepresentedVariable> getListDDIRepresentedVariables() {
        return listDDIRepresentedVariables;
    }
    
    /**
     * Dans la liste des {@code DDIDatabase}s lus par le handler, retourne s'il existe celui dont l'identifiant est précisé en entrée.
     * @param id l'identifiant du {@code DDIDatabase} recherché
     * @return {@code DDIDatabase} d'identifiant donné en entrée s'il existe, {@code null} sinon
     */
    public DDIDatabase getDDIDatabaseByID(String id) {
    	int j = 0;
        while (j < this.getListDDIDatabases().size() && !id.equals(this.getListDDIDatabases().get(j).getId())) {
        	j++;
        }
        return (j < this.getListDDIDatabases().size() ? this.getListDDIDatabases().get(j) : null);
    }
    
    /**
     * Dans la liste des {@code DDITable}s lus par le handler, retourne s'il existe celui dont l'identifiant est précisé en entrée.
     * @param id l'identifiant du {@code DDITable} recherché
     * @return {@code DDITable} d'identifiant donné en entrée s'il existe, {@code null} sinon
     */
    public DDITable getDDITableByID(String id) {
    	int j = 0;
        while (j < this.getListDDITables().size() && !id.equals(this.getListDDITables().get(j).getIdTable())) {
        	j++;
        }
        return (j < this.getListDDITables().size() ? this.getListDDITables().get(j) : null);
    }
    
    /**
     * Dans la liste des {@code DDIVariable}s lus par le handler, retourne s'il existe celui dont l'identifiant est précisé en entrée.
     * @param id l'identifiant du {@code DDIVariable} recherché
     * @return {@code DDIVariable} d'identifiant donné en entrée s'il existe, {@code null} sinon
     */
    public DDIVariable getDDIVariableByID(String id) {
    	int j = 0;
        while (j < this.getListDDIVariables().size() && !id.equals(this.getListDDIVariables().get(j).getIdVariable())) {
        	j++;
        }
        return (j < this.getListDDIVariables().size() ? this.getListDDIVariables().get(j) : null);
    }
    
    /**
     * Dans la liste des {@code DDIRepresentedVariable}s lus par le handler, retourne s'il existe celui dont l'identifiant est précisé en entrée.
     * @param id l'identifiant du {@code DDIRepresentedVariable} recherché
     * @return {@code DDIRepresentedVariable} d'identifiant donné en entrée s'il existe, {@code null} sinon
     */
    public DDIRepresentedVariable getDDIRepresentedVariableByID(String id) {
    	int j = 0;
        while (j < this.getListDDIRepresentedVariables().size() && !id.equals(this.getListDDIRepresentedVariables().get(j).getId())) {
        	j++;
        }
        return (j < this.getListDDIRepresentedVariables().size() ? this.getListDDIRepresentedVariables().get(j) : null);
    }
    
}
