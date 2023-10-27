package fr.insee.arc.core.service.p3normage.bo;

public enum TypeNormage {
	RELATION("relation"),
	CARTESIAN("cartesian"),
	UNICITE("unicité"),
	PARTITION("partition"),
	INDEPENDANCE("independance"),
	BLOC_INDEPENDANCE("bloc_independance"),
	DELETION("deletion"),
	DUPLICATION("duplication")
	;
	
	private String nom;

    private TypeNormage(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

}
