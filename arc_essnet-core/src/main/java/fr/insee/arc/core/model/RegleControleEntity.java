package fr.insee.arc.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.insee.arc.utils.dao.AbstractEntity;
import fr.insee.arc.utils.format.Format;

public class RegleControleEntity extends AbstractEntity {

    private static final String COL_ID_NORME = "id_norme";
    private static final String COL_PERIODICITY = "periodicite";
    private static final String COL_VALIDITY_INF = "validite_inf";
    private static final String COL_VALIDITY_SUP = "validite_sup";
    private static final String COL_VERSION = "version";
    private static final String COL_ID_CLASSE = "id_classe";
    private static final String COL_PARENT_ELEMENT = "rubrique_pere";
    private static final String COL_CHILD_ELEMENT = "rubrique_fils";
    private static final String COL_BORNE_INF = "borne_inf";
    private static final String COL_BORNE_SUP = "borne_sup";
    private static final String COL_CONDITION = "condition";
    private static final String COL_PREACTION = "pre_action";
    private static final String COL_ID_RULE = "id_regle";
    private static final String COL_TODO = "todo";
    private static final String COL_COMMENTAIRE = "commentaire";

    private static final Set<String> colNames = new HashSet<String>() {
	/**
	 *
	 */
	private static final long serialVersionUID = 3677223986776708059L;

	{
	    add(COL_ID_NORME);
	    add(COL_PERIODICITY);
	    add(COL_VALIDITY_INF);
	    add(COL_VALIDITY_SUP);
	    add(COL_VERSION);
	    add(COL_ID_CLASSE);
	    add(COL_PARENT_ELEMENT);
	    add(COL_CHILD_ELEMENT);
	    add(COL_BORNE_INF);
	    add(COL_BORNE_SUP);
	    add(COL_CONDITION);
	    add(COL_PREACTION);
	    add(COL_ID_RULE);
	    add(COL_TODO);
	    add(COL_COMMENTAIRE);
	}
    };

    public RegleControleEntity() {
	super();
    }

    public RegleControleEntity(List<String> someNames, List<String> someValues) {
	super(someNames, someValues);
    }

    /**
     * Constructeur spécific à la viewControle
     *
     * @param inputFields
     */
    public RegleControleEntity(Map<String, ArrayList<String>> mapInputFields) {
	super(mapInputFields);
	this.setIdRegle(mapInputFields.get(COL_ID_RULE).get(0));
	this.setIdClasse(mapInputFields.get(COL_ID_CLASSE).get(0));
	this.setRubriquePere(mapInputFields.get(COL_PARENT_ELEMENT).get(0));
	this.setRubriqueFils(mapInputFields.get(COL_CHILD_ELEMENT).get(0));
	this.setBorneInf(mapInputFields.get(COL_BORNE_INF).get(0));
	this.setBorneSup(mapInputFields.get(COL_BORNE_SUP).get(0));
	this.setCondition(mapInputFields.get(COL_CONDITION).get(0));
	this.setPreAction(mapInputFields.get(COL_PREACTION).get(0));
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((this.getBorneInf() == null) ? 0 : this.getBorneInf().hashCode());
	result = prime * result + ((this.getBorneSup() == null) ? 0 : this.getBorneSup().hashCode());
	result = prime * result + ((this.getCommentaire() == null) ? 0 : this.getCommentaire().hashCode());
	result = prime * result + ((this.getCondition() == null) ? 0 : this.getCondition().hashCode());
	result = prime * result + ((this.getIdClasse() == null) ? 0 : this.getIdClasse().hashCode());
	result = prime * result + ((this.getIdRegle() == null) ? 0 : this.getIdRegle().hashCode());
	result = prime * result + ((this.getPreAction() == null) ? 0 : this.getPreAction().hashCode());
	result = prime * result + ((this.getRubriqueFils() == null) ? 0 : this.getRubriqueFils().hashCode());
	result = prime * result + ((this.getRubriquePere() == null) ? 0 : this.getRubriquePere().hashCode());
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
	if (!(obj instanceof RegleControleEntity)) {
	    return false;
	}
	RegleControleEntity other = (RegleControleEntity) obj;
	if (this.getBorneInf() == null) {
	    if (other.getBorneInf() != null) {
		return false;
	    }
	} else if (!this.getBorneInf().equals(other.getBorneInf())) {
	    return false;
	}
	if (this.getBorneSup() == null) {
	    if (other.getBorneSup() != null) {
		return false;
	    }
	} else if (!this.getBorneSup().equals(other.getBorneSup())) {
	    return false;
	}
	if (this.getCommentaire() == null) {
	    if (other.getCommentaire() != null) {
		return false;
	    }
	} else if (!this.getCommentaire().equals(other.getCommentaire())) {
	    return false;
	}
	if (this.getCondition() == null) {
	    if (other.getCondition() != null) {
		return false;
	    }
	} else if (!this.getCondition().equals(other.getCondition())) {
	    return false;
	}
	if (this.getIdClasse() == null) {
	    if (other.getIdClasse() != null) {
		return false;
	    }
	} else if (!this.getIdClasse().equals(other.getIdClasse())) {
	    return false;
	}
	if (this.getIdRegle() == null) {
	    if (other.getIdRegle() != null) {
		return false;
	    }
	} else if (!this.getIdRegle().equals(other.getIdRegle())) {
	    return false;
	}
	if (this.getPreAction() == null) {
	    if (other.getPreAction() != null) {
		return false;
	    }
	} else if (!this.getPreAction().equals(other.getPreAction())) {
	    return false;
	}
	if (this.getRubriqueFils() == null) {
	    if (other.getRubriqueFils() != null) {
		return false;
	    }
	} else if (!this.getRubriqueFils().equals(other.getRubriqueFils())) {
	    return false;
	}
	if (this.getRubriquePere() == null) {
	    if (other.getRubriquePere() != null) {
		return false;
	    }
	} else if (!this.getRubriquePere().equals(other.getRubriquePere())) {
	    return false;
	}
	return true;
    }

