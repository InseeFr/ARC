package fr.insee.arc.core.service.global;

import java.util.List;
import java.util.Map;

import fr.insee.arc.core.service.global.scalability.ScalableConnection;
import fr.insee.arc.core.service.p2chargement.bo.NormeRules;

public class ThreadTemplate {


	// anti-spam delay when thread chain error
	protected static final int PREVENT_ERROR_SPAM_DELAY = 100;
	
	// thread temporary pilotage table name
	protected static final String TABLE_PILOTAGE_THREAD = "p";
	
	protected String envExecution;
	protected ScalableConnection connexion;
	protected String tablePilTemp;
	protected String tablePil;
	protected String paramBatch;
	protected List<NormeRules> listeNorme;
	protected String directoryIn;
	protected Map<String, List<String>> tabIdSource;

	
	public String getEnvExecution() {
		return envExecution;
	}
	public ScalableConnection getConnexion() {
		return connexion;
	}
	public String getTablePilTemp() {
		return tablePilTemp;
	}
	public String getTablePil() {
		return tablePil;
	}
	public String getParamBatch() {
		return paramBatch;
	}
	public List<NormeRules> getListeNorme() {
		return listeNorme;
	}
	public String getDirectoryIn() {
		return directoryIn;
	}
	public Map<String, List<String>> getTabIdSource() {
		return tabIdSource;
	}
	
	
}
