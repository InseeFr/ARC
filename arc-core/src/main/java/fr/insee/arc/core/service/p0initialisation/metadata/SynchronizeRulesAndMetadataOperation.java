package fr.insee.arc.core.service.p0initialisation.metadata;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegleDao;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.scalability.CopyFromCoordinatorToExecutors;
import fr.insee.arc.core.service.global.scalability.ServiceScalability;
import fr.insee.arc.core.service.p0initialisation.dbmaintenance.BddPatcher;
import fr.insee.arc.core.service.p0initialisation.metadata.dao.SynchronizeRulesAndMetadataDao;
import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.dao.CopyObjectsToDatabase;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.LoggerHelper;

public class SynchronizeRulesAndMetadataOperation {

	private static final Logger LOGGER = LogManager.getLogger(SynchronizeRulesAndMetadataOperation.class);

	public SynchronizeRulesAndMetadataOperation(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
		this.dao = new SynchronizeRulesAndMetadataDao(sandbox);
	}

	private Sandbox sandbox;
	
	private SynchronizeRulesAndMetadataDao dao;

	/**
	 * Recopie/remplace les règles définie par l'utilisateur (table de ihm_) dans
	 * Met à jour le schéma des tables métiers correspondant aux règles définies
	 * dans les familles
	 * 
	 * @param connexion
	 * @param envParameters
	 * @param envExecution
	 * @throws ArcException
	 */
	public void synchroniserSchemaExecutionAllNods() throws ArcException {

		copyMetadataAllNods();

		mettreAJourSchemaTableMetierOnNods();
	}

	/**
	 * Recopie/remplace les règles définie par l'utilisateur (table de ihm_) dans
	 * l'environnement d'excécution courant sur tous les noeuds postgres
	 * (coordinator et executors)
	 * 
	 * @param connexion
	 * @param envParameters
	 * @param envExecution
	 * @throws ArcException
	 */
	public void copyMetadataAllNods() throws ArcException {
		
		// on coordinator nod - copy the metadata user rules from metadata schema to sandbox schema
		copyMetadataToSandbox();

		// patch the executor nods with database script
		patchDatabaseToExecutorsAllNods();
		
		// copy the rules in sandbox schema of the coordinator nod to the sandbox schema of the executor nods
		copyMetadataToExecutorsAllNods();
		
		
	}

	/**
	 * Recopier les tables de l'environnement de parametres (IHM) vers
	 * l'environnement d'execution (batch, bas, ...)
	 *
	 * @param connexion
	 * @param anParametersEnvironment
	 * @param anExecutionEnvironment
	 * @throws ArcException
	 */
	private void copyMetadataToSandbox() throws ArcException {
		dao.copyRulesTablesToExecution();
		applyExpressions();
	}

	
	/**
	 * Patch the database with required table and function into all executors pod
	 * @return number of executor pod
	 * @throws ArcException
	 */
	protected void patchDatabaseToExecutorsAllNods() throws ArcException {

		Connection coordinatorConnexion = sandbox.getConnection();
		String envExecution = sandbox.getSchema();

		ThrowingConsumer<Connection> onCoordinator = c -> {
		};

		ThrowingConsumer<Connection> onExecutor = executorConnection -> {
			patchDatabaseToExecutors(coordinatorConnexion, executorConnection, envExecution);
		};

		ServiceScalability.dispatchOnNods(coordinatorConnexion, onCoordinator, onExecutor);

	}
	
	
	/**
	 * Instanciate the metadata required into all executors pod
	 * @return number of executor pod
	 * @throws ArcException
	 */
	protected void copyMetadataToExecutorsAllNods() throws ArcException {
		
		Connection coordinatorConnexion = sandbox.getConnection();
		String envExecution = sandbox.getSchema();
		
		// retrieve table to copy
		List<String> tablesToCopyIntoExecutor = BddPatcher.retrieveRulesTablesFromSchema(coordinatorConnexion,
				envExecution);
		tablesToCopyIntoExecutor
				.addAll(BddPatcher.retrieveExternalTablesUsedInRules(coordinatorConnexion, envExecution));
		tablesToCopyIntoExecutor.addAll(BddPatcher.retrieveModelTablesFromSchema(coordinatorConnexion, envExecution));

		for (String table : new HashSet<String>(tablesToCopyIntoExecutor)) {
			new CopyFromCoordinatorToExecutors().copyWithTee(table);
		}

	}

