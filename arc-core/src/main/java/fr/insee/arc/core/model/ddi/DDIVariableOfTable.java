package fr.insee.arc.core.model.ddi;

public class DDIVariableOfTable {

    private String idVariable;

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