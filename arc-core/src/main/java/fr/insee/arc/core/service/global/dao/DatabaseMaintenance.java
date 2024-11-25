package fr.insee.arc.core.service.global.dao;

import java.sql.Connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.scalability.ServiceScalability;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;

public class DatabaseMaintenance {

	private DatabaseMaintenance() {
		throw new IllegalStateException("Utility class");
	}

	protected static final Logger LOGGER = LogManager.getLogger(DatabaseMaintenance.class);

	
	/**
	 * dispatch on every nods the mainteance of catalog
	 * @param optionalProvidedIdSourceToDrop
	 * @throws ArcException
	 */
	public static void maintenancePgCatalogAllNods(Connection coordinatorConnection, String type) throws ArcException {

		ThrowingConsumer<Connection> function = executorConnection -> UtilitaireDao.get(0).maintenancePgCatalog(executorConnection, type);

		ServiceScalability.dispatchOnNods(coordinatorConnection, function, function);

	}
	
	/**
	 * Maintenance sur la table de pilotage
	 * 
	 * @param connexion
	 * @param envExecution
	 * @param type
	 */
	public static void maintenancePilotage(Connection coordinatorConnection, String envExecution, String type) {
		StaticLoggerDispatcher.info(LOGGER, "** Maintenance Pilotage **");

		String tablePil = ViewEnum.PILOTAGE_FICHIER.getFullName(envExecution);
		
		try {
			UtilitaireDao.get(0).executeImmediate(coordinatorConnection, FormatSQL.vacuumSecured(tablePil, type));
		} catch (Exception e) {
			StaticLoggerDispatcher.error(LOGGER, "Error in ApiService.maintenancePilotage");
		}
	}

	/**
	 * analyze and vacuum the postgres catalog tables analyze 
	 * vacuum the pilotage table located in the sandbox schema
	 * 
	 * @param connexion    the jdbc connexion
	 * @param envExecution the sandbox schema
	 * @throws ArcException 
	 */
	public static void maintenanceDatabaseClassic(Connection coordinatorConnection, String envExecution) throws ArcException {
		
		maintenancePgCatalogAllNods(coordinatorConnection, FormatSQL.VACUUM_OPTION_NONE);
		
		maintenancePilotage(coordinatorConnection, envExecution, FormatSQL.VACUUM_OPTION_NONE);
	}
	
}
