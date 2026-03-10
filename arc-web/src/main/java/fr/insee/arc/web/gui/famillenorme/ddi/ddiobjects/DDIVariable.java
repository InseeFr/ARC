package fr.insee.arc.web.gui.famillenorme.ddi.ddiobjects;

/**
 * Représente une variable concrète définie par DDI, soit le contenu d'une balise {@code Variable}.
 * 
 * @author Z84H10
 *
 */
public class DDIVariable {

	/**
	 * Nom de la variable.
	 */
    private String variableName;
    /**
     * Identifiant unique de l'objet. Il permet d'établir les relations entre objets.
     */
    private String idVariable;
    /**
     * Libellé de la variable.
     */
    private String label;
    /**
     * Identifiant du concept de variable associé à la variable.
     */
    private String idRepresentedVariable;

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getIdVariable() {
        return idVariable;
    }

    public void setIdVariable(String idVariable) {
        this.idVariable = idVariable;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIdRepresentedVariable() {
        return idRepresentedVariable;
    }

    public void setIdRepresentedVariable(String idRepresentedVariable) {
        this.idRepresentedVariable = idRepresentedVariable;
    }

    @Override
    public String toString() {
        return "Variable " + this.getVariableName()
                + " (" + this.getLabel()
                + ") ID " + this.getIdVariable()
                + "\n   représentée par " + this.getIdRepresentedVariable();
    }

}