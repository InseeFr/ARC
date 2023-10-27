package fr.insee.arc.core.service.p4controle.bo;

import java.util.List;
import java.util.stream.Collectors;

public class IdCardControle {
	
	private List<RegleControle> reglesControle;

	public IdCardControle(List<RegleControle> reglesControle) {
		super();
		this.reglesControle = reglesControle;
	}

	public List<RegleControle> getReglesControle() {
		return reglesControle;
	}
	
	/**
	 * Renvoie la liste des règles de contrôle filtrée selon le type de contrôle donné
	 * @param typeControle
	 * @return
	 */
	public List<RegleControle> getReglesControle(ControleTypeCode typeControle) {
		return reglesControle.stream()
				.filter(r -> r.getTypeControle().equals(typeControle))
				.collect(Collectors.toList());
	}

	public void setReglesControle(List<RegleControle> reglesControle) {
		this.reglesControle = reglesControle;
	}

}
