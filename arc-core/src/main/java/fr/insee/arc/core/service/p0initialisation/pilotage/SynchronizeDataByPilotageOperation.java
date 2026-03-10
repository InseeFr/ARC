package fr.insee.arc.core.service.p0initialisation.pilotage;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.DatabaseMaintenance;
import fr.insee.arc.core.service.global.dao.HashFileNameConversion;
import fr.insee.arc.core.service.global.dao.PhaseOperations;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.core.service.global.scalability.ServiceScalability;
import fr.insee.arc.core.service.p0initialisation.pilotage.bo.ListIdSourceInPilotage;
import fr.insee.arc.core.service.p0initialisation.pilotage.dao.SynchronizeDataByPilotageDao;
import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;

public class SynchronizeDataByPilotageOperation {
	
	private static final Logger LOGGER = LogManager.getLogger(SynchronizeDataByPilotageOperation.class);
	
	public SynchronizeDataByPilotageOperation(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
	}

	private Sandbox sandbox;


	/**
	 * Remise en coherence des tables de données avec la table de pilotage
	 *
	 * @param connexion
	 * @param envExecution
	 * @throws ArcException
	 */
	public void synchronizeDataByPilotage() throws ArcException {
		LoggerHelper.info(LOGGER, "synchronisationEnvironmentByPilotage");

		// maintenance de la table de pilotage
		// retirer les "encours" de la table de pilotage
		LoggerHelper.info(LOGGER, "** Maintenance table de pilotage **");

		// pour chaque fichier de la phase de pilotage, remet à etape='1' pour sa
		// derniere phase valide
		resetEtapePilotage();

		// recrée la table de pilotage, ses index, son trigger
		rebuildPilotage();
	
		// drop des tables temporaires de travail
		dropUnusedTemporaryTablesAllNods();

		// pour chaque table de l'environnement d'execution courant
		dropUnusedDataTablesAllNods(null);

		// pour chaque table de l'environnement d'execution courant
		deleteUnusedDataRecordsAllNods(null);

		// maintenance des tables de catalogue car postgres ne le réalise pas
		// correctement sans mettre en oeuvre
		// une stratégie de vacuum hyper agressive et donc ajouter une spécificité pour
		// les DBAs
		
		DatabaseMaintenance.maintenancePgCatalogAllNods(this.sandbox.getConnection(), FormatSQL.VACUUM_OPTION_NONE);
	
	}
	
	
	/**
	 * la variable etape indique si c'est bien l'etape à considerer pour traitement
	 * ou pas etape='1' : phase à considerer, sinon etape='0'
	 *
	 * @return
	 * @throws ArcException
	 */
	private void resetEtapePilotage() throws ArcException {
		SynchronizeDataByPilotageDao.resetEtapePilotageDao(this.sandbox.getConnection(), this.sandbox.getSchema());
	}
	
	

	private void rebuildPilotage() throws ArcException {
		SynchronizeDataByPilotageDao.rebuildPilotageDao(this.sandbox.getConnection(), this.sandbox.getSchema());
	}


	/**
	 * drop the unused temporary table on coordinator and on executors if there is
	 * any
	 * 
	 * @param coordinatorConnexion
	 * @return the number of executor nods in order to know if method worked on
	 *         executors too
	 * @throws ArcException
	 */
	private int dropUnusedTemporaryTablesAllNods() throws ArcException {

		ThrowingConsumer<Connection> function = c -> {
			SynchronizeDataByPilotageDao.dropUnusedTemporaryTablesOnConnection(c,this.sandbox.getSchema());
		};

		return ServiceScalability.dispatchOnNods(this.sandbox.getConnection(), function, function);

	}

	/**
	 * dispatch on every nods the void that drop unused data tables
	 * @param optionalProvidedIdSourceToDrop
	 * @throws ArcException
	 */
	public void dropUnusedDataTablesAllNods(List<String> optionalProvidedIdSourceToDrop) throws ArcException {

		Connection coordinatorConnexion = sandbox.getConnection();
		String envExecution = sandbox.getSchema();
		
		ThrowingConsumer<Connection> function = executorConnection -> dropUnusedDataTables(
				coordinatorConnexion, executorConnection, envExecution, optionalProvidedIdSourceToDrop);

		ServiceScalability.dispatchOnNods(coordinatorConnexion, function, function);

	}

