package fr.insee.arc.web.gui.norme.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.DataObjectService;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.format.Format;
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
	 * @param viewObject
	 * @param viewNorme
	 * @param theTableName
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
	 * @param viewObject
	 * @param viewCalendar
	 * @param theTableName
	 * @param selection
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
	 * @param viewRulesSet
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
	 * @param viewNormage
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

		ViewEnum dataModelChargement = ViewEnum.IHM_NORMAGE_REGLE;

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

		vObjectService.initialize(viewNormage, query, dataObjectService.getView(dataModelChargement),
				defaultInputFields);
	}

	/**
	 * generate a blank rules set for mapping based on variables declared in data
	 * model
	 * 
	 * @param viewMapping
	 * @param viewJeuxDeRegles
	 * @throws ArcException
	 */
	public void execQueryPreGenererRegleMapping(VObject viewNorme, VObject viewJeuxDeRegles, VObject viewMapping) throws ArcException {
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
	 * @param table
	 * @return
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
	 * 
	 * @param vObjectToUpdate the vObject to update with file
	 * @param tableName       the
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
			
			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(theFileToUpload.getInputStream(), StandardCharsets.UTF_8));) {
				// Get headers
				List<String> listHeaders = getHeaderFromFile(bufferedReader);

				/*
				 * Création d'une table temporaire (qui ne peut pas être TEMPORARY)
				 */
				ArcPreparedStatementBuilder requete=new ArcPreparedStatementBuilder();
			    requete.append("\n DROP TABLE IF EXISTS " + nomTableImage + " cascade;");
			    requete.append("\n CREATE TABLE " + nomTableImage + " AS SELECT "//
				    + Format.untokenize(listHeaders, ", ") //
				    + "\n\t FROM " //
				    + vObjectToUpdate.getTable() //
				    + "\n\t WHERE false");


				UtilitaireDao.get(0).executeRequest(null, requete);

				// Throwing away the first line
				String uselessLine = bufferedReader.readLine();
				LoggerHelper.debug(LOGGER, uselessLine + "is thrown away");
				

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

			ArcPreparedStatementBuilder requete=new ArcPreparedStatementBuilder();

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
	

	private static List<String> getHeaderFromFile(BufferedReader bufferedReader) throws IOException {
		String listeColonnesAggregees = bufferedReader.readLine();
		List<String> listeColonnes = Arrays.asList(listeColonnesAggregees.split(IConstanteCaractere.semicolon));
		LoggerHelper.debug(LOGGER, "Columns list : ", Format.untokenize(listeColonnes, ", "));
		return listeColonnes;
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
