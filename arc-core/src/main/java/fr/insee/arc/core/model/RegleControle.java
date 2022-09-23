package fr.insee.arc.core.model;

import java.util.ArrayList;
import java.util.HashMap;

public class RegleControle {

	
	private String idNorme;
	private String periodicite;
	private String idRegle;
	private String idClasse;
	private String rubriquePere;
	private String rubriqueFils;
	private String borneInf;
	private String borneSup;
	private String condition;
	private String preAction;
	private String commentaire;
	
	private String table;
	
	public RegleControle(){
	}
	/**
	 * Constructeur spécific à la viewControle
	 * @param inputFields
	 */
	public RegleControle(HashMap<String, ArrayList<String>> mapInputFields) {
		idRegle=mapInputFields.get("id_regle").get(0);
		idClasse=mapInputFields.get("id_classe").get(0);
		rubriquePere=mapInputFields.get("rubrique_pere").get(0);
		rubriqueFils=mapInputFields.get("rubrique_fils").get(0);
		borneInf=mapInputFields.get("borne_inf").get(0);
		borneSup=mapInputFields.get("borne_sup").get(0);
		condition=mapInputFields.get("condition").get(0);
		preAction=mapInputFields.get("pre_action").get(0);
	}
	
	
	// Getters et Setters

	
	

	public String getIdRegle() {
		return idRegle;
	}
	public void setIdRegle(String idRegle) {
		this.idRegle = idRegle;
	}
	public String getIdClasse() {
		return idClasse;
	}
	public void setIdClasse(String idClasse) {
		this.idClasse = idClasse;
	}
	public String getRubriquePere() {
		if(rubriquePere ==  null){
			return rubriquePere;
		}else{
			return rubriquePere.toUpperCase();
		}
	}
	public void setRubriquePere(String rubriquePere) {
		this.rubriquePere = rubriquePere;
	}
	public String getRubriqueFils() {
		if(rubriqueFils ==  null){
			return rubriqueFils;
		}else{
			return rubriqueFils.toUpperCase();
		}
	}
	public void setRubriqueFils(String rubriqueFils) {
		this.rubriqueFils = rubriqueFils;
	}
	public String getBorneInf() {
		return borneInf;
	}
	public void setBorneInf(String borneInf) {
		this.borneInf = borneInf;
	}
	public String getBorneSup() {
		return borneSup;
	}
	public void setBorneSup(String borneSup) {
		this.borneSup = borneSup;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public String getIdNorme() {
		return idNorme;
	}
	public void setIdNorme(String idNorme) {
		this.idNorme = idNorme;
	}
	public String getPeriodicite() {
		return periodicite;
	}
	public void setPeriodicite(String periodicite) {
		this.periodicite = periodicite;
	}
	public String getPreAction() {
		return preAction;
	}
	public void setPreAction(String preAction) {
		this.preAction = preAction;
	}
	public String getCommentaire() {
		return commentaire;
	}
	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((borneInf == null) ? 0 : borneInf.hashCode());
		result = prime * result
				+ ((borneSup == null) ? 0 : borneSup.hashCode());
		result = prime * result
				+ ((commentaire == null) ? 0 : commentaire.hashCode());
		result = prime * result
				+ ((condition == null) ? 0 : condition.hashCode());
		result = prime * result
				+ ((idClasse == null) ? 0 : idClasse.hashCode());
		result = prime * result + ((idRegle == null) ? 0 : idRegle.hashCode());
		result = prime * result
				+ ((preAction == null) ? 0 : preAction.hashCode());
		result = prime * result
				+ ((rubriqueFils == null) ? 0 : rubriqueFils.hashCode());
		result = prime * result
				+ ((rubriquePere == null) ? 0 : rubriquePere.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RegleControle)) {
			return false;
		}
		RegleControle other = (RegleControle) obj;
		if (borneInf == null) {
			if (other.borneInf != null) {
				return false;
			}
		} else if (!borneInf.equals(other.borneInf)) {
			return false;
		}
		if (borneSup == null) {
			if (other.borneSup != null) {
				return false;
			}
		} else if (!borneSup.equals(other.borneSup)) {
			return false;
		}
		if (commentaire == null) {
			if (other.commentaire != null) {
				return false;
			}
		} else if (!commentaire.equals(other.commentaire)) {
			return false;
		}
		if (condition == null) {
			if (other.condition != null) {
				return false;
			}
		} else if (!condition.equals(other.condition)) {
			return false;
		}
		if (idClasse == null) {
			if (other.idClasse != null) {
				return false;
			}
		} else if (!idClasse.equals(other.idClasse)) {
			return false;
		}
		if (idRegle == null) {
			if (other.idRegle != null) {
				return false;
			}
		} else if (!idRegle.equals(other.idRegle)) {
			return false;
		}
		if (preAction == null) {
			if (other.preAction != null) {
				return false;
			}
		} else if (!preAction.equals(other.preAction)) {
			return false;
		}
		if (rubriqueFils == null) {
			if (other.rubriqueFils != null) {
				return false;
			}
		} else if (!rubriqueFils.equals(other.rubriqueFils)) {
			return false;
		}
		if (rubriquePere == null) {
			if (other.rubriquePere != null) {
				return false;
			}
		} else if (!rubriquePere.equals(other.rubriquePere)) {
			return false;
		}
		return true;
	}
	
	
	
	
	
}
