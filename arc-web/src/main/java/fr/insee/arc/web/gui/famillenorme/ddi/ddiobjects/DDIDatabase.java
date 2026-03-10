package fr.insee.arc.web.gui.famillenorme.ddi.ddiobjects;

/**
 * Représente un modèle de données défini par DDI, soit le contenu d'une balise {@code DataRelationship}.
 * 
 * @author Z84H10
 *
 */
public class DDIDatabase {

	/**
	 * Nom du modèle de données.
	 */
    private String dbName;
    /**
     * Identifiant unique de l'objet. Il permet d'établir les relations entre objets.
     */
    private String id;
    /**
     * Intitulé du modèle de données.
     */
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
