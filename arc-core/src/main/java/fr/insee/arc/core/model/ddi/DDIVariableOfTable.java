package fr.insee.arc.core.model.ddi;

/**
 * Fait le lien entre une variable et une table. Une table contient une ou plusieurs variables. Une variable peut appartenir à une ou plusieurs tables, ce qui permet les jointures.
 * 
 * @author Z84H10
 *
 */
public class DDIVariableOfTable {

	/**
	 * Identifiant de la variable à lier.
	 */
    private String idVariable;
    /**
	 * Identifiant de la table à lier.
	 */
    private String idTable;


    public String getIdVariable() {
        return idVariable;
    }

    public void setIdVariable(String idVariable) {
        this.idVariable = idVariable;
    }

    public String getIdTable() {
        return idTable;
    }

    public void setIdTable(String idTable) {
        this.idTable = idTable;
    }

    @Override
    public String toString() {
        return "Lien entre table " + this.getIdTable()
                + " et variable " + this.getIdVariable();
    }
}