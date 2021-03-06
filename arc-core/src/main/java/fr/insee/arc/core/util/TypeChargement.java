package fr.insee.arc.core.util;

public enum TypeChargement {
    CLEF_VALEUR("clef-valeur"),
    XML("xml"),
    PLAT("plat"),
    XML_COMPLEXE("xml-complexe");
    
    private String nom;

    private TypeChargement(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public static TypeChargement getEnum(String code) {

        switch (code) {
            case "clef-valeur":
                return CLEF_VALEUR;
            case "xml":
                return XML;
            case "plat":
                return PLAT;
            case "xml-complexe":
                return XML_COMPLEXE;
            default:
                return null;
         }
       }
    
}
