package fr.insee.arc.core.model.ddi;

/**
 * Représente un concept de variable défini par DDI, soit le contenu d'une balise {@code RepresentedVariable}.
 * <p>
 * Cet objet détermine notamment le type de la variable, et peut être représenté par plusieurs variables à la fois (par exemple, les variables {@code pays_naissance} et {@code pays_residence} partagent le concept de variable {@code code_pays}).
 * 
 * @author Z84H10
 *
 */
public class DDIRepresentedVariable {

    /**
     * Nom du concept de variable.
     */
	private String representedVariableName;
	/**
	 * Identifiant unique de l'objet. Il permet d'établir les relations entre objets.
	 */
    private String id;
    /**
     * Type du concept de variable. Type {@code Text} par défaut et si non renseigné.
     */
    private String type = "Text";
    /**
     * Intitulé du concept de variable.
     */
    private String label;
    /**
     * Description et particularités du concept de variable.
     */
    private String description;

    public String getRepresentedVariableName() {
        return representedVariableName;
    }

    public void setRepresentedVariableName(String representedVariableName) {
        this.representedVariableName = representedVariableName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    @Override
    public String toString() {
        return "Represented variable " + this.getRepresentedVariableName()
                + " (" + this.getLabel()
                + ") ID " + this.getId()
                + "\n   " + this.getDescription()
                + "\n   de type " + this.getType();
    }

}