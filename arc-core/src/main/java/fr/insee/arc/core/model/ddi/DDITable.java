package fr.insee.arc.core.model.ddi;

public class DDITable {

    private String tableName;
    private String idTable;
    private String label;
    private String description;
    private String idDatabase;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIdTable() {
        return idTable;
    }

    public void setIdTable(String idTable) {
        this.idTable = idTable;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIdDatabase() {
        return idDatabase;
    }

    public void setIdDatabase(String idDatabase) {
        this.idDatabase = idDatabase;
    }

    @Override
    public String toString() {
        return "Table " + this.getTableName()
                + " (" + this.getLabel()
                + ") ID " + this.getIdTable()
                + "\n   " + this.getDescription()
                + "\n   dans la database " + this.getIdDatabase();
    }

}