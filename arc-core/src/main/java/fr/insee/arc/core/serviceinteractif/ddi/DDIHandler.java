package fr.insee.arc.core.serviceinteractif.ddi;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.insee.arc.core.model.ddi.DDIDatabase;
import fr.insee.arc.core.model.ddi.DDIRepresentedVariable;
import fr.insee.arc.core.model.ddi.DDITable;
import fr.insee.arc.core.model.ddi.DDIVariable;
import fr.insee.arc.core.model.ddi.DDIVariableOfTable;

import java.util.ArrayList;
import java.util.List;

public class DDIHandler extends DefaultHandler {

    private final List<DDIDatabase> listDDIDatabases = new ArrayList<>();
    private final List<DDITable> listDDITables = new ArrayList<>();
    private final List<DDIVariableOfTable> listDDIVariableOfTables = new ArrayList<>();
    private final List<DDIVariable> listDDIVariables = new ArrayList<>();
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

    public void startDocument ()
    {

    }

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
        }
    }

    public void characters(char[] ch, int start,
                           int length) throws SAXException
    {
        this.content = new String(ch, start, length);
    }

    public void endElement(String uri, String localName,
                           String qName) throws SAXException
    {
        switch (qName) {
            case "r:ID": // Identifiants
                if (bDatabase && constructedDDIDatabase.getId() == null && !bTable && !bVariableOfTable) {
                    this.constructedDDIDatabase.setId(content);
                } else if (bDatabase && bTable && !bVariableOfTable) {
                    this.constructedDDITable.setIdTable(content);
                    this.constructedDDITable.setIdDatabase(this.constructedDDIDatabase.getId());
                } else if (bDatabase && bTable) {
                    this.constructedDDIVariableOfTable.setIdVariable(content);
                    this.constructedDDIVariableOfTable.setIdTable(this.constructedDDITable.getIdTable());
                } else if (bVariable && !bRepresentedVariableReference) {
                    this.constructedDDIVariable.setIdVariable(content);
                } else if (bVariable) {
                    this.constructedDDIVariable.setIdRepresentedVariable(content);
                } else if (bRepresentedVariable && constructedDDIRepresentedVariable.getId() == null) {
                    this.constructedDDIRepresentedVariable.setId(content);
                }
                break;
            case "r:Content": // Labels et descriptions
                if (!bDescription) { // Labels
                    if (bDatabase && constructedDDIDatabase.getLabel() == null && !bTable) {
                        this.constructedDDIDatabase.setLabel(content);
                    } else if (bDatabase && bTable) {
                        this.constructedDDITable.setLabel(content);
                    } else if (bVariable) {
                        this.constructedDDIVariable.setLabel(content);
                    } else if (bRepresentedVariable) {
                        this.constructedDDIRepresentedVariable.setLabel(content);
                    }
                } else { // Descriptions
                    if (bTable) {
                        this.constructedDDITable.setDescription(content);
                    } else if (bRepresentedVariable) {
                        this.constructedDDIRepresentedVariable.setDescription(content);
                    }
                }
                break;
            case "r:String": // Noms des objets
                if (bDatabase && constructedDDIDatabase.getDbName() == null && !bTable) {
                    this.constructedDDIDatabase.setDbName(content);
                } else if (bDatabase && bTable) {
                    this.constructedDDITable.setTableName(content);
                } else if (bVariable) {
                    this.constructedDDIVariable.setVariableName(content);
                } else if (bRepresentedVariable) {
                    this.constructedDDIRepresentedVariable.setRepresentedVariableName(content);
                }
                break;
            case "r:TextRepresentation":
                this.constructedDDIRepresentedVariable.setType("Text");
                break;
            case "r:NumericRepresentation":
                this.constructedDDIRepresentedVariable.setType("Numeric");
                break;
            case "r:CodeRepresentation":
                this.constructedDDIRepresentedVariable.setType("Category");
                break;
            case "r:DateTimeRepresentation":
                this.constructedDDIRepresentedVariable.setType("DateTime");
                break;
            case "DataRelationship":
                bDatabase = false;
                this.listDDIDatabases.add(constructedDDIDatabase);
                break;
            case "LogicalRecord":
                bTable = false;
                this.listDDITables.add(constructedDDITable);
                break;
            case "VariableUsedReference":
                bVariableOfTable = false;
                this.listDDIVariableOfTables.add(constructedDDIVariableOfTable);
                break;
            case "Variable":
                bVariable = false;
                this.listDDIVariables.add(constructedDDIVariable);
                break;
            case "RepresentedVariableReference":
                bRepresentedVariableReference = false;
                break;
            case "RepresentedVariable":
                bRepresentedVariable = false;
                this.listDDIRepresentedVariables.add(constructedDDIRepresentedVariable);
                break;
            case "r:Description":
                bDescription = false;
                break;
        }
    }

    public void endDocument ()
    {

    }

    public List<DDIDatabase> getListDDIDatabases() {
        return listDDIDatabases;
    }

    public List<DDITable> getListTables() {
        return listDDITables;
    }

    public List<DDIVariableOfTable> getListDDIVariableOfTables() {
        return listDDIVariableOfTables;
    }

    public List<DDIVariable> getListVariables() {
        return listDDIVariables;
    }

    public List<DDIRepresentedVariable> getListRepresentedVariables() {
        return listDDIRepresentedVariables;
    }
}
