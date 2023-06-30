package fr.insee.arc.core.service.api.query;

import java.sql.Connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.model.TraitementTableExecution;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.FormatSQL;

public class ServiceDatabaseMaintenance {

	private ServiceDatabaseMaintenance() {
		throw new IllegalStateException("Utility class");
	}

	protected static final Logger LOGGER = LogManager.getLogger(ServiceDatabaseMaintenance.class);

	/**
	 * Maintenance sur la table de pilotage
	 * 
	 * @param connexion
	 * @param envExecution
	 * @param type
	 */
	private static void maintenancePilotage(Integer poolIndex, Connection connexion, String envExecution, String type) {
		String tablePil = ServiceTableNaming.dbEnv(envExecution) + TraitementTableExecution.PILOTAGE_FICHIER;
		StaticLoggerDispatcher.info(LOGGER, "** Maintenance Pilotage **");

		try {
			UtilitaireDao.get(poolIndex).executeImmediate(connexion, FormatSQL.analyzeSecured(tablePil));
			UtilitaireDao.get(poolIndex).executeImmediate(connexion, FormatSQL.vacuumSecured(tablePil, type));
		} catch (Exception e) {
			StaticLoggerDispatcher.error(LOGGER, "Error in ApiService.maintenancePilotage");
		}
	}

	/**
	 * 
	 * @param connexion
	 * @param type
	 */
	public static void maintenancePgCatalog(Integer poolIndex, Connection connexion, String type) {
		// postgres libere mal l'espace sur ces tables qaund on fait trop d'opération
		// sur les colonnes
		// vaccum full sinon ca fait quasiment rien ...
		StaticLoggerDispatcher.info(LOGGER, "** Maintenance Catalogue **");
		UtilitaireDao.get(poolIndex).maintenancePgCatalog(connexion, type);
	}

	/**
	 * classic database maintenance routine 2 vacuum are sent successively to
	 * analyze and remove dead tuple completely from
	 * 
	 * @param connexion    the jdbc connexion
	 * @param envExecution the sandbox schema
	 */
	public static void maintenanceDatabaseClassic(Connection connexion, String envExecution) {
		maintenanceDatabaseClassic(0, connexion, envExecution);
	}

	public static void maintenanceDatabaseClassic(Integer poolIndex, Connection connexion, String envExecution) {
		maintenanceDatabase(poolIndex, connexion, envExecution, FormatSQL.VACUUM_OPTION_NONE);
	}

	/**
	 * analyze and vacuum on postgres catalog tables analyze and vacuum on the
	 * pilotage table located in the sandbox schema
	 * 
	 * @param connexion       the jdbc connexion
	 * @param envExecution    the sandbox schema
	 * @param typeMaintenance FormatSQL.VACUUM_OPTION_FULL or
	 *                        FormatSQL.VACUUM_OPTION_NONE
	 */
	private static void maintenanceDatabase(Integer poolIndex, Connection connexion, String envExecution,
			String typeMaintenance) {
		maintenancePgCatalog(poolIndex, connexion, typeMaintenance);

		maintenancePilotage(poolIndex, connexion, envExecution, typeMaintenance);

		StaticLoggerDispatcher.info(LOGGER, "** Fin de maintenance **");
	}

}
