package fr.insee.arc.ws.services.rest.sirene4.view;

public enum EnumCategorie {
    BLOQUANT("bloquant"), NON_BLOQUANT("non bloquant");

    private String nom;

    public String getNom() {
        return nom;
    }

    EnumCategorie(String nom) {
        this.nom = nom;
    }

}
