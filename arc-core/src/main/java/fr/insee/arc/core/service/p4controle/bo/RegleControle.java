package fr.insee.arc.core.service.p4controle.bo;

import fr.insee.arc.utils.format.Format;

public class RegleControle {
	
	private ControleTypeCode typeControle; // aka id_classe
	private String rubriquePere;
	private String rubriqueFils;
	
	private String borneInf;
	private String borneSup;
	private String condition;
	
	private String preAction; // aka prétraitement SQL
	private int idRegle;
	
	private String seuilBloquant; // aka blocking_threshold
	private String traitementLignesErreur; // aka error_row_processing
	
	public RegleControle(ControleTypeCode typeControle, String rubriquePere, String rubriqueFils, String borneInf,
			String borneSup, String condition, String preAction, int idRegle, String seuilBloquant, String traitementLignesErreur) {
		super();
		this.typeControle = typeControle;
		this.rubriquePere = rubriquePere;
		this.rubriqueFils = rubriqueFils;
		this.borneInf = borneInf;
		this.borneSup = borneSup;
		this.condition = condition;
		this.preAction = preAction;
		this.idRegle = idRegle;
		this.seuilBloquant = seuilBloquant;
		this.traitementLignesErreur = traitementLignesErreur;
	}

	public RegleControle() {
		super();
	}

	public ControleTypeCode getTypeControle() {
		return typeControle;
	}

	public void setTypeControle(ControleTypeCode typeControle) {
		this.typeControle = typeControle;
	}

	public String getRubriquePere() {
		return Format.toUpperCase(rubriquePere);
	}

	public void setRubriquePere(String rubriquePere) {
		this.rubriquePere = rubriquePere;
	}

	public String getRubriqueFils() {
		return Format.toUpperCase(rubriqueFils);
	}

	public String getBorneInf() {
		return borneInf;
	}

	public String getBorneSup() {
		return borneSup;
	}


	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getPreAction() {
		return preAction;
	}

	public int getIdRegle() {
		return idRegle;
	}

	public void setIdRegle(int idRegle) {
		this.idRegle = idRegle;
	}

	public String getSeuilBloquant() {
		return seuilBloquant;
	}

	public String getTraitementLignesErreur() {
		return traitementLignesErreur;
	}

}
