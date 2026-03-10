package fr.insee.arc.web.gui.all.model;

public enum Sandbox {
    ARC_PROD("arc.prod")//
    ;

    private String value;

    private Sandbox(String value) {
	this.value = value;
    }

    public String getValue() {
	return value;
    }

}
