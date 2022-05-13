package fr.insee.arc.batch.threadRunners.template;

import java.util.HashMap;

import fr.insee.arc.batch.threadRunners.parameter.ParameterKey;
import fr.insee.arc.core.model.ServiceReporting;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;

public class ThreadRunnerTemplate extends Thread {

	private 
	
	
	// parameters map for the thread
	// keys in parameterKey class
	HashMap<String, String> mapParam;

	private TraitementPhase phaseName;

	private int previousNumberOfFilesLeft = -1;

	
	private ServiceReporting report = new ServiceReporting();

	public void initializeThreadRunnerTemplate(HashMap<String, String> mapParam, TraitementPhase phaseName) {
		setPhaseName(phaseName);
		setMapParam(mapParam);
	}

	
	/**
	 * return the status of the thread
	 * it queries the pilotage table to see if there is a blockage in the thread 
	 * by comparing the current files left with the previous recorded files left
	 * @return
	 */
	public boolean isBlocked() {

		boolean blocked;
		
		// count the number of file left to be processed in the phase
		PreparedStatementBuilder query = new PreparedStatementBuilder();
		query.append("SELECT count(*) FROM " + this.getMapParam().get(ParameterKey.KEY_FOR_EXECUTION_ENVIRONMENT)
				+ ".pilotage_fichier WHERE etape=1 and phase_traitement='" + this.getPhaseName()
				+ "' and etat_traitement='{" + TraitementEtat.ENCOURS + "}'");

		int currentNumberOfFilesLeft = UtilitaireDao.get("arc").getInt(null, query);

		// compare it to the previous recorded number of files left
		// if still some files to go and if the stack hadn't moved, the thread will be marked as blocked
		if (currentNumberOfFilesLeft>0 && currentNumberOfFilesLeft == previousNumberOfFilesLeft) {
			blocked = true;
		} else {
			this.previousNumberOfFilesLeft = currentNumberOfFilesLeft;
			blocked = false;
		}
		return blocked;
	}

	public HashMap<String, String> getMapParam() {
		return mapParam;
	}

	public void setMapParam(HashMap<String, String> mapParam) {
		this.mapParam = mapParam;
	}

	public TraitementPhase getPhaseName() {
		return phaseName;
	}


	public void setPhaseName(TraitementPhase phaseName) {
		this.phaseName = phaseName;
	}


	public ServiceReporting getReport() {
		return report;
	}

	public void setReport(ServiceReporting report) {
		this.report = report;
	}
	
	
}
