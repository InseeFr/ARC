package fr.insee.arc.core.service.p0initialisation.pilotage;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.DatabaseMaintenance;
import fr.insee.arc.core.service.global.dao.HashFileNameConversion;
import fr.insee.arc.core.service.global.dao.PhaseOperations;
import fr.insee.arc.core.service.global.dao.PilotageOperations;
import fr.insee.arc.core.service.global.scalability.CopyFromCoordinatorToExecutors;
import fr.insee.arc.core.service.global.scalability.ServiceScalability;
import fr.insee.arc.core.service.p0initialisation.pilotage.dao.SynchronizeDataByPilotageDao;
import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;

public class SynchronizeDataByPilotageOperation {
	
	private static final Logger LOGGER = LogManager.getLogger(SynchronizeDataByPilotageOperation.class);
	
	public SynchronizeDataByPilotageOperation(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
		// compute table name
		tableOfIdSource = ViewEnum.ID_SOURCE_SYNCHRO.getFullName(sandbox.getSchema());
	}

	private Sandbox sandbox;
	private String tableOfIdSource;


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
		
		// mark mapping tables
		if (mappingTablesAreAllEmpty(coordinatorConnexion, envExecution))
		{
			return;
		}
		
		if (optionalProvidedIdSourceToDelete == null) {
			deleteUnusedDataRecordNotFoundInPilotageAllNods(coordinatorConnexion, envExecution,TraitementPhase.MAPPING, TraitementEtat.OK);
			deleteUnusedDataRecordNotFoundInPilotageAllNods(coordinatorConnexion, envExecution,TraitementPhase.MAPPING, TraitementEtat.KO);
			return;
		}
		
