package fr.insee.arc.core.service.p0initialisation.metadata;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementTableParametre;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.global.bo.JeuDeRegleDao;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.TableNaming;
import fr.insee.arc.core.service.global.scalability.ServiceScalability;
import fr.insee.arc.core.service.p0initialisation.dbmaintenance.BddPatcher;
import fr.insee.arc.core.service.p5mapping.engine.ExpressionService;
import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.dao.CopyObjectsToDatabase;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.dataobjects.TypeEnum;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.AttributeValue;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.structure.tree.HierarchicalView;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

public class SynchronizeUserRulesAndMetadata {


	private static final Logger LOGGER = LogManager.getLogger(SynchronizeUserRulesAndMetadata.class);

	public SynchronizeUserRulesAndMetadata(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
	}
	
	private Sandbox sandbox;
	

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
	public void copyMetadataAllNods()
			throws ArcException {
		copyMetadataToSandbox();

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
		copyRulesTablesToExecution();
		applyExpressions();
	}
	
	/**
	 * Instanciate the metadata required into all executors pod
	 * 
	 * @param envExecution
	 * @throws ArcException
	 */
	protected int copyMetadataToExecutorsAllNods()
			throws ArcException {
		
		Connection coordinatorConnexion = sandbox.getConnection();
		String envExecution = sandbox.getSchema();

		ThrowingConsumer<Connection, ArcException> onCoordinator = c -> {
		};

		ThrowingConsumer<Connection, ArcException> onExecutor = executorConnection -> {
			copyMetaDataToExecutors(coordinatorConnexion, executorConnection, envExecution);
		};

		return ServiceScalability.dispatchOnNods(coordinatorConnexion, onCoordinator, onExecutor);

	}

