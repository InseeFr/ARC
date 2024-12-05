package fr.insee.arc.core.service.p0initialisation.metadata.dao;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.SchemaEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementTableParametre;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p0initialisation.metadata.ApplyExpressionRulesOperation;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.dataobjects.TypeEnum;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.security.SqlInjectionChecked;
import fr.insee.arc.utils.structure.AttributeValue;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.structure.tree.HierarchicalView;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

public class SynchronizeRulesAndMetadataDao {

	private static final Logger LOGGER = LogManager.getLogger(SynchronizeRulesAndMetadataDao.class);

	public SynchronizeRulesAndMetadataDao(Sandbox sandbox) {
		super();
		this.sandbox = sandbox;
	}

	private Sandbox sandbox;

	/**
	 * Copy the table containing user rules to the sandbox so they will be used by
	 * the sandbox process
	 * 
	 * @param coordinatorConnexion
	 * @param anParametersEnvironment
	 * @param anExecutionEnvironment
	 * @throws ArcException
	 */
	@SqlInjectionChecked(requiredAsSafe = "anExecutionEnvironment")
	public void copyRulesTablesToExecution() throws ArcException {
		LoggerHelper.info(LOGGER, "copyTablesToExecution");

		Connection coordinatorConnexion = sandbox.getConnection();
		String anExecutionEnvironment = sandbox.getSchema();

		try {
			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
			ArcPreparedStatementBuilder condition = new ArcPreparedStatementBuilder();

			
			TraitementTableParametre[] r = TraitementTableParametre.values();
			String modaliteEtat = anExecutionEnvironment.replace("_", ".");
			for (int i = 0; i < r.length; i++) {
				// on créé une table image de la table venant de l'ihm
				// (environnement de parametre)
				TraitementTableParametre parameterTable = r[i];
				String tableImage = FormatSQL
						.temporaryTableName(parameterTable.getTablenameInSandbox().getFullName(anExecutionEnvironment));

				// recopie partielle (en fonction de l'environnement
				// d'exécution)
				// pour les tables JEUDEREGLE, CONTROLE_REGLE et MAPPING_REGLE
				condition.setLength(0);
				if (parameterTable == TraitementTableParametre.NORME) {
					condition.append(" WHERE etat='1'");
					condition.append(" AND EXISTS (select 1 from "+ViewEnum.IHM_CALENDRIER.getFullName()+" b ");
					condition.append(" WHERE a.id_norme=b.id_norme ");
					condition.append(" AND EXISTS (SELECT 1 FROM "+ ViewEnum.IHM_JEUDEREGLE.getFullName()+" c WHERE b.id_norme=c.id_norme AND b.validite_inf=c.validite_inf AND b.validite_sup=c.validite_sup AND c.etat=lower("+ condition.quoteText(modaliteEtat) +"))");
					condition.append(" ) ");
				} else if (parameterTable == TraitementTableParametre.CALENDRIER) {
					condition.append(" WHERE etat='1' ");
					condition.append(" and exists (select 1 from " + ViewEnum.IHM_NORME.getFullName()
							+ " b where a.id_norme=b.id_norme and b.etat='1')");
					condition.append(" AND EXISTS (SELECT 1 FROM "+ ViewEnum.IHM_JEUDEREGLE.getFullName()+" b WHERE a.id_norme=b.id_norme AND a.validite_inf=b.validite_inf AND a.validite_sup=b.validite_sup AND b.etat=lower("+ condition.quoteText(modaliteEtat) +"))");
				} else if (parameterTable == TraitementTableParametre.JEUDEREGLE) {
					condition.append(" WHERE etat=lower(" + condition.quoteText(modaliteEtat) + ")");
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
							+ " b where a.id_norme=b.id_norme and a.periodicite=b.periodicite and a.validite_inf=b.validite_inf and a.validite_sup=b.validite_sup AND a.version=b.version and b.etat=lower("
							+ condition.quoteText(modaliteEtat) + "))");
				}
				requete.append(FormatSQL.dropTable(tableImage));

				requete.append("CREATE TABLE " + tableImage + " " + FormatSQL.WITH_NO_VACUUM + " AS SELECT a.* FROM "
						+ r[i].getTablenameInMetadata().getFullName() + " AS a " + condition + ";\n");

				requete.append(FormatSQL
						.dropTable(parameterTable.getTablenameInSandbox().getFullName(anExecutionEnvironment)));
				requete.append("ALTER TABLE " + tableImage + " rename to "
						+ ManipString.substringAfterLast(parameterTable.getTablenameInSandbox().getTableName(), ".")
						+ "; \n");
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

			List<String> requetesDeSuppressionTablesNmcl = new GenericBean(
					UtilitaireDao.get(0).executeRequest(coordinatorConnexion, requeteSelectDrop)).mapContent()
					.get("requete_drop");

			if (requetesDeSuppressionTablesNmcl != null) {
				for (String requeteDeSuppression : requetesDeSuppressionTablesNmcl) {
					requete.append("\n ").append(requeteDeSuppression);
				}
			}

			// 2.Préparation des requêtes de création des tables
			List<String> requetesDeCreationTablesNmcl = new GenericBean(UtilitaireDao.get(0)
					.executeRequest(coordinatorConnexion, new ArcPreparedStatementBuilder(
							"select tablename from pg_tables where (tablename like 'nmcl\\_%' OR tablename like 'ext\\_%') and schemaname='arc'")))
					.mapContent().get("tablename");

			if (requetesDeCreationTablesNmcl != null) {
				for (String tableName : requetesDeCreationTablesNmcl) {
					requete.append("\n CREATE TABLE " + ViewEnum.getFullName(anExecutionEnvironment, tableName) + " "
							+ FormatSQL.WITH_NO_VACUUM + " AS SELECT * FROM "
							+ ViewEnum.getFullName(SchemaEnum.ARC_METADATA.getSchemaName(), tableName) + ";");
				}
			}

			// 3.Execution du script Sql de suppression/création
			UtilitaireDao.get(0).executeBlock(coordinatorConnexion, requete);

		} catch (ArcException e) {
			LoggerHelper.trace(LOGGER,
					"Problème lors de la copie des tables vers l'environnement : " + anExecutionEnvironment);
			LoggerHelper.error(LOGGER, "Error in ApiInitialisation.copyRulesTablesToExecution");
			throw e;
		}
	}

