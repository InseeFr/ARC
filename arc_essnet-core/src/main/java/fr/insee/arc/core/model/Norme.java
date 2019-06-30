package fr.insee.arc.core.model;

import fr.insee.arc.core.util.RegleChargement;

/**
 * classe permettant de gérer les normes
 * 
 * @author S4LWO8
 *
 */
public class Norme {

    private String idNorme;
    private String periodicite;
    private String defNorme;
    private String defValidite;
    private String id;
    private String etat;
    private String idFamille;

    private RegleChargement regleChargement;

    /**
     * 
     * @param idNorme
     * @param periodicite
     * @param defNorme
     * @param defValidite
     * @Deprecated
     */
    @Deprecated
    public Norme(String idNorme, String periodicite, String defNorme, String defValidite) {
	super();
	this.idNorme = idNorme;
	this.periodicite = periodicite;
	this.defNorme = defNorme;
	this.defValidite = defValidite;
    }

    public Norme(String idNorme, String periodicite, String defNorme, String defValidite, String id, String etat,
	    String idFamille, RegleChargement regleChargement) {
	super();
	this.idNorme = idNorme;
	this.periodicite = periodicite;
	this.defNorme = defNorme;
	this.defValidite = defValidite;
	this.id = id;
	this.etat = etat;
	this.idFamille = idFamille;
	this.regleChargement = regleChargement;
    }

    public Norme() {
	// Dummy contructor
    }

    public Norme(String idNorme) {
	this.idNorme = idNorme;
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

    public String getDefNorme() {
	return defNorme;
    }

    public void setDefNorme(String defNorme) {
	this.defNorme = defNorme;
    }

    public String getDefValidite() {
	return defValidite;
    }

    public void setDefValidite(String defValidite) {
	this.defValidite = defValidite;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getEtat() {
	return etat;
    }

    public void setEtat(String etat) {
	this.etat = etat;
    }

    public String getIdFamille() {
	return idFamille;
    }

    public void setIdFamille(String idFamille) {
	this.idFamille = idFamille;
    }

    public RegleChargement getRegleChargement() {
	return regleChargement;
    }

    public void setRegleChargement(RegleChargement regleChargement) {
	this.regleChargement = regleChargement;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((defNorme == null) ? 0 : defNorme.hashCode());
	result = prime * result + ((defValidite == null) ? 0 : defValidite.hashCode());
	result = prime * result + ((etat == null) ? 0 : etat.hashCode());
	result = prime * result + ((id == null) ? 0 : id.hashCode());
	result = prime * result + ((idFamille == null) ? 0 : idFamille.hashCode());
	result = prime * result + ((idNorme == null) ? 0 : idNorme.hashCode());
	result = prime * result + ((periodicite == null) ? 0 : periodicite.hashCode());
	result = prime * result + ((regleChargement == null) ? 0 : regleChargement.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Norme other = (Norme) obj;
	if (defNorme == null) {
	    if (other.defNorme != null)
		return false;
	} else if (!defNorme.equals(other.defNorme))
	    return false;
	if (defValidite == null) {
	    if (other.defValidite != null)
		return false;
	} else if (!defValidite.equals(other.defValidite))
	    return false;
	if (etat == null) {
	    if (other.etat != null)
		return false;
	} else if (!etat.equals(other.etat))
	    return false;
	if (id == null) {
	    if (other.id != null)
		return false;
	} else if (!id.equals(other.id))
	    return false;
	if (idFamille == null) {
	    if (other.idFamille != null)
		return false;
	} else if (!idFamille.equals(other.idFamille))
	    return false;
	if (idNorme == null) {
	    if (other.idNorme != null)
		return false;
	} else if (!idNorme.equals(other.idNorme))
	    return false;
	if (periodicite == null) {
	    if (other.periodicite != null)
		return false;
	} else if (!periodicite.equals(other.periodicite))
	    return false;
	if (regleChargement == null) {
	    if (other.regleChargement != null)
		return false;
	} else if (!regleChargement.equals(other.regleChargement))
	    return false;
	return true;
    }

}