	/**
	 * Instanciate the metadata required into the given executor pod
	 * 
	 * @param coordinatorConnexion
	 * @param executorConnection
	 * @param envExecution
	 * @throws ArcException
	 */
	private static void copyMetaDataToExecutors(Connection coordinatorConnexion, Connection executorConnection,
			String envExecution) throws ArcException {
		PropertiesHandler properties = PropertiesHandler.getInstance();

		// add utility functions
		BddPatcher.executeBddScript(executorConnection, "BdD/script_function_utility.sql",
				properties.getDatabaseRestrictedUsername(), null, null);

		// add tables for phases if required
		BddPatcher.bddScriptEnvironmentExecutor(executorConnection, properties.getDatabaseRestrictedUsername(),
				new String[] { envExecution });

		// copy tables

		ArrayList<String> tablesToCopyIntoExecutor = BddPatcher.retrieveRulesTablesFromSchema(coordinatorConnexion,
				envExecution);
		tablesToCopyIntoExecutor
				.addAll(BddPatcher.retrieveExternalTablesUsedInRules(coordinatorConnexion, envExecution));
		tablesToCopyIntoExecutor.addAll(BddPatcher.retrieveModelTablesFromSchema(coordinatorConnexion, envExecution));

		for (String table : new HashSet<String>(tablesToCopyIntoExecutor)) {
			GenericBean gb = new GenericBean(UtilitaireDao.get(0).executeRequest(coordinatorConnexion,
					new ArcPreparedStatementBuilder("SELECT * FROM " + table)));

			CopyObjectsToDatabase.execCopyFromGenericBean(executorConnection, table, gb);

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
		ExpressionService expressionService = new ExpressionService();
		ArrayList<JeuDeRegle> allRuleSets = JeuDeRegleDao.recupJeuDeRegle(connexion,
				anExecutionEnvironment + ".jeuderegle");
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
				UtilitaireDao.get(0).executeRequest(connexion,
						expressionService.applyExpressionsToControl(ruleSet, expressions, anExecutionEnvironment));
			}
			if (expressionService.isExpressionSyntaxPresentInMapping(connexion, anExecutionEnvironment, ruleSet)) {
				UtilitaireDao.get(0).executeRequest(connexion,
						expressionService.applyExpressionsToMapping(ruleSet, expressions, anExecutionEnvironment));
			}
		}

	}

	
	/**
	 * Copy the table containing user rules to the sandbox so they will be used by
	 * the sandbox process
	 * 
	 * @param coordinatorConnexion
	 * @param anParametersEnvironment
	 * @param anExecutionEnvironment
	 * @throws ArcException
	 */
	private void copyRulesTablesToExecution() throws ArcException {
		LoggerHelper.info(LOGGER, "copyTablesToExecution");
		
		Connection coordinatorConnexion = sandbox.getConnection();
		String anExecutionEnvironment = sandbox.getSchema();
		
		try {

			anExecutionEnvironment = anExecutionEnvironment.replace(".", "_");

			StringBuilder requete = new StringBuilder();
			TraitementTableParametre[] r = TraitementTableParametre.values();
			StringBuilder condition = new StringBuilder();
			String modaliteEtat = anExecutionEnvironment.replace("_", ".");
			for (int i = 0; i < r.length; i++) {
				// on créé une table image de la table venant de l'ihm
				// (environnement de parametre)
				TraitementTableParametre parameterTable = r[i];
				String tableImage = FormatSQL.temporaryTableName(parameterTable.getTablenameInSandbox().getFullName(anExecutionEnvironment));

				// recopie partielle (en fonction de l'environnement
				// d'exécution)
				// pour les tables JEUDEREGLE, CONTROLE_REGLE et MAPPING_REGLE
				condition.setLength(0);
				if (parameterTable == TraitementTableParametre.NORME) {
					condition.append(" WHERE etat='1'");
				} else if (parameterTable == TraitementTableParametre.CALENDRIER) {
					condition.append(" WHERE etat='1' ");
					condition.append(" and exists (select 1 from " + ViewEnum.IHM_NORME.getFullName()
							+ " b where a.id_norme=b.id_norme and b.etat='1')");
				} else if (parameterTable == TraitementTableParametre.JEUDEREGLE) {
					condition.append(" WHERE etat=lower('" + modaliteEtat + "')");
					condition.append(" and exists (select 1 from " + ViewEnum.IHM_NORME.getFullName()
							+ " b where a.id_norme=b.id_norme and b.etat='1')");
					condition.append(" and exists (select 1 from " + ViewEnum.IHM_CALENDRIER.getFullName()
							+ " b where a.id_norme=b.id_norme and a.periodicite=b.periodicite and a.validite_inf=b.validite_inf and a.validite_sup=b.validite_sup and b.etat='1')");
				} else if (parameterTable.isPartOfRuleset()) {
					condition.append(" WHERE exists (select 1 from " + ViewEnum.IHM_NORME.getFullName()
							+ " b where a.id_norme=b.id_norme and b.etat='1')");
					condition.append(" and exists (select 1 from " + ViewEnum.IHM_CALENDRIER.getFullName()
							+ " b where a.id_norme=b.id_norme and a.periodicite=b.periodicite and a.validite_inf=b.validite_inf and a.validite_sup=b.validite_sup and b.etat='1')");
					condition.append(" and exists (select 1 from " + ViewEnum.IHM_JEUDEREGLE.getFullName()
							+ " b where a.id_norme=b.id_norme and a.periodicite=b.periodicite and a.validite_inf=b.validite_inf and a.validite_sup=b.validite_sup AND a.version=b.version and b.etat=lower('"
							+ modaliteEtat + "'))");
				}
				requete.append(FormatSQL.dropTable(tableImage));

				requete.append("CREATE TABLE " + tableImage + " " + FormatSQL.WITH_NO_VACUUM + " AS SELECT a.* FROM "
						+ r[i].getTablenameInMetadata().getFullName() + " AS a " + condition + ";\n");

				requete.append(FormatSQL.dropTable(parameterTable.getTablenameInSandbox().getFullName(anExecutionEnvironment)));
				requete.append("ALTER TABLE " + tableImage + " rename to "
						+ ManipString.substringAfterLast(parameterTable.getTablenameInSandbox().getTableName(), ".") + "; \n");
			}
			UtilitaireDao.get(0).executeBlock(coordinatorConnexion, requete);

			// Dernière étape : recopie des tables de nomenclature et des tables prefixées
			// par ext_ du schéma arc vers schéma courant

			requete.setLength(0);

			// 1.Préparation des requêtes de suppression des tables nmcl_ et ext_ du schéma
			// courant

			ArcPreparedStatementBuilder requeteSelectDrop = new ArcPreparedStatementBuilder();
			requeteSelectDrop
					.append(" SELECT 'DROP TABLE IF EXISTS '||schemaname||'.'||tablename||';'  AS requete_drop");
			requeteSelectDrop.append(" FROM pg_tables where schemaname = "
					+ requeteSelectDrop.quoteText(anExecutionEnvironment.toLowerCase()) + " ");
			requeteSelectDrop.append(" AND tablename SIMILAR TO '%nmcl%|%ext%'");

			ArrayList<String> requetesDeSuppressionTablesNmcl = new GenericBean(
					UtilitaireDao.get(0).executeRequest(coordinatorConnexion, requeteSelectDrop)).mapContent()
					.get("requete_drop");

			if (requetesDeSuppressionTablesNmcl != null) {
				for (String requeteDeSuppression : requetesDeSuppressionTablesNmcl) {
					requete.append("\n ").append(requeteDeSuppression);
				}
			}

			// 2.Préparation des requêtes de création des tables
			ArrayList<String> requetesDeCreationTablesNmcl = new GenericBean(UtilitaireDao.get(0)
					.executeRequest(coordinatorConnexion, new ArcPreparedStatementBuilder(
							"select tablename from pg_tables where (tablename like 'nmcl\\_%' OR tablename like 'ext\\_%') and schemaname='arc'")))
					.mapContent().get("tablename");

			if (requetesDeCreationTablesNmcl != null) {
				for (String tableName : requetesDeCreationTablesNmcl) {
					requete.append("\n CREATE TABLE " + TableNaming.dbEnv(anExecutionEnvironment) + tableName + " "
							+ FormatSQL.WITH_NO_VACUUM + " AS SELECT * FROM arc." + tableName + ";");
				}
			}

			// 3.Execution du script Sql de suppression/création
			UtilitaireDao.get(0).executeBlock(coordinatorConnexion, requete);

		} catch (Exception e) {
			LoggerHelper.trace(LOGGER,
					"Problème lors de la copie des tables vers l'environnement : " + anExecutionEnvironment);
			LoggerHelper.error(LOGGER, "Error in ApiInitialisation.copyRulesTablesToExecution");
			throw e;
		}
	}
	
	private void mettreAJourSchemaTableMetierOnNods() throws ArcException {

		Connection coordinatorConnexion = sandbox.getConnection();
		String envExecution = sandbox.getSchema();
		
		ThrowingConsumer<Connection, ArcException> function = executorConnection -> {
			mettreAJourSchemaTableMetier(executorConnection, envExecution);
		};

		ServiceScalability.dispatchOnNods(coordinatorConnexion, function, function);

	}

	/**
	 * Créer ou detruire les colonnes ou les tables métiers en comparant ce qu'il y
	 * a en base à ce qu'il y a de déclaré dans la table des familles de norme
	 *
	 * @param connexion
	 * @throws ArcException
	 */
	private static void mettreAJourSchemaTableMetier(Connection connexion, String envExecution)
			throws ArcException {
		LoggerHelper.info(LOGGER, "mettreAJourSchemaTableMetier");
		/*
		 * Récupérer la table qui mappe : famille / table métier / variable métier et
		 * type de la variable
		 */
		ArcPreparedStatementBuilder requeteRef = new ArcPreparedStatementBuilder();
		requeteRef.append("SELECT lower(id_famille), lower('" + TableNaming.dbEnv(envExecution)
				+ "'||nom_table_metier), lower(nom_variable_metier), lower(type_variable_metier) FROM " + ViewEnum.IHM_MOD_VARIABLE_METIER.getFullName());

		List<List<String>> relationalViewRef = Format
				.patch(UtilitaireDao.get(0).executeRequestWithoutMetadata(connexion, requeteRef));
		HierarchicalView familleToTableToVariableToTypeRef = HierarchicalView.asRelationalToHierarchical(
				"(Réf) Famille -> Table -> Variable -> Type",
				Arrays.asList("id_famille", "nom_table_metier", "variable_metier", "type_variable_metier"),
				relationalViewRef);
		/*
		 * Récupérer dans le méta-modèle de la base les tables métiers correspondant à
		 * la famille chargée
		 */
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append(
				"SELECT lower(id_famille), lower(table_schema||'.'||table_name) nom_table_metier, lower(column_name) nom_variable_metier");

		// les types dans postgres sont horribles :(
		// udt_name : float8 = float, int8=bigint, int4=int
		// data_type : double precision = float, integer=int
		requete.append(
				", case when lower(data_type)='array' then replace(replace(replace(ltrim(udt_name,'_'),'int4','int'),'int8','bigint'),'float8','float')||'[]' ");
		requete.append(
				" else replace(replace(lower(data_type),'double precision','float'),'integer','int') end type_variable_metier ");
		requete.append("\n FROM information_schema.columns, " + ViewEnum.IHM_FAMILLE.getFullName());
		requete.append("\n WHERE table_schema='"
				+ ManipString.substringBeforeFirst(TableNaming.dbEnv(envExecution), ".").toLowerCase() + "' ");
		requete.append("\n and table_name LIKE '"
				+ ManipString.substringAfterFirst(TableNaming.dbEnv(envExecution), ".").toLowerCase()
				+ "mapping\\_%' ");
		requete.append("\n and table_name LIKE '"
				+ ManipString.substringAfterFirst(TableNaming.dbEnv(envExecution), ".").toLowerCase()
				+ "mapping\\_'||lower(id_famille)||'\\_%';");

		List<List<String>> relationalView = Format
				.patch(UtilitaireDao.get(0).executeRequestWithoutMetadata(connexion, requete));

		HierarchicalView familleToTableToVariableToType = HierarchicalView.asRelationalToHierarchical(
				"(Phy) Famille -> Table -> Variable -> Type",
				Arrays.asList("id_famille", "nom_table_metier", "variable_metier", "type_variable_metier"),
				relationalView);
		StringBuilder requeteMAJSchema = new StringBuilder();

		/*
		 * AJOUT/MODIFICATION DES COLONNES DE REFERENCE
		 */
		for (HierarchicalView famille : familleToTableToVariableToTypeRef.children()) {
			/**
			 * Pour chaque table de référence
			 */
			for (HierarchicalView table : famille.children()) {
				/**
				 * Est-ce que la table existe physiquement ?
				 */
				if (familleToTableToVariableToType.hasPath(famille, table)) {
					/**
					 * Pour chaque variable de référence
					 */
					for (HierarchicalView variable : table.children()) {
						/*
						 * Si la variable*type n'existe pas
						 */
						if (!familleToTableToVariableToType.hasPath(famille, table, variable,
								variable.getUniqueChild())) {

							// BUG POSTGRES : pb drop et add column : recréer la table sinon ca peut excéder
							// la limite postgres de 1500
							requeteMAJSchema.append("DROP TABLE IF EXISTS " + table.getLocalRoot() + "_IMG ;");
							requeteMAJSchema.append("CREATE TABLE " + table.getLocalRoot() + "_IMG "
									+ FormatSQL.WITH_NO_VACUUM + " AS SELECT * FROM " + table.getLocalRoot() + ";");
							requeteMAJSchema.append("DROP TABLE IF EXISTS " + table.getLocalRoot() + " ;");
							requeteMAJSchema.append("ALTER TABLE " + table.getLocalRoot() + "_IMG RENAME TO "
									+ ManipString.substringAfterFirst(table.getLocalRoot(), ".") + ";");

							/*
							 * Si la variable existe
							 */
							if (familleToTableToVariableToType.hasPath(famille, table, variable)) {
								/*
								 * Drop de la variable
								 */
								requeteMAJSchema.append("ALTER TABLE " + table.getLocalRoot() + " DROP COLUMN "
										+ variable.getLocalRoot() + ";");
							}
							/*
							 * Ajout de la variable
							 */
							requeteMAJSchema.append("ALTER TABLE " + table.getLocalRoot() + " ADD COLUMN "
									+ variable.getLocalRoot() + " " + variable.getUniqueChild().getLocalRoot() + " ");
							if (variable.getUniqueChild().getLocalRoot().equals(TypeEnum.TEXT.getTypeName())) {
								requeteMAJSchema.append(" collate \"C\" ");
							}
							requeteMAJSchema.append(";");

						}
					}
				} else {
					AttributeValue[] attr = new AttributeValue[table.children().size()];
					int i = 0;
					for (HierarchicalView variable : table.children()) {
						attr[i++] = new AttributeValue(variable.getLocalRoot(),
								variable.getUniqueChild().getLocalRoot());
					}
					requeteMAJSchema.append("CREATE TABLE " + table.getLocalRoot() + " (");
					for (int j = 0; j < attr.length; j++) {
						if (j > 0) {
							requeteMAJSchema.append(", ");
						}
						requeteMAJSchema.append(attr[j].getFirst() + " " + attr[j].getSecond());
						if (attr[j].getSecond().equals(TypeEnum.TEXT.getTypeName())) {
							requeteMAJSchema.append(" collate \"C\" ");
						}
					}
					requeteMAJSchema.append(") " + FormatSQL.WITH_NO_VACUUM + ";\n");
				}

			}
		}
		/*
		 * SUPPRESSION DES COLONNES QUI NE SONT PAS CENSEES EXISTER
		 */
		for (HierarchicalView famille : familleToTableToVariableToType.children()) {
			/**
			 * Pour chaque table physique
			 */
			for (HierarchicalView table : familleToTableToVariableToType.get(famille).children()) {
				/**
				 * Est-ce que la table devrait exister ?
				 */
				if (!familleToTableToVariableToTypeRef.hasPath(famille, table)) {
					requeteMAJSchema.append("DROP TABLE IF EXISTS " + table.getLocalRoot() + ";\n");
				} else {
					/**
					 * Pour chaque variable de cette table
					 */
					for (HierarchicalView variable : table.children()) {
						/**
						 * Est-ce que la variable devrait exister ?
						 */
						if (!familleToTableToVariableToTypeRef.hasPath(famille, table, variable)) {
							requeteMAJSchema.append("ALTER TABLE " + table.getLocalRoot() + " DROP COLUMN "
									+ variable.getLocalRoot() + ";\n");
						}
					}
				}
			}
		}
		UtilitaireDao.get(0).executeBlock(connexion, requeteMAJSchema);
	}
	
}