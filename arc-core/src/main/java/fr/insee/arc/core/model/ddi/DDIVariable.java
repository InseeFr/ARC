package fr.insee.arc.core.model.ddi;

public class DDIVariable {

    private String variableName;
    private String idVariable;
    private String label;
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