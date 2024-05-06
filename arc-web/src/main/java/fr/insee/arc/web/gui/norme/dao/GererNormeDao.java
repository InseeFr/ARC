package fr.insee.arc.web.gui.norme.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.all.model.GuiModules;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectHelperDao;
import fr.insee.arc.web.gui.all.util.VObjectService;

@Component
public class GererNormeDao extends VObjectHelperDao {

	private static final Logger LOGGER = LogManager.getLogger(GererNormeDao.class);

	/**
	 * dao call to build norm vobject
	 * 
	 * @param viewNorme
	 */
	public void initializeViewNorme(VObject viewNorme) {

		ViewEnum dataModelNorm = ViewEnum.IHM_NORME;

		Map<String, String> defaultInputFields = new HashMap<>();

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelNorm));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelNorm));
		query.append(SQL.ORDER_BY);
		query.append(ColumnEnum.ID_NORME);

		// Initialize the vobject
		vObjectService.initialize(viewNorme, query, dataObjectService.getView(dataModelNorm), defaultInputFields);
	}

	/**
	 * dao call to build calendar vobject
	 * 
	 * @param viewCalendar
	 */
	public void initializeViewCalendar(VObject viewCalendar) {

		ViewEnum dataModelCalendar = ViewEnum.IHM_CALENDRIER;

		// requete de la vue
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelCalendar));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelCalendar));
		query.append(SQL.WHERE);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_NORME));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.PERIODICITE));

		// build the default value when adding a record
		Map<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_NORME,
				ColumnEnum.PERIODICITE);

		// check constraints on calendar after insert or update
		ArcPreparedStatementBuilder queryCheckConstraint = new ArcPreparedStatementBuilder();
		queryCheckConstraint.append(SQL.SELECT).append("arc.fn_check_calendrier()").append(SQL.END_QUERY);

		viewCalendar.setAfterInsertQuery(queryCheckConstraint);
		viewCalendar.setAfterUpdateQuery(queryCheckConstraint);

		// Initialize the vobject
		vObjectService.initialize(viewCalendar, query, dataObjectService.getView(dataModelCalendar),
				defaultInputFields);

	}

	/**
	 * dao call to build ruleset vobject
	 * 
	 * @param viewRulesSet
	 */
	public void initializeViewRulesSet(VObject viewRulesSet) {

		ViewEnum dataModelRulesSet = ViewEnum.IHM_JEUDEREGLE;

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelRulesSet));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelRulesSet));
		query.append(SQL.WHERE);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_NORME));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.PERIODICITE));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_INF));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_SUP));

		// build the default value when adding a record
		Map<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_NORME,
				ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP);

		// check constraints on rulesets after insert or update
		ArcPreparedStatementBuilder queryCheckConstraint = new ArcPreparedStatementBuilder();
		queryCheckConstraint.append(SQL.SELECT).append("arc.fn_check_jeuderegle()").append(SQL.END_QUERY);

		viewRulesSet.setAfterInsertQuery(queryCheckConstraint);
		viewRulesSet.setAfterUpdateQuery(queryCheckConstraint);

		vObjectService.initialize(viewRulesSet, query, dataObjectService.getView(dataModelRulesSet),
				defaultInputFields);
	}

	/**
	 * dao call to build module menu vobject
	 * 
	 * @param viewModules
	 * @param functionGetModuleName
	 */
	public void initializeViewModules(VObject viewModules, Function<GuiModules, String> functionGetModuleName) {

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		boolean union = false;
		int i = 0;
		for (GuiModules module : GuiModules.values()) {
			if (union) {
				query.append(SQL.UNION_ALL);
			} else {
				union = true;
			}
			query.append(moduleQuery(i++, module, functionGetModuleName));
		}

		vObjectService.initialize(viewModules, query, null, new HashMap<>());
	}

	/**
	 * build query for module module are set in record each module record contains -
	 * a number (order in guimodules) that indexes the module - a name obtained by
	 * applying functionGetModuleName
	 * 
	 * @param moduleIndex
	 * @param module
	 * @param functionGetModuleName
	 * @return
	 */
	private ArcPreparedStatementBuilder moduleQuery(int moduleIndex, GuiModules module,
			Function<GuiModules, String> functionGetModuleName) {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.append(SQL.SELECT);
		query.append(moduleIndex);
		query.append(SQL.AS);
		query.append(ColumnEnum.MODULE_ORDER);

		query.append(SQL.COMMA);

		query.append(query.quoteText(functionGetModuleName.apply(module)));
		query.append(SQL.AS);
		query.append(ColumnEnum.MODULE_NAME);

		return query;
	}

	/**
	 * Query to get load rules view
	 * 
	 * @param viewChargement
	 */
	public void initializeViewChargement(VObject viewChargement) {

		ViewEnum dataModelChargement = ViewEnum.IHM_CHARGEMENT_REGLE;

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelChargement));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelChargement));
		query.append(SQL.WHERE);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_NORME));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.PERIODICITE));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_INF));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_SUP));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VERSION));

		// build the default value when adding a record
		Map<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_NORME,
				ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION);

		vObjectService.initialize(viewChargement, query, dataObjectService.getView(dataModelChargement),
				defaultInputFields);
	}

	/**
	 * Query to get normage rules view
	 * 
	 * @param viewNormage
	 */
	public void initializeNormage(VObject viewNormage) {

		ViewEnum dataModelNormage = ViewEnum.IHM_NORMAGE_REGLE;

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

		query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelNormage));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelNormage));
		query.append(SQL.WHERE);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_NORME));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.PERIODICITE));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_INF));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_SUP));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VERSION));

		// build the default value when adding a record
		Map<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_NORME,
				ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION);

		vObjectService.initialize(viewNormage, query, dataObjectService.getView(dataModelNormage), defaultInputFields);
	}
	
	/**
	 * Query to get control rules view
	 * 
	 * @param viewControle
	 */
	public void initializeControle(VObject viewControle) {

		ViewEnum dataModelControle = ViewEnum.IHM_CONTROLE_REGLE;

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

        query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelControle));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelControle));
		query.append(SQL.WHERE);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_NORME));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.PERIODICITE));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_INF));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_SUP));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VERSION));

		// build the default value when adding a record
		Map<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_NORME,
				ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION);

		vObjectService.initialize(viewControle, query, dataObjectService.getView(dataModelControle), defaultInputFields);
	}
	
	/**
	 * Query to get mapping rules view
	 * 
	 * @param viewMapping
	 */
	public void initializeMapping(VObject viewMapping) {

		ViewEnum dataModelMapping = ViewEnum.IHM_MAPPING_REGLE;

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
        query.append("SELECT mapping.id_regle, mapping.id_norme, mapping.validite_inf, mapping.validite_sup, mapping.version, mapping.periodicite, mapping.variable_sortie, mapping.expr_regle_col, mapping.commentaire, variables.type_variable_metier type_sortie, variables.nom_table_metier nom_table_metier ");
        query.append("\n  FROM arc.ihm_mapping_regle mapping INNER JOIN arc.ihm_norme norme");
        query.append("\n  ON norme.id_norme = mapping.id_norme AND norme.periodicite = mapping.periodicite");
        query.append("\n  LEFT JOIN (SELECT id_famille, nom_variable_metier, type_variable_metier, string_agg(nom_table_metier,',') as nom_table_metier  FROM arc.ihm_mod_variable_metier group by id_famille, nom_variable_metier, type_variable_metier) variables");
        query.append("\n  ON variables.id_famille = norme.id_famille AND variables.nom_variable_metier = mapping.variable_sortie");
		query.append(SQL.WHERE + "mapping.");
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_NORME));
		query.append(SQL.AND + "mapping.");
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.PERIODICITE));
		query.append(SQL.AND + "mapping.");
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_INF));
		query.append(SQL.AND + "mapping.");
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_SUP));
		query.append(SQL.AND + "mapping.");
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VERSION));

		// build the default value when adding a record
		Map<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_NORME,
				ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION);

		vObjectService.initialize(viewMapping, query, dataObjectService.getView(dataModelMapping), defaultInputFields);
	}
	
	/**
	 * Query to get expressions view
	 * 
	 * @param viewExpression
	 */
	public void initializeExpression(VObject viewExpression) {

		ViewEnum dataModelExpression = ViewEnum.IHM_EXPRESSION;

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

        query.append(SQL.SELECT);
		query.append(query.sqlListeOfColumnsFromModel(dataModelExpression));
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelExpression));
		query.append(SQL.WHERE);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.ID_NORME));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.PERIODICITE));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_INF));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VALIDITE_SUP));
		query.append(SQL.AND);
		query.append(sqlEqualWithFirstSelectedRecord(ColumnEnum.VERSION));

		// build the default value when adding a record
		Map<String, String> defaultInputFields = buildDefaultInputFieldsWithFirstSelectedRecord(ColumnEnum.ID_NORME,
				ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION);

		vObjectService.initialize(viewExpression, query, dataObjectService.getView(dataModelExpression), defaultInputFields);
	}
	
	/**
	 * Query to get ruleset view for copy
	 * 
	 * @param viewJeuxDeReglesCopie
	 */
	public void initializeJeuxDeReglesCopie(VObject viewJeuxDeReglesCopie) {

		ViewEnum dataModelJeuxDeReglesCopie = ViewEnum.IHM_JEUDEREGLE;

		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();

        query.append(SQL.SELECT);
		query.append("id_norme, periodicite, validite_inf, validite_sup, version, etat");
		query.append(SQL.FROM);
		query.append(dataObjectService.getView(dataModelJeuxDeReglesCopie));

		// build the default value when adding a record
		Map<String, String> defaultInputFields = new HashMap<>();

		vObjectService.initialize(viewJeuxDeReglesCopie, query, dataObjectService.getView(dataModelJeuxDeReglesCopie), defaultInputFields);
	}

	/**
	 * generate a blank rules set for mapping based on variables declared in data
	 * model
	 * 
	 * @param viewNorme
	 * @param viewJeuxDeRegles
	 * @param viewMapping
	 * @throws ArcException
	 */
	public void execQueryPreGenererRegleMapping(VObject viewNorme, VObject viewJeuxDeRegles, VObject viewMapping)
			throws ArcException {
		// List hard coded to be sure of the order in the select
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("INSERT INTO " + viewMapping.getTable()).append(
				"  (id_regle, id_norme, validite_inf, validite_sup,  version , periodicite, variable_sortie, expr_regle_col, commentaire) ")
				.append("  SELECT coalesce((SELECT max(id_regle) FROM " + viewMapping.getTable()
						+ "),0)+row_number() over () ,")
				.append(requete.quoteText(
						viewJeuxDeRegles.mapContentSelected().get(ColumnEnum.ID_NORME.getColumnName()).get(0)) + ", ")
				.append(requete.quoteText(
						viewJeuxDeRegles.mapContentSelected().get(ColumnEnum.VALIDITE_INF.getColumnName()).get(0))
						+ "::date, ")
				.append(requete.quoteText(
						viewJeuxDeRegles.mapContentSelected().get(ColumnEnum.VALIDITE_SUP.getColumnName()).get(0))
						+ "::date, ")
				.append(requete.quoteText(
						viewJeuxDeRegles.mapContentSelected().get(ColumnEnum.VERSION.getColumnName()).get(0)) + ", ")
				.append(requete.quoteText(
						viewJeuxDeRegles.mapContentSelected().get(ColumnEnum.PERIODICITE.getColumnName()).get(0))
						+ ", ")
				.append("  liste_colonne.nom_variable_metier,").append("  null,").append("  null").append("  FROM (")
				.append(ApiService.listeColonneTableMetierSelonFamilleNorme(
						viewNorme.mapContentSelected().get(ColumnEnum.ID_FAMILLE.getColumnName()).get(0)))
				.append(") liste_colonne");

		UtilitaireDao.get(0).executeRequest(null, requete);
	}

	/**
	 * copy rules from a target rules set to the current ruleset
	 * 
	 * @param viewJeuxDeRegles
	 * @param viewJeuxDeReglesCopie
	 * @param selectedTableOfRegles
	 * @throws ArcException
	 */
	public void execQueryCopieJeuxDeRegles(VObject viewJeuxDeRegles, VObject viewJeuxDeReglesCopie,
			String selectedTableOfRegles) throws ArcException {
		// le jeu de regle à copier
		Map<String, List<String>> selectionOut = viewJeuxDeRegles.mapContentSelected();
		// le nouveau jeu de regle
		Map<String, List<String>> selectionIn = viewJeuxDeReglesCopie.mapContentSelected();

		Map<String, String> type = viewJeuxDeReglesCopie.mapHeadersType();

		// columns found in all rules tables
		String inCommonColumns = new StringBuilder().append(ColumnEnum.ID_NORME.getColumnName())
				.append("," + ColumnEnum.PERIODICITE.getColumnName())
				.append("," + ColumnEnum.VALIDITE_INF.getColumnName())
				.append("," + ColumnEnum.VALIDITE_SUP.getColumnName()).append("," + ColumnEnum.VERSION.getColumnName())
				.toString();

		// specific columns = column of the table minus common tables minus id_regle
		// (rules generated id)
		ArcPreparedStatementBuilder getTableSpecificColumns = new ArcPreparedStatementBuilder();
		getTableSpecificColumns.append("\n SELECT string_agg(column_name,',') ");
		getTableSpecificColumns.append("\n FROM information_schema.columns c ");
		getTableSpecificColumns.append(
				"\n WHERE table_schema||'.'||table_name =" + getTableSpecificColumns.quoteText(selectedTableOfRegles));
		getTableSpecificColumns.append("\n AND column_name NOT IN ");
		getTableSpecificColumns.append(
				"\n ('" + inCommonColumns.replace(",", "','") + "','" + ColumnEnum.ID_REGLE.getColumnName() + "') ");

		String specificColumns = UtilitaireDao.get(0).getString(null, getTableSpecificColumns);

		// Build the copy query
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();

		requete.append("INSERT INTO " + selectedTableOfRegles + " ");
		requete.append("(");
		requete.append(inCommonColumns + "," + specificColumns);
		requete.append(")");

		requete.append("\n SELECT ");
		requete.append(String.join(",", requete.quoteText(selectionOut.get(ColumnEnum.ID_NORME.getColumnName()).get(0)),
				requete.quoteText(selectionOut.get(ColumnEnum.PERIODICITE.getColumnName()).get(0)),
				requete.quoteText(selectionOut.get(ColumnEnum.VALIDITE_INF.getColumnName()).get(0)) + "::date ",
				requete.quoteText(selectionOut.get(ColumnEnum.VALIDITE_SUP.getColumnName()).get(0)) + "::date ",
				requete.quoteText(selectionOut.get(ColumnEnum.VERSION.getColumnName()).get(0))));
		requete.append("," + specificColumns);

		requete.append(" FROM " + selectedTableOfRegles + "  ");

		requete.append(" WHERE ");

		requete.append(String.join(" AND ", //
				// condition about id_norm
				ColumnEnum.ID_NORME.getColumnName() + requete.sqlEqual(
						selectionIn.get(ColumnEnum.ID_NORME.getColumnName()).get(0),
						type.get(ColumnEnum.ID_NORME.getColumnName())),
				ColumnEnum.PERIODICITE.getColumnName()
						// condition about PERIODICITE
						+ requete.sqlEqual(selectionIn.get(ColumnEnum.PERIODICITE.getColumnName()).get(0),
								type.get(ColumnEnum.PERIODICITE.getColumnName())),
				ColumnEnum.VALIDITE_INF.getColumnName()
						// condition about VALIDITE_INF
						+ requete.sqlEqual(selectionIn.get(ColumnEnum.VALIDITE_INF.getColumnName()).get(0),
								type.get(ColumnEnum.VALIDITE_INF.getColumnName())),
				ColumnEnum.VALIDITE_SUP.getColumnName()
						// condition about VALIDITE_SUP
						+ requete.sqlEqual(selectionIn.get(ColumnEnum.VALIDITE_SUP.getColumnName()).get(0),
								type.get(ColumnEnum.VALIDITE_SUP.getColumnName())),
				ColumnEnum.VERSION.getColumnName()
						// condition about VERSION
						+ requete.sqlEqual(selectionIn.get(ColumnEnum.VERSION.getColumnName()).get(0),
								type.get(ColumnEnum.VERSION.getColumnName()))

		));
		requete.append(" order by " + ColumnEnum.ID_REGLE.getColumnName() + " ;");

		// delete the current rules before the copy
		emptyRuleTable(viewJeuxDeRegles, selectedTableOfRegles);
		UtilitaireDao.get(0).executeRequest(null, requete);
	}

	/**
	 * Empty all the rules of a norm module
	 * 
	 * @param viewRulesSet
	 * @param table
	 * @throws ArcException
	 */
	public void emptyRuleTable(VObject viewRulesSet, String table) throws ArcException {

		Map<String, List<String>> selection = viewRulesSet.mapContentSelected();
		Map<String, String> type = viewRulesSet.mapHeadersType();
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("DELETE FROM " + table);
		requete.append(" WHERE id_norme" + requete.sqlEqual(selection.get("id_norme").get(0), type.get("id_norme")));
		requete.append(
				" AND periodicite" + requete.sqlEqual(selection.get("periodicite").get(0), type.get("periodicite")));
		requete.append(
				" AND validite_inf" + requete.sqlEqual(selection.get("validite_inf").get(0), type.get("validite_inf")));
		requete.append(
				" AND validite_sup" + requete.sqlEqual(selection.get("validite_sup").get(0), type.get("validite_sup")));
		requete.append(" AND version" + requete.sqlEqual(selection.get("version").get(0), type.get("version")));
		requete.append(" ;");

		UtilitaireDao.get(0).executeRequest(null, requete);

	}

	/**
	 * Upload module rules for a rule set of a norm family. Upload for rules
	 * uses {@code uploadFileMapping} method.
	 * 
	 * @param vObjectToUpdate the vObject to update with file
	 * @param viewRulesSet    the ruleset vObject, used to get the selected ruleset
	 * @param theFileToUpload the file uploaded by the user
	 */
	public void uploadFileRule(VObject vObjectToUpdate, VObject viewRulesSet, MultipartFile theFileToUpload) {

		// Check if there is file
		if (theFileToUpload == null || theFileToUpload.isEmpty()) {
			// No file -> ko
			vObjectToUpdate.setMessage("normManagement.upload.noSelection");
		} else {
			// A file -> can process it
			LoggerHelper.debug(LOGGER, " filesUpload  : " + theFileToUpload);

			// before inserting in the final table, the rules will be inserted in a table to
			// test them
			String nomTableImage = FormatSQL.temporaryTableName(vObjectToUpdate.getTable() + "_img" + 0);

			try (BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(theFileToUpload.getInputStream(), StandardCharsets.UTF_8));
				CSVReader readerCSV = new CSVReader(bufferedReader, IConstanteCaractere.semicolon.charAt(0));	
					) {
				// Get headers and type
				GenericBean fileSchema= getHeaderFromFile(readerCSV);

				/*
				 * Création d'une table temporaire (qui ne peut pas être TEMPORARY)
				 */
				ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
				requete.append("\n DROP TABLE IF EXISTS " + nomTableImage + " cascade;");
				requete.append("\n CREATE TABLE " + nomTableImage + " AS SELECT "//
						+ Format.untokenize(fileSchema.getHeaders(), ", ") //
						+ "\n\t FROM " //
						+ vObjectToUpdate.getTable() //
						+ "\n\t WHERE false");

				UtilitaireDao.get(0).executeRequest(null, requete);

				// Importing the file in the database (COPY command)
				UtilitaireDao.get(0).importingWithReader(null, nomTableImage, bufferedReader, false,
						IConstanteCaractere.semicolon);

			} catch (Exception ex) {
				vObjectToUpdate.setMessage("normManagement.upload.error");
				vObjectToUpdate.setMessageArgs(ex.getMessage());
				LoggerHelper.error(LOGGER, ex, "uploadOutils()", "\n");
				// After the exception, the methode cant go further, so the better thing to do
				// is to quit it
				return;
			}
			LoggerHelper.debug(LOGGER, "Insert file in the " + nomTableImage + " table");

			Map<String, List<String>> selection = viewRulesSet.mapContentSelected();

			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();

			requete.append("\n UPDATE " + nomTableImage + " SET ");
			requete.append("\n id_norme=" + requete.quoteText(selection.get("id_norme").get(0)));
			requete.append("\n, periodicite=" + requete.quoteText(selection.get("periodicite").get(0)));
			requete.append("\n, validite_inf=" + requete.quoteText(selection.get("validite_inf").get(0)) + "::date");
			requete.append("\n, validite_sup=" + requete.quoteText(selection.get("validite_sup").get(0)) + "::date");
			requete.append("\n, version=" + requete.quoteText(selection.get("version").get(0)));
			requete.append("\n ; ");

			requete.append("\n DELETE FROM " + vObjectToUpdate.getTable());
			requete.append("\n WHERE ");
			requete.append("\n id_norme=" + requete.quoteText(selection.get("id_norme").get(0)));
			requete.append("\n AND  periodicite=" + requete.quoteText(selection.get("periodicite").get(0)));
			requete.append("\n AND  validite_inf=" + requete.quoteText(selection.get("validite_inf").get(0)) + "::date");
			requete.append("\n AND  validite_sup=" + requete.quoteText(selection.get("validite_sup").get(0)) + "::date");
			requete.append("\n AND  version=" + requete.quoteText(selection.get("version").get(0)));
			requete.append("\n ; ");

			requete.append("\n INSERT INTO " + vObjectToUpdate.getTable() + " ");
			requete.append("\n SELECT * FROM " + nomTableImage + " ;");
			requete.append("\n DROP TABLE IF EXISTS " + nomTableImage + " cascade;");

			try {
				UtilitaireDao.get(0).executeRequest(null, requete);
			} catch (Exception ex) {
				vObjectToUpdate.setMessage("normManagement.upload.error");
				vObjectToUpdate.setMessageArgs(ex.getMessage());
				LoggerHelper.error(LOGGER, ex, "uploadOutils()");
			}
		}
	}

	/**
	 * Upload mapping rules for a rule set of a norm family. The upload method for
	 * mapping rules is different because it needs to ensure that mapping rules
	 * remain consistent with the norm family
	 * 
	 * @param viewMapping     the mapping vObject to update with file
	 * @param viewRulesSet    the ruleset vObject, used to get the selected ruleset
	 * @param viewNorme       the norme vObject, used to pregenerate mapping rules
	 * @param theFileToUpload the file uploaded by the user
	 */
	public void uploadFileMapping(VObject viewMapping, VObject viewRulesSet, VObject viewNorme,
			MultipartFile theFileToUpload) {

		// Check if there is file
		if (theFileToUpload == null || theFileToUpload.isEmpty()) {
			// No file -> ko
			viewMapping.setMessage("normManagement.upload.noSelection");
		} else {
			try {
				// before : create temporary table from uploaded file
				String nomTableImage = copyFileIntoTemporaryTable(viewMapping, theFileToUpload);
				// first step : empty the table
				emptyRuleTable(viewRulesSet, dataObjectService.getView(ViewEnum.IHM_MAPPING_REGLE));
				// second step : pregenerate rules
				execQueryPreGenererRegleMapping(viewNorme, viewRulesSet, viewMapping);
				// third step : add rules from the files
				copyTemporaryTableToRuleTable(viewMapping, viewRulesSet, nomTableImage);
			} catch (Exception e) {
				viewMapping.setMessage("normManagement.upload.error");
				viewMapping.setMessageArgs(e.getMessage());
				LoggerHelper.error(LOGGER, e, "uploadOutils()", "\n");
				if (e instanceof ArcException) {
					((ArcException) e).logFullException();
				}
			}
		}
	}

	/**
	 * Copies the mapping rules file given by the user into a temporary table.
	 * Exceptions that might be thrown because of the file given by the user
	 * are dealt here to avoid deleting and regenerating the rules in the
	 * database for nothing
	 * 
	 * @param viewMapping     the mapping vObject to update with file
	 * @param theFileToUpload the file uploaded by the user
	 * @return the name of the temporary table
	 * @throws IOException
	 * @throws ArcException
	 */
	private String copyFileIntoTemporaryTable(VObject viewMapping, MultipartFile theFileToUpload)
			throws IOException, ArcException {
		// A file -> can process it
		LoggerHelper.debug(LOGGER, " filesUpload  : " + theFileToUpload);

		// the file will be read into a temporary table
		String nomTableUpload = FormatSQL.temporaryTableName("fileUpload" + "_img" + 0);
		
		// schema provided by the file
		GenericBean fileSchema;

		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(theFileToUpload.getInputStream(), StandardCharsets.UTF_8));
			 CSVReader readerCSV = new CSVReader(bufferedReader, IConstanteCaractere.semicolon.charAt(0));
				)
		{
		fileSchema = getHeaderFromFile(readerCSV);
		
		// temporary table to read the file
		ArcPreparedStatementBuilder requeteUpload = new ArcPreparedStatementBuilder();
		requeteUpload.append("\n DROP TABLE IF EXISTS " + nomTableUpload + " cascade;");
		requeteUpload.append("\n CREATE TABLE " + nomTableUpload + " (");
		for (int i = 0; i < fileSchema.getHeaders().size(); i++) {
			requeteUpload.append((i == 0 ? "" : ",") + "\n " + fileSchema.getHeaders().get(i) + " " + fileSchema.getTypes().get(i));
		}
		requeteUpload.append("\n );");

		UtilitaireDao.get(0).executeRequest(null, requeteUpload);

		// Importing the file in the database (COPY command)
		UtilitaireDao.get(0).importingWithReader(null, nomTableUpload, bufferedReader, false,
				IConstanteCaractere.semicolon);
		}
		
		// before inserting in the final table, the rules will be inserted in a table to
		// test them
		String nomTableImage = FormatSQL.temporaryTableName(viewMapping.getTable() + "_img" + 0);

		List<String> listColumnsMapping = ColumnEnum
				.listColumnEnumByName(ViewEnum.IHM_MAPPING_REGLE.getColumns().keySet());

		/*
		 * Création d'une table temporaire (qui ne peut pas être TEMPORARY)
		 */
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("\n DROP TABLE IF EXISTS " + nomTableImage + " cascade;");
		requete.append("\n CREATE TABLE " + nomTableImage + " AS SELECT "//
				+ Format.untokenize(listColumnsMapping, ", ") //
				+ "\n\t FROM " //
				+ viewMapping.getTable() //
				+ "\n\t WHERE false");

		UtilitaireDao.get(0).executeRequest(null, requete);

		// Only keep columns from mapping regle table
		List<String> listHeadersToInsert = new ArrayList<>(fileSchema.getHeaders());
		listHeadersToInsert.retainAll(listColumnsMapping);

		// Insert into temporary table from upload table
		ArcPreparedStatementBuilder requeteInsert = new ArcPreparedStatementBuilder();
		requeteInsert
				.append("\n INSERT INTO " + nomTableImage + " (" + Format.untokenize(listHeadersToInsert, ", ") + ")");
		requeteInsert.append("\n SELECT "//
				+ Format.untokenize(listHeadersToInsert, ", ") //
				+ "\n\t FROM " //
				+ nomTableUpload + ";");
		requeteInsert.append("\n DROP TABLE IF EXISTS " + nomTableUpload + " cascade;");
		UtilitaireDao.get(0).executeRequest(null, requeteInsert);

		return nomTableImage;

	}

	/**
	 * Update the mapping rules with data from the temporary table, then drops
	 * the temporary table
	 * 
	 * @param viewMapping   the mapping vObject to update with file
	 * @param viewRulesSet  the ruleset vObject, used to get the selected ruleset
	 * @param nomTableImage the name of the temporary table
	 * @throws ArcException
	 */
	private void copyTemporaryTableToRuleTable(VObject viewMapping, VObject viewRulesSet, String nomTableImage)
			throws ArcException {
		LoggerHelper.debug(LOGGER, "Insert file in the " + nomTableImage + " table");

		Map<String, List<String>> selection = viewRulesSet.mapContentSelected();

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append("\n UPDATE " + nomTableImage + " SET ");
		requete.append("\n id_norme=" + requete.quoteText(selection.get("id_norme").get(0)));
		requete.append("\n, periodicite=" + requete.quoteText(selection.get("periodicite").get(0)));
		requete.append("\n, validite_inf=" + requete.quoteText(selection.get("validite_inf").get(0)) + "::date");
		requete.append("\n, validite_sup=" + requete.quoteText(selection.get("validite_sup").get(0)) + "::date");
		requete.append("\n, version=" + requete.quoteText(selection.get("version").get(0)));
		requete.append("\n ; ");

		requete.append("\n UPDATE " + viewMapping.getTable() + " m SET ");
		boolean isOtherItem = false;
		List<String> listColumnsMapping = ColumnEnum
				.listColumnEnumByName(ViewEnum.IHM_MAPPING_REGLE.getColumns().keySet());
		for (String col : listColumnsMapping) {
			requete.append("\n" + (isOtherItem ? ", " : " ") + col + "=f." + col);
			if (!isOtherItem)
				isOtherItem = true;
		}
		requete.append("\n FROM " + nomTableImage + " AS f ");
		requete.append("\n WHERE f.id_norme =m.id_norme");
		requete.append("\n AND f.periodicite =m.periodicite");
		requete.append("\n AND f.validite_inf =m.validite_inf");
		requete.append("\n AND f.validite_sup =m.validite_sup");
		requete.append("\n AND f.version =m.version");
		requete.append("\n AND f.variable_sortie =m.variable_sortie");
		requete.append(SQL.END_QUERY);

		requete.append("\n DROP TABLE IF EXISTS " + nomTableImage + " cascade;");

		UtilitaireDao.get(0).executeRequest(null, requete);
	}

	private static GenericBean getHeaderFromFile(CSVReader csvReader) throws IOException {	
		return new GenericBean(Arrays.asList(csvReader.readNext()), Arrays.asList(csvReader.readNext()), null);
	}

	public VObjectService getvObjectService() {
		return vObjectService;
	}

	public void setvObjectService(VObjectService vObjectService) {
		this.vObjectService = vObjectService;
	}

	public DataObjectService getDataObjectService() {
		return dataObjectService;
	}

	public void setDataObjectService(DataObjectService dataObjectService) {
		this.dataObjectService = dataObjectService;
	}

}