	/**
	 * Instanciate the metadata required into the given executor pod
	 * 
	 * @param coordinatorConnexion
	 * @param executorConnection
	 * @param envExecution
	 * @throws ArcException
	 */
	private static void patchDatabaseToExecutors(Connection coordinatorConnexion, Connection executorConnection,
			String envExecution) throws ArcException {
		PropertiesHandler properties = PropertiesHandler.getInstance();

		// add utility functions
		BddPatcher.executeBddScript(executorConnection, "BdD/script_function_utility.sql",
				properties.getDatabaseRestrictedUsername(), null, null);

		// add tables for phases if required
		BddPatcher.bddScriptEnvironment(executorConnection, properties.getDatabaseRestrictedUsername(),
				new String[] { envExecution });
		
		// set an empty image of mapping tables
		List<String> tablesToCopyIntoExecutor = new ArrayList<>(BddPatcher.retrieveMappingTablesFromSchema(coordinatorConnexion, envExecution));
		for (String table : new HashSet<String>(tablesToCopyIntoExecutor)) {
			GenericBean gb = SynchronizeRulesAndMetadataDao.execQuerySelectMetaDataOnlyFrom(coordinatorConnexion, table);
			CopyObjectsToDatabase.execCopyFromGenericBeanWithoutDroppingTargetTable(executorConnection, table, gb);
		}
		
	}

	/**
	 * replace an expression in rules
	 * 
	 * @param connexion
	 * @param anExecutionEnvironment
	 * @throws ArcException
	 */
	private void applyExpressions() throws ArcException {

		Connection connexion = sandbox.getConnection();
		String anExecutionEnvironment = sandbox.getSchema();

		// Checks expression validity
		ApplyExpressionRulesOperation expressionService = new ApplyExpressionRulesOperation();
		List<JeuDeRegle> allRuleSets = JeuDeRegleDao.recupJeuDeRegle(connexion, ViewEnum.JEUDEREGLE.getFullName(anExecutionEnvironment));
		for (JeuDeRegle ruleSet : allRuleSets) {
			// Check
			GenericBean expressions = expressionService.fetchExpressions(connexion, anExecutionEnvironment, ruleSet);
			if (expressions.isEmpty()) {
				continue;
			}

			Optional<String> loopInExpressionSet = expressionService.loopInExpressionSet(expressions);
			if (loopInExpressionSet.isPresent()) {
				LoggerHelper.info(LOGGER, "A loop is present in the expression set : " + loopInExpressionSet.get());
				LoggerHelper.info(LOGGER, "The expression set is not applied");
				continue;
			}

			// Apply
			expressions = expressionService.fetchOrderedExpressions(connexion, anExecutionEnvironment, ruleSet);
			if (expressionService.isExpressionSyntaxPresentInControl(connexion, anExecutionEnvironment, ruleSet)) {
				dao.execQueryApplyExpressionsToControl(expressionService, ruleSet, expressions);
			}
			if (expressionService.isExpressionSyntaxPresentInMapping(connexion, anExecutionEnvironment, ruleSet)) {
				dao.execQueryApplyExpressionsToMapping(expressionService, ruleSet, expressions);
			}
		}

	}


	private void mettreAJourSchemaTableMetierOnNods() throws ArcException {

		Connection coordinatorConnexion = sandbox.getConnection();
		String envExecution = sandbox.getSchema();

		ThrowingConsumer<Connection> function = executorConnection -> {
			SynchronizeRulesAndMetadataDao.mettreAJourSchemaTableMetier(executorConnection, envExecution);
		};

		ServiceScalability.dispatchOnNods(coordinatorConnexion, function, function);

	}



}