	/**
	 * Créer ou detruire les colonnes ou les tables métiers en comparant ce qu'il y
	 * a en base à ce qu'il y a de déclaré dans la table des familles de norme
	 *
	 * @param coordinatorOrExecutorConnexion
	 * @throws ArcException
	 */
	@SqlInjectionChecked
	public static void mettreAJourSchemaTableMetier(Connection coordinatorOrExecutorConnexion, String envExecution)
			throws ArcException {
		LoggerHelper.info(LOGGER, "mettreAJourSchemaTableMetier");

		HierarchicalView familleToTableToVariableToTypeRef = retrieveDeclaredVariableMetier(
				coordinatorOrExecutorConnexion, envExecution);
		HierarchicalView familleToTableToVariableToType = retrieveEffectiveVariableMetier(
				coordinatorOrExecutorConnexion, envExecution);

		ArcPreparedStatementBuilder requeteMAJSchema = new ArcPreparedStatementBuilder();

		addAndUpdateColumnsOfModelTable(requeteMAJSchema, familleToTableToVariableToTypeRef,
				familleToTableToVariableToType);
		deleteColumnsOfModelTable(requeteMAJSchema, familleToTableToVariableToTypeRef, familleToTableToVariableToType);

		UtilitaireDao.get(0).executeBlock(coordinatorOrExecutorConnexion, requeteMAJSchema);
	}

	/**
	 * delete the columns of business model table that shouldn't exists according to
	 * what user have declared in ihm_mod_variable_metier
	 * 
	 * @param requeteMAJSchema
	 * @param familleToTableToVariableToTypeRef
	 * @param familleToTableToVariableToType
	 */
	private static void deleteColumnsOfModelTable(ArcPreparedStatementBuilder requeteMAJSchema,
			HierarchicalView familleToTableToVariableToTypeRef, HierarchicalView familleToTableToVariableToType) {
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
	}

	/**
	 * add or update the columns of business model table according to what user have
	 * declared in ihm_mod_variable_metier
	 * 
	 * @param requeteMAJSchema
	 * @param familleToTableToVariableToTypeRef
	 * @param familleToTableToVariableToType
	 */
	@SqlInjectionChecked
	private static void addAndUpdateColumnsOfModelTable(ArcPreparedStatementBuilder requeteMAJSchema,
			HierarchicalView familleToTableToVariableToTypeRef, HierarchicalView familleToTableToVariableToType) {
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
	}

