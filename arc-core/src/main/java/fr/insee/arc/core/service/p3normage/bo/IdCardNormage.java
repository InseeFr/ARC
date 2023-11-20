package fr.insee.arc.core.service.p3normage.bo;

import java.util.List;
import java.util.stream.Collectors;

public class IdCardNormage {
	
	private List<RegleNormage> reglesNormage;

	public IdCardNormage(List<RegleNormage> reglesNormage) {
		super();
		this.reglesNormage = reglesNormage;
	}

	public List<RegleNormage> getReglesNormage() {
		return reglesNormage;
	}
	
	/**
	 * Renvoie la liste des règles de normage filtrée selon le type de structuration donné
	 * @param typeNormage
	 * @return
	 */
	public List<RegleNormage> getReglesNormage(TypeNormage typeNormage) {
		return reglesNormage.stream()
				.filter(r -> r.getTypeNormage().equals(typeNormage))
				.collect(Collectors.toList());
	}

	public void setReglesNormage(List<RegleNormage> reglesNormage) {
		this.reglesNormage = reglesNormage;
	}
	
	public void addRegleNormage(RegleNormage regleNormage) {
		this.reglesNormage.add(regleNormage);
	}
	
	/**
	 * test if provided rubriqueToTest matches any of rubrique in idCardNormage
	 * @param rubriqueToTest
	 * @return
	 */
	public boolean isAnyRubrique(String rubriqueToTest)
	{
		return this.reglesNormage.stream().anyMatch(t -> t.getRubrique()!=null && t.getRubrique().contains(rubriqueToTest));
	}
	
	/**
	 * test if provided rubriqueToTest matches any of rubriqueNmcl in idCardNormage
	 * @param rubriqueToTest
	 * @return
	 */
	public boolean isAnyRubriqueNmcl(String rubriqueToTest)
	{
		return this.reglesNormage.stream().anyMatch(t -> t.getRubriqueNmcl()!=null && t.getRubriqueNmcl().contains(rubriqueToTest));
	}
	
}