	/**
	 * call method to drop the unused data table found on an given executor nod
	 * 
	 * @param coordinatorConnexion
	 * @param executorConnection
	 * @param envExecution
	 * @param tablePilotage
	 * @throws ArcException
	 */
	private static void dropUnusedDataTables(Connection coordinatorConnexion, Connection executorConnection,
			String envExecution, List<String> providedIdSourceToDrop) throws ArcException {
		// This returns the list of the template data table for phases
		// For example, "chargement_ok" is the template table for the phase called
		// "CHARGEMENT" in an "OK" state
		// The table names from the files proceeded in the phase "CHARGEMENT" will be
		// based on the table template name
		// chargement_ok_child_<hash_of_filename1>
		// chargement_ok_child_<hash_of_filename2>
		// ...
		// Historically there was an inheritance link between template table (parent)
		// and all the real data tables (children)
		// but it had been removed for performance issue
		List<String> templateDataTablesThatCanBeDropped = PhaseOperations
				.selectPhaseDataTablesFoundInEnv(executorConnection, envExecution).stream()
				.filter(nomTable -> !PhaseOperations.extractPhaseFromTableName(nomTable)
						.equals(TraitementPhase.MAPPING))
				.collect(Collectors.toList());

		// no data tables to check ? exit
		if (templateDataTablesThatCanBeDropped.isEmpty()) {
			return;
		}

		dropUnusedDataTables(coordinatorConnexion, executorConnection, envExecution,
				templateDataTablesThatCanBeDropped, providedIdSourceToDrop);

	}

	/**
	 * iterate over data table found in executor nod drop the ones that are no
	 * longer referenced in the pilotage table found on coordinator nod
	 * 
	 * @param coordinatorConnexion
	 * @param executorConnection
	 * @param tablePilotage
	 * @param dataTablesThatCanBeDropped
	 * @throws ArcException
	 */
	private static void dropUnusedDataTables(Connection coordinatorConnexion, Connection executorConnection,
			String envExecution, List<String> templateDataTablesThatCanBeDropped, List<String> providedIdSourceToDrop)
			throws ArcException {
		// Build the list of child data tables to drop

		List<String> childDataTablesToBeDropped = new ArrayList<>();

		for (String templateDataTable : templateDataTablesThatCanBeDropped) {

			if (providedIdSourceToDrop != null) {
				// if list of idSource is provided, calculate the corresponding tablenames and
				// add it to drop list
				for (String idSource : providedIdSourceToDrop) {
					childDataTablesToBeDropped.add(HashFileNameConversion.tableOfIdSource(templateDataTable, idSource));
				}
			} else {
				TraitementPhase phase = PhaseOperations.extractPhaseFromTableName(templateDataTable);
				TraitementEtat etat = PhaseOperations.extractEtatFromTableName(templateDataTable);

				// retrieve all the children tables of the template table
				List<String> childDataTables = PhaseOperations.selectAllChildrenPhaseDataTables(executorConnection,
						templateDataTable);

				// it could be more bulky but it would be less readable and useless; this is
				// rarely triggered and access 10000 objects at max
				// loop over children tables
				for (String childDataTable : childDataTables) {

					// retrieve the idSource of the childDataTable
					String idSource = PhaseOperations.execQuerySelectIdSourceOfChildDataTable(executorConnection,
							childDataTable);
					String etape = PilotageOperations.execQuerySelectEtapeForIdSource(coordinatorConnexion, envExecution,
							phase, etat, idSource);

					// if no references in pilotage table, mark for drop
					if (etape == null) {
						childDataTablesToBeDropped.add(childDataTable);
					}
				}
			}
		}

		SynchronizeDataByPilotageDao.dropDataTables(executorConnection, childDataTablesToBeDropped);
	}



