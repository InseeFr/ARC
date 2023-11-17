package fr.insee.arc.core.service.p3normage.bo;

public class RegleNormage {

	private TypeNormage typeNormage; // aka id_classe, type de structuration
	private String rubrique;
	private String rubriqueNmcl; // aka rubrique_nmcl
	
	public RegleNormage(TypeNormage typeNormage, String rubrique, String rubriqueNmcl) {
		super();
		this.typeNormage = typeNormage;
		this.rubrique = rubrique;
		this.rubriqueNmcl = rubriqueNmcl;
	}

	public TypeNormage getTypeNormage() {
		return typeNormage;
	}

	public void setTypeNormage(TypeNormage typeNormage) {
		this.typeNormage = typeNormage;
	}

	public String getRubrique() {
		return rubrique;
	}

	public void setRubrique(String rubrique) {
		this.rubrique = rubrique;
	}

	public String getRubriqueNmcl() {
		return rubriqueNmcl;
	}

	public void setRubriqueNmcl(String rubriqueNmcl) {
		this.rubriqueNmcl = rubriqueNmcl;
	}

}
