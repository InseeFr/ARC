package fr.insee.arc.core.service.p0initialisation.pilotage.bo;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;

public class ListIdSourceInPilotage {


	private Map<String, GenericBean> idSourceInPilotage;

	public ListIdSourceInPilotage() {
		super();
		this.idSourceInPilotage = new HashMap<>();
	}


	public ListIdSourceInPilotage addSource(Connection coordinatorConnexion, String envExecution, TraitementPhase phase, TraitementEtat etat) throws ArcException
	{
		String key= serializeAsKey(phase, etat);
		this.idSourceInPilotage.put(key, new GenericBean(UtilitaireDao.get(0).executeRequest(coordinatorConnexion, PilotageOperations.querySelectIdSourceFromPilotage(envExecution, phase, etat))));		
		return this;
	}

	public GenericBean getIdSourceInPilotage(TraitementPhase phase, TraitementEtat etat) throws ArcException
	{
		String key= serializeAsKey(phase, etat);
		return idSourceInPilotage.get(key);
	}

	private static String serializeAsKey(TraitementPhase phase, TraitementEtat etat)
	{
		return phase+","+etat;
	}
}