		deleteDataRecordFromTheIdSourceProvidedAllNods(coordinatorConnexion, envExecution,TraitementPhase.MAPPING, TraitementEtat.OK, optionalProvidedIdSourceToDelete);
		deleteDataRecordFromTheIdSourceProvidedAllNods(coordinatorConnexion, envExecution,TraitementPhase.MAPPING, TraitementEtat.KO, optionalProvidedIdSourceToDelete);
		
	}

	
	private void deleteUnusedDataRecordNotFoundInPilotageAllNods(Connection coordinatorConnexion, String envExecution, TraitementPhase phase, TraitementEtat etat) throws ArcException {

		// create the table of id_source found in pilotage table
		UtilitaireDao.get(0).executeRequest(coordinatorConnexion, 
				PilotageOperations.queryCreateIdSourceFromPilotage(this.tableOfIdSource, envExecution, phase, etat));
		
		// copy the table of idsource to executors
		CopyFromCoordinatorToExecutors copy = new CopyFromCoordinatorToExecutors();
		copy.copyWithTee(this.tableOfIdSource);
		
		// delete data from mapping_ok tables
		// that matches optionalProvidedIdSourceToDelete if provided
		// that don't match any id_source in the tableOfIdSourceToKeep if optionalProvidedIdSourceToDelete not provided
		ThrowingConsumer<Connection> function = connection -> deleteUnusedDataRecordsNotFoundInPilotage(connection, envExecution, phase, etat);
		ServiceScalability.dispatchOnNods(coordinatorConnexion, function, function);
		
	}


	/**
	 * Delete the unreferenced data records found in all tables that may contains
	 * data i.e. currently tables of the "mapping" phase
	 * Delete the unreferenced data records found in the tables corresponding to a
	 * given phase and state
	 * 
	 * @param executorConnection
	 * @param envExecution
	 * @param listIdSourceInPilotage
	 * @throws ArcException
	 */
	private void deleteUnusedDataRecordsNotFoundInPilotage(Connection executorConnection, String envExecution, TraitementPhase phase,
			TraitementEtat etat) throws ArcException {
		
		// query the mapping table that contain data
		List<String> envTablesWithRecords
		 = listOfNotEmptyMappingTables(executorConnection, envExecution, phase, etat);
		
		// if no tables with records, exit
		if (envTablesWithRecords.isEmpty())
		{
			SynchronizeDataByPilotageDao.dropTable(executorConnection, tableOfIdSource);
			return;
		}
	
		// iterate over data tables
		for (String dataTable : envTablesWithRecords) {
				SynchronizeDataByPilotageDao.keepDataRecordsFoundInIdSourceOnly(executorConnection, this.tableOfIdSource ,dataTable);
		}
		
		SynchronizeDataByPilotageDao.dropTable(executorConnection, tableOfIdSource);
	
	
	}

	
	private void deleteDataRecordFromTheIdSourceProvidedAllNods(Connection coordinatorConnexion, String envExecution, TraitementPhase phase,
			TraitementEtat etat, List<String> providedIdSourceToDelete) throws ArcException {
		
		ThrowingConsumer<Connection> function = connection -> deleteDataRecordFromTheIdSourceProvided(connection, envExecution, phase, etat,providedIdSourceToDelete);
		
		ServiceScalability.dispatchOnNods(coordinatorConnexion, function, function);
		
	}

	/**
	 * Delete the unreferenced data records found in all tables that may contains
	 * data i.e. currently tables of the "mapping" phase
	 * Delete the unreferenced data records found in the tables corresponding to a
	 * given phase and state
	 * 
	 * @param executorConnection
	 * @param envExecution
	 * @param listIdSourceInPilotage
	 * @throws ArcException
	 */
	private void deleteDataRecordFromTheIdSourceProvided(Connection executorConnection, String envExecution, TraitementPhase phase,
			TraitementEtat etat, List<String> providedIdSourceToDelete) throws ArcException {
	
		List<String> envTablesWithRecords
		 = listOfNotEmptyMappingTables(executorConnection, envExecution, phase, etat);
		
		// if no tables with records, exit
		if (envTablesWithRecords.isEmpty())
		{
			SynchronizeDataByPilotageDao.dropTable(executorConnection, tableOfIdSource);
			return;
		}
	
		// materialize the table id_source table if provided
		SynchronizeDataByPilotageDao.execQueryMaterializeOnExecutorIdSource(executorConnection, tableOfIdSource , providedIdSourceToDelete);
	
		// iterate over data tables
		for (String dataTable : envTablesWithRecords) {
			SynchronizeDataByPilotageDao.deleteDataRecordsFoundInIdSource(executorConnection, tableOfIdSource, dataTable);
		}
		
		SynchronizeDataByPilotageDao.dropTable(executorConnection, tableOfIdSource);
		
	}


	/**
	 * 
	 * @param coordinatorConnexion
	 * @param envExecution
	 * @return
	 * @throws ArcException 
	 */
	private boolean mappingTablesAreAllEmpty(Connection coordinatorConnexion, String envExecution) throws ArcException {
		List<String> tablesWithRecords = new ArrayList<>();
		ThrowingConsumer<Connection> functionMarkMappingTablesWithRecords = connection -> markMappingTablesWithRecords(
				connection, envExecution, tablesWithRecords);
		
		ServiceScalability.dispatchOnNods(coordinatorConnexion, functionMarkMappingTablesWithRecords, functionMarkMappingTablesWithRecords);
		
		// return true when there is not any mapping tables with records
		return tablesWithRecords.isEmpty();
	}


	/**
	 * Mark tables with data records
	 * 
	 * @param executorConnection
	 * @param envExecution
	 * @param listIdSourceInPilotage
	 * @throws ArcException
	 */
	private static void markMappingTablesWithRecords(Connection connection, String envExecution, List<String> tablesWithRecord ) throws ArcException {
		
		SynchronizeDataByPilotageDao.unregisterTablesWithRecord(connection, envExecution);
		List<String> dataTables = PhaseOperations.selectPhaseDataTablesFoundInEnv(connection, envExecution);
		
		// récupérer la liste des tables de la phase
		List<String> dataTablesFilteredOnMappingPhase = dataTables.stream()
				.filter(nomTable -> PhaseOperations.extractPhaseFromTableName(nomTable).equals(TraitementPhase.MAPPING))
				.filter(nomTable -> SynchronizeDataByPilotageDao.isDataTableHasRecords(connection, nomTable))
				.collect(Collectors.toList());

		// if no phase tables detected, exit
		if (dataTablesFilteredOnMappingPhase.isEmpty()) {
			return;
		}
		
		tablesWithRecord.addAll(dataTablesFilteredOnMappingPhase);
		SynchronizeDataByPilotageDao.registerTablesWithRecord(connection, envExecution, dataTablesFilteredOnMappingPhase);
		
	}

	private List<String> listOfNotEmptyMappingTables(Connection executorConnection, String envExecution, TraitementPhase phase, TraitementEtat etat) throws ArcException {

		// query the mapping table that contain data
		List<String> dataTables = SynchronizeDataByPilotageDao.execQueryMappingTablesWithRecords(executorConnection, envExecution);
		
		// récupérer la liste des tables de la phase
		List<String> envTablesWithRecords = dataTables.stream()
				.filter(nomTable -> PhaseOperations.extractPhaseFromTableName(nomTable).equals(phase)
						&& PhaseOperations.extractEtatFromTableName(nomTable).equals(etat))
				.collect(Collectors.toList());
		
		return envTablesWithRecords;
	}

	
}
