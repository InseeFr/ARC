package fr.insee.arc.core.service.p0initialisation.pilotage.bo;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

public class ListIdSourceInPilotage {

	
	private Connection coordinatorConnexion;
	private String envExecution;


	private Map<String, String> idSourceInPilotage;

	public ListIdSourceInPilotage(Connection coordinatorConnexion, String envExecution) {
		super();
		this.idSourceInPilotage = new HashMap<>();
		this.coordinatorConnexion = coordinatorConnexion;
		this.envExecution = envExecution;
	}


	public ListIdSourceInPilotage addSource(TraitementPhase phase, TraitementEtat etat) throws ArcException
	{

		// create table of id_source
		UtilitaireDao.get(0).executeRequest(coordinatorConnexion, PilotageOperations.queryCreateIdSourceFromPilotage(envExecution, phase, etat));
		
		// register table name
		String key= serializeAsKey(phase, etat);
		this.idSourceInPilotage.put(key, PilotageOperations.tableOfIdSourceForPhaseAndEtat(envExecution, phase, etat));
		return this;
	}

	public String getIdSourceInPilotage(TraitementPhase phase, TraitementEtat etat) throws ArcException
	{
		String key= serializeAsKey(phase, etat);
		return idSourceInPilotage.get(key);
	}

	private static String serializeAsKey(TraitementPhase phase, TraitementEtat etat)
	{
		return phase+","+etat;
	}


	public Connection getCoordinatorConnexion() {
		return coordinatorConnexion;
	}


	public void dropSources() {
		idSourceInPilotage.values().forEach( t-> UtilitaireDao.get(0).dropTable(coordinatorConnexion, t));
	}

}

