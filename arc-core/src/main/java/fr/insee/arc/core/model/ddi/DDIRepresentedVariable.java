package fr.insee.arc.core.model.ddi;

public class DDIRepresentedVariable {

    private String representedVariableName;
    private String id;
    private String type = "[type]";
    private String label;
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