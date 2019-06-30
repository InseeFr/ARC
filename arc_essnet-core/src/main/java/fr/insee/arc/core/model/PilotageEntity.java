package fr.insee.arc.core.model;

import java.util.Arrays;

public class PilotageEntity {

    
    private String idSource ;
    private String idNorme  ;
    private String validite  ;
    private String periodicite ;
    private TypeTraitementPhase phaseTraitement;
    private String[] etatTraitemennt;
    private String dateTraitemennt;
    private String rapport ;
    private float tauxKo ;
    private String nbEnr ;
    private String nbEssais ;
    private int etape ;
    private String validiteInf;
    private String validiteSup ;
    private String version ;
    private String dateEntree ;
    private String container ;
    private String vContainer;
    private String oContainer;
    private String toDelete ;
    private String client ;
    private String dateClient ;
    private String jointure ;
    
   
    
    public String getIdSource() {
        return idSource;
    }
    public void setIdSource(String idSource) {
        this.idSource = idSource;
    }
    public String getIdNorme() {
        return idNorme;
    }
    public void setIdNorme(String idNorme) {
        this.idNorme = idNorme;
    }
    public String getValidite() {
        return validite;
    }
    public void setValidite(String validite) {
        this.validite = validite;
    }
    public String getPeriodicite() {
        return periodicite;
    }
    public void setPeriodicite(String periodicite) {
        this.periodicite = periodicite;
    }
    public TypeTraitementPhase getPhaseTraitement() {
        return phaseTraitement;
    }
    public void setPhaseTraitement(TypeTraitementPhase phaseTraitement) {
        this.phaseTraitement = phaseTraitement;
    }
    public String[] getEtatTraitemennt() {
        return etatTraitemennt;
    }
    public void setEtatTraitemennt(String[] etatTraitemennt) {
        this.etatTraitemennt = etatTraitemennt;
    }
    public String getDateTraitemennt() {
        return dateTraitemennt;
    }
    public void setDateTraitemennt(String dateTraitemennt) {
        this.dateTraitemennt = dateTraitemennt;
    }
    public String getRapport() {
        return rapport;
    }
    public void setRapport(String rapport) {
        this.rapport = rapport;
    }
    public float getTauxKo() {
        return tauxKo;
    }
    public void setTauxKo(float tauxKo) {
        this.tauxKo = tauxKo;
    }
    public String getNbEnr() {
        return nbEnr;
    }
    public void setNbEnr(String nbEnr) {
        this.nbEnr = nbEnr;
    }
    public String getNbEssais() {
        return nbEssais;
    }
    public void setNbEssais(String nbEssais) {
        this.nbEssais = nbEssais;
    }
    public int getEtape() {
        return etape;
    }
    public void setEtape(int etape) {
        this.etape = etape;
    }
    public String getValiditeInf() {
        return validiteInf;
    }
    public void setValiditeInf(String validiteInf) {
        this.validiteInf = validiteInf;
    }
    public String getValiditeSup() {
        return validiteSup;
    }
    public void setValiditeSup(String validiteSup) {
        this.validiteSup = validiteSup;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getDateEntree() {
        return dateEntree;
    }
    public void setDateEntree(String dateEntree) {
        this.dateEntree = dateEntree;
    }
    public String getContainer() {
        return container;
    }
    public void setContainer(String container) {
        this.container = container;
    }
    public String getvContainer() {
        return vContainer;
    }
    public void setvContainer(String vContainer) {
        this.vContainer = vContainer;
    }
    public String getoContainer() {
        return oContainer;
    }
    public void setoContainer(String oContainer) {
        this.oContainer = oContainer;
    }
    public String getToDelete() {
        return toDelete;
    }
    public void setToDelete(String toDelete) {
        this.toDelete = toDelete;
    }
    public String getClient() {
        return client;
    }
    public void setClient(String client) {
        this.client = client;
    }
    public String getDateClient() {
        return dateClient;
    }
    public void setDateClient(String dateClient) {
        this.dateClient = dateClient;
    }
    public String getJointure() {
        return jointure;
    }
    public void setJointure(String jointure) {
        this.jointure = jointure;
    }
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((client == null) ? 0 : client.hashCode());
	result = prime * result + ((container == null) ? 0 : container.hashCode());
	result = prime * result + ((dateClient == null) ? 0 : dateClient.hashCode());
	result = prime * result + ((dateEntree == null) ? 0 : dateEntree.hashCode());
	result = prime * result + ((dateTraitemennt == null) ? 0 : dateTraitemennt.hashCode());
	result = prime * result + etape;
	result = prime * result + Arrays.hashCode(etatTraitemennt);
	result = prime * result + ((idNorme == null) ? 0 : idNorme.hashCode());
	result = prime * result + ((idSource == null) ? 0 : idSource.hashCode());
	result = prime * result + ((jointure == null) ? 0 : jointure.hashCode());
	result = prime * result + ((nbEnr == null) ? 0 : nbEnr.hashCode());
	result = prime * result + ((nbEssais == null) ? 0 : nbEssais.hashCode());
	result = prime * result + ((oContainer == null) ? 0 : oContainer.hashCode());
	result = prime * result + ((periodicite == null) ? 0 : periodicite.hashCode());
	result = prime * result + ((phaseTraitement == null) ? 0 : phaseTraitement.hashCode());
	result = prime * result + ((rapport == null) ? 0 : rapport.hashCode());
	result = prime * result + Float.floatToIntBits(tauxKo);
	result = prime * result + ((toDelete == null) ? 0 : toDelete.hashCode());
	result = prime * result + ((vContainer == null) ? 0 : vContainer.hashCode());
	result = prime * result + ((validite == null) ? 0 : validite.hashCode());
	result = prime * result + ((validiteInf == null) ? 0 : validiteInf.hashCode());
	result = prime * result + ((validiteSup == null) ? 0 : validiteSup.hashCode());
	result = prime * result + ((version == null) ? 0 : version.hashCode());
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
	PilotageEntity other = (PilotageEntity) obj;
	if (client == null) {
	    if (other.client != null)
		return false;
	} else if (!client.equals(other.client))
	    return false;
	if (container == null) {
	    if (other.container != null)
		return false;
	} else if (!container.equals(other.container))
	    return false;
	if (dateClient == null) {
	    if (other.dateClient != null)
		return false;
	} else if (!dateClient.equals(other.dateClient))
	    return false;
	if (dateEntree == null) {
	    if (other.dateEntree != null)
		return false;
	} else if (!dateEntree.equals(other.dateEntree))
	    return false;
	if (dateTraitemennt == null) {
	    if (other.dateTraitemennt != null)
		return false;
	} else if (!dateTraitemennt.equals(other.dateTraitemennt))
	    return false;
	if (etape != other.etape)
	    return false;
	if (!Arrays.equals(etatTraitemennt, other.etatTraitemennt))
	    return false;
	if (idNorme == null) {
	    if (other.idNorme != null)
		return false;
	} else if (!idNorme.equals(other.idNorme))
	    return false;
	if (idSource == null) {
	    if (other.idSource != null)
		return false;
	} else if (!idSource.equals(other.idSource))
	    return false;
	if (jointure == null) {
	    if (other.jointure != null)
		return false;
	} else if (!jointure.equals(other.jointure))
	    return false;
	if (nbEnr == null) {
	    if (other.nbEnr != null)
		return false;
	} else if (!nbEnr.equals(other.nbEnr))
	    return false;
	if (nbEssais == null) {
	    if (other.nbEssais != null)
		return false;
	} else if (!nbEssais.equals(other.nbEssais))
	    return false;
	if (oContainer == null) {
	    if (other.oContainer != null)
		return false;
	} else if (!oContainer.equals(other.oContainer))
	    return false;
	if (periodicite == null) {
	    if (other.periodicite != null)
		return false;
	} else if (!periodicite.equals(other.periodicite))
	    return false;
	if (phaseTraitement != other.phaseTraitement)
	    return false;
	if (rapport == null) {
	    if (other.rapport != null)
		return false;
	} else if (!rapport.equals(other.rapport))
	    return false;
	if (Float.floatToIntBits(tauxKo) != Float.floatToIntBits(other.tauxKo))
	    return false;
	if (toDelete == null) {
	    if (other.toDelete != null)
		return false;
	} else if (!toDelete.equals(other.toDelete))
	    return false;
	if (vContainer == null) {
	    if (other.vContainer != null)
		return false;
	} else if (!vContainer.equals(other.vContainer))
	    return false;
	if (validite == null) {
	    if (other.validite != null)
		return false;
	} else if (!validite.equals(other.validite))
	    return false;
	if (validiteInf == null) {
	    if (other.validiteInf != null)
		return false;
	} else if (!validiteInf.equals(other.validiteInf))
	    return false;
	if (validiteSup == null) {
	    if (other.validiteSup != null)
		return false;
	} else if (!validiteSup.equals(other.validiteSup))
	    return false;
	if (version == null) {
	    if (other.version != null)
		return false;
	} else if (!version.equals(other.version))
	    return false;
	return true;
    }
    
    

}
