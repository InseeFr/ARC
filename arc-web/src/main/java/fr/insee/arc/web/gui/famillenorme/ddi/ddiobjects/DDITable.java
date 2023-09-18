package fr.insee.arc.web.gui.famillenorme.ddi.ddiobjects;

/**
 * Représente une table de données défini par DDI, soit le contenu d'une balise {@code LogicalRecord}.
 * 
 * @author Z84H10
 *
 */
public class DDITable {

	/**
	 * Nom de la table.
	 */
    private String tableName;
    /**
     * Identifiant unique de l'objet. Il permet d'établir les relations entre objets.
     */
    private String idTable;
    /**
     * Libellé de la table.
     */
    private String label;
    /**
     * Description et particularités de la table.
     */
    private String description;
    /**
     * Identifiant du modèle de données auquel appartient la table.
     */
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