    /**
     *
     * @return
     */
    public String getIdNorme() {
	return this.getMap().get(COL_ID_NORME);
    }

    /**
     * @param idNorme
     *            the idNorme to set
     */
    public void setIdNorme(String idNorme) {
	this.getMap().put(COL_ID_NORME, idNorme);
    }

    /**
     *
     * @return
     */
    public String getPeriodicite() {
	return this.getMap().get(COL_PERIODICITY);
    }

    /**
     * @param periodicite
     *            the periodicite to set
     */
    public void setPeriodicite(String periodicite) {
	this.getMap().put(COL_PERIODICITY, periodicite);
    }

    /**
     *
     * @return
     */
    public String getValiditeInf() {
	return this.getMap().get(COL_VALIDITY_INF);
    }

    /**
     * @param validiteInf
     *            the validiteInf to set
     */
    public void setValiditeInf(String validiteInf) {
	this.getMap().put(COL_VALIDITY_INF, validiteInf);
    }

    /**
     *
     * @return
     */
    public String getValiditeSup() {
	return this.getMap().get(COL_VALIDITY_SUP);
    }

    /**
     * @param validiteSup
     *            the validiteSup to set
     */
    public void setValiditeSup(String validiteSup) {
	this.getMap().put(COL_VALIDITY_SUP, validiteSup);
    }

    /**
     *
     * @return
     */
    public String getVersion() {
	return this.getMap().get(COL_VERSION);
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(String version) {
	this.getMap().put(COL_VERSION, version);
    }

    /**
     *
     * @return
     */
    public String getIdClasse() {
	return this.getMap().get(COL_ID_CLASSE);
    }

    /**
     * @param idClasse
     *            the idClasse to set
     */
    public void setIdClasse(String idClasse) {
	this.getMap().put(COL_ID_CLASSE, idClasse);
    }

    /**
     *
     * @return
     */
    public String getRubriquePere() {
	return Format.toUpperCase(this.getMap().get(COL_PARENT_ELEMENT));
    }

    /**
     * @param rubriquePere
     *            the rubriquePere to set
     */
    public void setRubriquePere(String rubriquePere) {
	this.getMap().put(COL_PARENT_ELEMENT, rubriquePere);
    }

    /**
     *
     * @return
     */
    public String getRubriqueFils() {
	return Format.toUpperCase(this.getMap().get(COL_CHILD_ELEMENT));
    }

    /**
     * @param rubriqueFils
     *            the rubriqueFils to set
     */
    public void setRubriqueFils(String rubriqueFils) {
	this.getMap().put(COL_CHILD_ELEMENT, rubriqueFils);
    }

    /**
     *
     * @return
     */
    public String getBorneInf() {
	return this.getMap().get(COL_BORNE_INF);
    }

    /**
     * @param borneInf
     *            the borneInf to set
     */
    public void setBorneInf(String borneInf) {
	this.getMap().put(COL_BORNE_INF, borneInf);
    }

    /**
     *
     * @return
     */
    public String getBorneSup() {
	return this.getMap().get(COL_BORNE_SUP);
    }

    /**
     * @param borneSup
     *            the borneSup to set
     */
    public void setBorneSup(String borneSup) {
	this.getMap().put(COL_BORNE_SUP, borneSup);
    }

    /**
     *
     * @return
     */
    public String getCondition() {
	return this.getMap().get(COL_CONDITION);
    }

    /**
     * @param condition
     *            the condition to set
     */
    public void setCondition(String condition) {
	this.getMap().put(COL_CONDITION, condition);
    }

    /**
     *
     * @return
     */
    public String getPreAction() {
	return this.getMap().get(COL_PREACTION);
    }

    /**
     * @param preAction
     *            the preAction to set
     */
    public void setPreAction(String preAction) {
	this.getMap().put(COL_PREACTION, preAction);
    }

    /**
     *
     * @return
     */
    public String getIdRegle() {
	return this.getMap().get(COL_ID_RULE);
    }

    /**
     * @param idRegle
     *            the idRegle to set
     */
    public void setIdRegle(String idRegle) {
	this.getMap().put(COL_ID_RULE, idRegle);
    }

    /**
     *
     * @return
     */
    public String getTodo() {
	return this.getMap().get(COL_TODO);
    }

    /**
     * 
     * @param todo
     */
    public void setTodo(String todo) {
	this.getMap().put(COL_TODO, todo);
    }

    /**
     *
     * @return
     */
    public String getCommentaire() {
	return this.getMap().get(COL_COMMENTAIRE);
    }

    /**
     * @param commentaire
     *            the commentaire to set
     */
    public void setCommentaire(String commentaire) {
	this.getMap().put(COL_COMMENTAIRE, commentaire);
    }

    @Override
    public Set<String> colNames() {
	return colNames;
    }
}
