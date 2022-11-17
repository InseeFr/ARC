package fr.insee.arc.core.model.ddi;

public class DDIDatabase {

    private String dbName;
    private String id;
    private String label;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "Database " + this.getDbName()
                + " (" + this.getLabel()
                + ") ID " + this.getId();
    }

}
