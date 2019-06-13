package fr.insee.arc_essnet.core.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import fr.insee.arc_essnet.core.model.Norme;
import fr.insee.arc_essnet.core.service.thread.ThreadLoadService;

/**
 * ApiLoadService
 *
 *<ol>
 *<li> Get all the file to load</li>
 *<li> For each create a {@link ThreadLoadService} and run it</li>
 *</ol>
 * All the business code is in {@link ThreadLoadService}
 * Create a {@link ThreadLoadService} for each file to 
 * 
 * 
 * 1- Créer les tables de reception du chargement</br>
 * 2- Récupérer la liste des fichiers à traiter et le nom de leur entrepôt 3-
 * Pour chaque fichier, determiner son format de ***REMOVED*** (zip, tgz, raw) et le
 * chargeur à utlisé (voir entrepot) 4- Pour chaque fichier, Invoquer le
 * chargeur 4-1 Parsing du fichier 4-2 Insertion dans les tables I et A des
 * données lues dans le fichier 4-3 Fin du parsing. Constituer la requete de
 * mise en relation des données chargées et la stocker pour son utilisation
 * ultérieure au normage 5- Fin chargement. Insertion dans la table applicative
 * CHARGEMENT_OK. Mise à jour de la table de pilotage
 *
 * @author Rémi Pépin
 *
 */

@Component
public class ApiLoadService extends AbstractThreadRunnerService<ThreadLoadService>
	implements IApiServiceWithoutOutputTable {
    private static final Class<ThreadLoadService> THREAD_TYPE = ThreadLoadService.class;

    
    protected String loadOkTable;

    private Map<String, Integer> col = new HashMap<>();
    private List<String> allCols;
    private Map<String, Integer> colData;
    private StringBuilder insertRequest;
    protected int nbFileLoaded = 0;

    protected String fileName;

    protected List<Norme> normList;

    private Map<String, ArrayList<String>> idSourceList;

    public ApiLoadService() {
	super();
    }

    public ApiLoadService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution,
	    String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
	super(THREAD_TYPE, aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);

	// fr.insee.arc.threads.chargement
	this.nbThread = 3;

	// Get all normes in database
	this.normList = getAllNorms();

    }

    public ApiLoadService(Connection connexion, String aCurrentPhase, String anParametersEnvironment,
	    String aEnvExecution, String aDirectoryRoot, Integer aNbEnr, String[] paramBatch) {
	super(connexion, THREAD_TYPE, aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr,
		paramBatch);

	// fr.insee.arc.threads.chargement
	this.nbThread = 3;

	// Get all normes in database
	this.normList = getAllNorms();
    }

    public Map<String, ArrayList<String>> getListIdsource() {
	return idSourceList;
    }

    public void setListIdsource(Map<String, ArrayList<String>> listIdsource) {
	this.idSourceList = listIdsource;
    }

    public Map<String, Integer> getCol() {
	return col;
    }

    public void setCol(Map<String, Integer> col) {
	this.col = col;
    }

    public Map<String, Integer> getColData() {
	return colData;
    }

    public void setColData(Map<String, Integer> colData) {
	this.colData = colData;
    }

    public List<String> getAllCols() {
	return allCols;
    }

    public void setAllCols(List<String> allCols) {
	this.allCols = allCols;
    }

    public StringBuilder getRequeteInsert() {
	return insertRequest;
    }

    public void setRequeteInsert(StringBuilder requeteInsert) {
	this.insertRequest = requeteInsert;
    }


}