	/**
	 * Retrieve the real columns, names of business model table found in database
	 * 
	 * @param coordinatorOrExecutorConnexion
	 * @param envExecution
	 * @return
	 * @throws ArcException
	 */
	@SqlInjectionChecked
	private static HierarchicalView retrieveEffectiveVariableMetier(Connection coordinatorOrExecutorConnexion,
			String envExecution) throws ArcException {
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
		requete.append("\n WHERE table_schema=").appendText(envExecution);
		requete.append("\n and table_name LIKE 'mapping\\_%' ");
		requete.append("\n and table_name LIKE 'mapping\\_'||lower(id_famille)||'\\_%' ");
		requete.append("\n ;");

		List<List<String>> relationalView = UtilitaireDao.get(0)
				.executeRequestWithoutMetadata(coordinatorOrExecutorConnexion, requete);

		return HierarchicalView.asRelationalToHierarchical("(Phy) Famille -> Table -> Variable -> Type",
				Arrays.asList("id_famille", "nom_table_metier", "variable_metier", "type_variable_metier"),
				relationalView);
	}

	/**
	 * Retrieve the business model table schema (famille, nom_de_table, variable,
	 * ...) declared by user
	 * 
	 * @param coordinatorOrExecutorConnexion
	 * @param envExecution
	 * @return
	 * @throws ArcException
	 */
	@SqlInjectionChecked
	private static HierarchicalView retrieveDeclaredVariableMetier(Connection coordinatorOrExecutorConnexion,
			String envExecution) throws ArcException {
		/*
		 * Récupérer la table qui mappe : famille / table métier / variable métier et
		 * type de la variable
		 */
		ArcPreparedStatementBuilder requeteRef = new ArcPreparedStatementBuilder();
		requeteRef.append("SELECT lower(id_famille), lower(").appendText(envExecution)
				.append("||'.'||nom_table_metier), lower(nom_variable_metier), lower(type_variable_metier) FROM "
						+ ViewEnum.IHM_MOD_VARIABLE_METIER.getFullName());

		List<List<String>> relationalViewRef = UtilitaireDao.get(0)
				.executeRequestWithoutMetadata(coordinatorOrExecutorConnexion, requeteRef);

		return HierarchicalView.asRelationalToHierarchical("(Réf) Famille -> Table -> Variable -> Type",
				Arrays.asList("id_famille", "nom_table_metier", "variable_metier", "type_variable_metier"),
				relationalViewRef);
	}

	/**
	 * Query to return data from target table if emptyTable is true, only metadata
	 * will be return without data
	 * 
	 * @param coordinatorConnexion
	 * @param table
	 * @return
	 * @throws ArcException
	 */
	@SqlInjectionChecked(requiredAsSafe = "table")
	private static GenericBean execQuerySelectDataFrom(Connection coordinatorConnexion, String table,
			boolean emptyTable) throws ArcException {
		return new GenericBean(UtilitaireDao.get(0).executeRequest(coordinatorConnexion,
				new ArcPreparedStatementBuilder("SELECT * FROM " + table + " " + (emptyTable ? "LIMIT 0" : ""))));
	}

	/**
	 * Query to return data and metadata from target table
	 * 
	 * @param coordinatorConnexion
	 * @param table
	 * @return
	 * @throws ArcException
	 */
	public static GenericBean execQuerySelectDataFrom(Connection coordinatorConnexion, String table)
			throws ArcException {
		return execQuerySelectDataFrom(coordinatorConnexion, table, false);
	}

	public static GenericBean execQuerySelectMetaDataOnlyFrom(Connection coordinatorConnexion, String table)
			throws ArcException {
		return execQuerySelectDataFrom(coordinatorConnexion, table, true);
	}

	public void execQueryApplyExpressionsToControl(ApplyExpressionRulesOperation expressionService, JeuDeRegle ruleSet,
			GenericBean expressions) throws ArcException {
		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(),
				expressionService.applyExpressionsToControl(ruleSet, expressions, sandbox.getSchema()));
	}

	public void execQueryApplyExpressionsToMapping(ApplyExpressionRulesOperation expressionService, JeuDeRegle ruleSet,
			GenericBean expressions) throws ArcException {
		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(),
				expressionService.applyExpressionsToMapping(ruleSet, expressions, sandbox.getSchema()));
	}

}