	/**
	 * delete the record from the data tables on all nods - according to pilotage
	 * table if providedIdSourceToDelete is not provided - according to
	 * providedIdSourceToDelete if provided
	 * 
	 * @param coordinatorConnexion
	 * @param envExecution
	 * @param tablePilotage
	 * @param optionalProvidedIdSourceToDelete
	 * @throws ArcException
	 */
	public void deleteUnusedDataRecordsAllNods(List<String> optionalProvidedIdSourceToDelete) throws ArcException {

		Connection coordinatorConnexion = sandbox.getConnection();
		String envExecution = sandbox.getSchema();
		
		ListIdSourceInPilotage listIdSourceInPilotage = new ListIdSourceInPilotage();

		if (optionalProvidedIdSourceToDelete == null) {
			listIdSourceInPilotage
					.addSource(coordinatorConnexion, envExecution, TraitementPhase.MAPPING, TraitementEtat.OK)
					.addSource(coordinatorConnexion, envExecution, TraitementPhase.MAPPING, TraitementEtat.KO);
		}

		ThrowingConsumer<Connection> function = executorConnection -> deleteUnusedDataRecordsAllTables(
				executorConnection, envExecution, listIdSourceInPilotage, optionalProvidedIdSourceToDelete);

		ServiceScalability.dispatchOnNods(coordinatorConnexion, function, function);

	}

	/**
	 * Delete the unreferenced data records found in all tables that may contains
	 * data i.e. currently tables of the "mapping" phase
	 * 
	 * @param executorConnection
	 * @param envExecution
	 * @param listIdSourceInPilotage
	 * @throws ArcException
	 */
	private static void deleteUnusedDataRecordsAllTables(Connection executorConnection, String envExecution,
			ListIdSourceInPilotage listIdSourceInPilotage, List<String> providedIdSourceToDelete) throws ArcException {

		List<String> dataTables = PhaseOperations.selectPhaseDataTablesFoundInEnv(executorConnection, envExecution);

		// if no phase tables, exit
		if (dataTables.isEmpty()) {
			return;
		}

		deleteUnusedDataRecords(executorConnection, listIdSourceInPilotage, dataTables, TraitementPhase.MAPPING,
				TraitementEtat.OK, providedIdSourceToDelete);

		deleteUnusedDataRecords(executorConnection, listIdSourceInPilotage, dataTables, TraitementPhase.MAPPING,
				TraitementEtat.KO, providedIdSourceToDelete);

	}

	/**
	 * Delete the unreferenced data records found in the tables corresponding to a
	 * given phase and state
	 * 
	 * @param executorConnection
	 * @param envExecution
	 * @param listIdSourceInPilotage
	 * @throws ArcException
	 */
	private static void deleteUnusedDataRecords(Connection executorConnection,
			ListIdSourceInPilotage listIdSourceInPilotage, List<String> envTables, TraitementPhase phase,
			TraitementEtat etat, List<String> providedIdSourceToDelete) throws ArcException {
		// récupérer la liste des tables de la phase
		List<String> envTablesWithRecords = envTables.stream()
				.filter(nomTable -> PhaseOperations.extractPhaseFromTableName(nomTable).equals(phase)
						&& PhaseOperations.extractEtatFromTableName(nomTable).equals(etat))
				.collect(Collectors.toList());

		// quels enregistrements à effacer
		if (envTablesWithRecords.isEmpty()) {
			return;
		}
		
		// materialize the table id_source table
		if (providedIdSourceToDelete == null) {
			SynchronizeDataByPilotageDao.execQueryMaterializeOnExecutorIdSource(executorConnection, listIdSourceInPilotage.getIdSourceInPilotage(phase, etat));
		}
		else
		{
			SynchronizeDataByPilotageDao.execQueryMaterializeOnExecutorIdSource(executorConnection, providedIdSourceToDelete);
		}

		// iterate over data tables
		for (String dataTable : envTablesWithRecords) {
			if (providedIdSourceToDelete == null) {
				SynchronizeDataByPilotageDao.keepDataRecordsFoundInIdSourceOnly(executorConnection, dataTable);
			} else {
				SynchronizeDataByPilotageDao.deleteDataRecordsFoundInIdSource(executorConnection, dataTable);
			}
		}

	}

	
}
