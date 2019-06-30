package fr.insee.arc.core.service;

import java.sql.Connection;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.service.thread.ThreadStructurizeService;

/**
 * ApiNormageService
 *
 * 1- créer la table des données à traiter dans le module</br>
 * 2- calcul de la norme, validité, periodicité sur chaque ligne de la table de
 * donnée</br>
 * 3- déterminer pour chaque fichier si le normage s'est bien déroulé et marquer
 * sa norme, sa validité et sa périodicité</br>
 * 4- créer les tables OK et KO; marquer les info de normage(norme, validité,
 * périodicité) sur chaque ligne de donnée</br>
 * 5- transformation de table de donnée; mise à plat du fichier; suppression et
 * relation</br>
 * 6- mettre à jour le nombre d'enregistrement par fichier après sa
 * transformation</br>
 * 7- sortir les données du module vers l'application</br>
 *
 * @author Manuel SOULIER
 *
 */
@Component
public class ApiNormageService extends AbstractThreadRunnerService<ThreadStructurizeService>
	implements IApiServiceWithOutputTable {

    private static final Logger LOGGER = Logger.getLogger(ApiNormageService.class);
    protected String separator = ",";
    private static final Class<ThreadStructurizeService> THREAD_TYPE = ThreadStructurizeService.class;

    public ApiNormageService() {
	super();
    }

    public ApiNormageService(String aCurrentPhase, String anParametersEnvironment, String aEnvExecution,
	    String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
	super(THREAD_TYPE, aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr, paramBatch);

	// fr.insee.arc.threads.normage
	this.nbThread = 3;

    }

    public ApiNormageService(Connection connexion, String aCurrentPhase, String anParametersEnvironment,
	    String aEnvExecution, String aDirectoryRoot, Integer aNbEnr, String... paramBatch) {
	super(connexion, THREAD_TYPE, aCurrentPhase, anParametersEnvironment, aEnvExecution, aDirectoryRoot, aNbEnr,
		paramBatch);

	// fr.insee.arc.threads.normage
	this.nbThread = 3;
    }

}
