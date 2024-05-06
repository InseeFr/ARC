package fr.insee.arc.core.dataobjects;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dataobjects.PgColumnEnum;
import fr.insee.arc.utils.dataobjects.PgSchemaEnum;
import fr.insee.arc.utils.dataobjects.TypeEnum;

public enum ColumnEnum {

	  BLOCKING_THRESHOLD("blocking_threshold", TypeEnum.TEXT, "") // ihm_controle_regle
	, BORNE_INF("borne_inf", TypeEnum.TEXT, "") // ihm_controle_regle
	, BORNE_SUP("borne_sup", TypeEnum.TEXT, "") // ihm_controle_regle
	, CALL_ID("call_id", TypeEnum.INTEGER, "") // ihm_ws_context,ihm_ws_query
	, COMMENTAIRE("commentaire", TypeEnum.TEXT, "") // ihm_controle_regle,ihm_mapping_regle,ihm_normage_regle,ihm_chargement_regle,ihm_expression
	, CONDITION("condition", TypeEnum.TEXT, "") // ihm_controle_regle
	, DATE_INACTIF("date_inactif", TypeEnum.DATE, "") // ihm_jeuderegle
	, DATE_PRODUCTION("date_production", TypeEnum.DATE, "") // ihm_jeuderegle
	, DATE_INTEGRATION("date_integration", TypeEnum.TEXT, "")
	, DEF_NORME("def_norme", TypeEnum.TEXT, "") // ihm_norme
	, DEF_VALIDITE("def_validite", TypeEnum.TEXT, "") // ihm_norme
	, DELIMITER("delimiter", TypeEnum.TEXT, "") // ihm_chargement_regle
	, DESCRIPTION("description", TypeEnum.TEXT, "") // ihm_nmcl,parameter
	, DESCRIPTION_TABLE_METIER("description_table_metier", TypeEnum.TEXT, "") // ihm_mod_table_metier
	, DESCRIPTION_TYPE("description_type", TypeEnum.TEXT, "") // ext_mod_type_autorise
	, DESCRIPTION_VARIABLE_METIER("description_variable_metier", TypeEnum.TEXT, "") // ihm_mod_variable_metier
	, ENVIRONMENT("environment", TypeEnum.TEXT, "") // ihm_ws_context
	, ENV_DESCRIPTION("env_description", TypeEnum.TEXT, "") // ext_etat_jeuderegle
	, ERROR_ROW_PROCESSING("error_row_processing", TypeEnum.TEXT, "") // ihm_controle_regle
	, ETAT("etat", TypeEnum.TEXT, "") // ihm_jeuderegle,ihm_norme,ihm_calendrier, export
	, EVENT_TIMESTAMP("event_timestamp", TypeEnum.TIMESTAMP, "") // security_webservice_log
	, EXPRESSION("expression", TypeEnum.TEXT, "") // ihm_ws_query
	, EXPR_NOM("expr_nom", TypeEnum.TEXT, "") // ihm_expression
	, EXPR_REGLE_COL("expr_regle_col", TypeEnum.TEXT, "") // ihm_mapping_regle
	, EXPR_VALEUR("expr_valeur", TypeEnum.TEXT, "") // ihm_expression
	, FORMAT("format", TypeEnum.TEXT, "") // ihm_chargement_regle
	, HOST_ALLOWED("host_allowed", TypeEnum.TEXT, "") // ihm_webservice_whitelist,security_webservice_log
	, ID("id", TypeEnum.TEXT, "") // ext_webservice_type,ext_mod_periodicite,ext_webservice_queryview,ext_export_format,ext_etat,ext_type_normage,ext_type_fichier_chargement,ext_type_controle,ext_etat_jeuderegle
	, ID_SAX("id", TypeEnum.INTEGER, "") // used by sax xml loaders
	, IDEP("idep", TypeEnum.TEXT, "") // ihm_user
	, ID_APPLICATION("id_application", TypeEnum.TEXT, "") // ihm_webservice_whitelist,security_webservice_log,ihm_client
	, ID_CLASSE("id_classe", TypeEnum.TEXT, "") // ihm_normage_regle,ihm_controle_regle
	, ID_ENTREPOT("id_entrepot", TypeEnum.TEXT, "") // ihm_entrepot
	, ID_FAMILLE("id_famille", TypeEnum.TEXT, "") // ihm_norme,security_webservice_log,ihm_mod_variable_metier,ihm_famille,ihm_mod_table_metier,ihm_webservice_whitelist,ihm_client
	, ID_LOADER("id_loader", TypeEnum.TEXT, "") // ihm_entrepot
	, ID_NORME("id_norme", TypeEnum.TEXT, "") // ihm_calendrier,ihm_expression,ihm_controle_regle,ihm_jeuderegle,ihm_norme,ihm_chargement_regle,ihm_normage_regle,ihm_mapping_regle
	, ID_REGLE("id_regle", TypeEnum.BIGINT, "") // ihm_chargement_regle,ihm_expression,ihm_mapping_regle, ihm_controle_regle,ihm_normage_regle
	, ID_REGLE_INT("id_regle", TypeEnum.INTEGER, "") // ihm_controle_regle,ihm_normage_regle
	, ID_WEBSERVICE_LOGGING("id_webservice_logging", TypeEnum.BIGINT, "") // security_webservice_log
	, ISENV("isenv", TypeEnum.BOOLEAN, "") // ext_etat_jeuderegle
	, IS_SECURED("is_secured", TypeEnum.TEXT, "") // ihm_webservice_whitelist
	, KEY("key", TypeEnum.TEXT, "") // parameter
	, LAST_INIT("last_init", TypeEnum.TEXT, "") // pilotage_batch
	, MISE_A_JOUR_IMMEDIATE("mise_a_jour_immediate", TypeEnum.BOOLEAN, "") // ext_etat_jeuderegle
	, NOM_COLONNE("nom_colonne", TypeEnum.TEXT, "") // view schema nmcl
	, NOM_TABLE("nom_table", TypeEnum.TEXT, "") // ihm_nmcl
	, NOM_TABLE_METIER("nom_table_metier", TypeEnum.TEXT, "") // ihm_mod_table_metier,ihm_mod_variable_metier
	, NOM_TYPE("nom_type", TypeEnum.NAME, "") // ext_mod_type_autorise
	, NOM_VARIABLE_METIER("nom_variable_metier", TypeEnum.TEXT, "") // ihm_mod_variable_metier
	, NORME("norme", TypeEnum.TEXT, "") // ihm_ws_context
	, OPERATION("operation", TypeEnum.TEXT, "") // pilotage_batch
	, ORDRE("ordre", TypeEnum.INTEGER, "") // ext_type_fichier_chargement,ext_type_controle,ext_type_normage
	, PERIODICITE("periodicite", TypeEnum.TEXT, "") // ihm_controle_regle,ihm_jeuderegle,ihm_calendrier,ihm_mapping_regle,ihm_normage_regle,ihm_expression,ihm_ws_context,ihm_norme,ihm_chargement_regle
	, PRE_ACTION("pre_action", TypeEnum.TEXT, "") // ihm_controle_regle
	, PROFIL("profil", TypeEnum.TEXT, "") // ihm_user
	, QUERY_ID("query_id", TypeEnum.INTEGER, "") // ihm_ws_query
	, QUERY_NAME("query_name", TypeEnum.TEXT, "") // ihm_ws_query
	, QUERY_VIEW("query_view", TypeEnum.INTEGER, "") // ihm_ws_query
	, RUBRIQUE("rubrique", TypeEnum.TEXT, "") // ihm_normage_regle
	, RUBRIQUE_FILS("rubrique_fils", TypeEnum.TEXT, "") // ihm_controle_regle
	, RUBRIQUE_NMCL("rubrique_nmcl", TypeEnum.TEXT, "") // ihm_normage_regle
	, RUBRIQUE_PERE("rubrique_pere", TypeEnum.TEXT, "") // ihm_controle_regle
	, SERVICE_NAME("service_name", TypeEnum.TEXT, "") // ihm_ws_context,ihm_ws_query
	, SERVICE_TYPE("service_type", TypeEnum.INTEGER, "") // ihm_ws_context
	, TARGET_PHASE("target_phase", TypeEnum.TEXT, "") // ihm_ws_context
	, TODO("todo", TypeEnum.TEXT, "") // ihm_controle_regle,ihm_normage_regle
	, TYPE_COLONNE("type_colonne", TypeEnum.TEXT, "") // view schema nmcl
	, TYPE_CONSOLIDATION("type_consolidation", TypeEnum.TEXT, "") // ihm_mod_variable_metier
	, TYPE_FICHIER("type_fichier", TypeEnum.TEXT, "") // ihm_chargement_regle
	, TYPE_NMCL("type_nmcl", TypeEnum.TEXT, "") // view schema nmcl
	, TYPE_VARIABLE_METIER("type_variable_metier", TypeEnum.NAME, "") // ihm_mod_variable_metier
	, VAL("val", TypeEnum.TEXT, "") // ext_export_format,ext_etat_jeuderegle,ext_webservice_queryview,ext_webservice_type,ext_etat,parameter,ext_mod_periodicite
	, VALIDITE("validite", TypeEnum.TEXT, "") // ihm_ws_context
	, VALIDITE_INF("validite_inf", TypeEnum.DATE, "") // ihm_expression,ihm_normage_regle,ihm_chargement_regle,ihm_mapping_regle,ihm_calendrier,ihm_jeuderegle,ihm_controle_regle
	, VALIDITE_SUP("validite_sup", TypeEnum.DATE, "") // ihm_mapping_regle,ihm_expression,ihm_normage_regle,ihm_controle_regle,ihm_chargement_regle,ihm_calendrier,ihm_jeuderegle
	, VARIABLE_SORTIE("variable_sortie", TypeEnum.TEXT, "") // ihm_mapping_regle
	, VERSION("version", TypeEnum.TEXT, "") // ihm_normage_regle,ihm_controle_regle,ihm_chargement_regle,ihm_mapping_regle,ihm_expression,ihm_jeuderegle
	, XSD_LABEL_FILS("xsd_label_fils", TypeEnum.TEXT, "") // ihm_controle_regle
	, XSD_ORDRE("xsd_ordre", TypeEnum.INTEGER, "") // ihm_controle_regle
	, XSD_ROLE("xsd_role", TypeEnum.TEXT, "") // ihm_controle_regle

	, FILE_NAME("file_name", TypeEnum.TEXT, "") // export
	, TABLE_TO_EXPORT("table_to_export", TypeEnum.TEXT, "") // export
	, NOMENCLATURE_EXPORT("nomenclature_export", TypeEnum.TEXT, "") // export
	, FILTER_TABLE("filter_table", TypeEnum.TEXT, "") // export
	, COLUMNS_ARRAY_HEADER("columns_array_header", TypeEnum.TEXT, "") // export
	, COLUMNS_ARRAY_VALUE("columns_array_value", TypeEnum.TEXT, "") // export
	, NULLS("nulls", TypeEnum.TEXT, "") // export
	, HEADERS("headers", TypeEnum.TEXT, "") // export
	, ORDER_TABLE("order_table", TypeEnum.TEXT, "") // export
	, ZIP("zip", TypeEnum.TEXT, "") // export
	
	, ID_SOURCE("id_source", TypeEnum.TEXT, "the entry filename contatenated with entry repository") //

	, MODULE_ORDER("module_order", TypeEnum.INTEGER, "index of rules module")
	, MODULE_NAME("module_name", TypeEnum.TEXT, "name of rules module")
	
	, PHASE_TRAITEMENT("phase_traitement", TypeEnum.TEXT, "phase of the process of file in pilotage")
	, ETAT_TRAITEMENT("etat_traitement", TypeEnum.TEXT_ARRAY, "state of the process of file in pilotage")
	, DATE_TRAITEMENT("date_traitement", TypeEnum.TIMESTAMP, "timestamp of the beginning of the process of file in pilotage")
	, RAPPORT("rapport", TypeEnum.INTEGER, "report of the process of file in pilotage")
	, TAUX_KO("taux_ko", TypeEnum.NUMERIC, "deprecated")
	, NB_ENR("nb_enr", TypeEnum.INTEGER, "number records of the process of file in pilotage")
	, NB_ESSAIS("nb_essais", TypeEnum.INTEGER, "deprecated")
	, ETAPE("etape", TypeEnum.INTEGER, "status of the process of file in pilotage")
	, DATE_ENTREE("date_entree", TypeEnum.TEXT, "entry date of the file")
	, CONTAINER("container", TypeEnum.TEXT, "name of the file container reworked by arc")
	, V_CONTAINER("v_container", TypeEnum.TEXT, "version of the container")
	, O_CONTAINER("o_container", TypeEnum.TEXT, "genuine name of the file container")
	, TO_DELETE("to_delete", TypeEnum.TEXT, "flag to mark a user maintenance operation for a file")
	, CLIENT("client", TypeEnum.TEXT_ARRAY, "clients who have already retrieved the file")
	, DATE_CLIENT("date_client", TypeEnum.TIMESTAMP_ARRAY, "dates when the clients retrieved the file")
	, JOINTURE("jointure", TypeEnum.TEXT, "data structure query of the processing file")
	, GENERATION_COMPOSITE("generation_composite", TypeEnum.TEXT, "timestamp of the end of the process of the file in pilotage")

	, ENTREPOT("entrepot", TypeEnum.TEXT, "name of the datastore that recieve archive file")
	, NOM_ARCHIVE("nom_archive", TypeEnum.TEXT, "name of archive file")
	
	// aliases
	, VARBDD("varbdd", TypeEnum.TEXT, "variable in BDD") // export
	, POS("pos", TypeEnum.BIGINT, "ordinal position") // export
	, MAXP("maxp", TypeEnum.BIGINT, "highest position") // export
	, I("i", TypeEnum.BIGINT, "row number index") // maintenanceparametre
	, NB("nb", TypeEnum.BIGINT, "count") // pilotage
	
	, TEST1(PgColumnEnum.TEST1)
	, TEST2(PgColumnEnum.TEST2)
		
	, COLUMN_NAME(PgColumnEnum.COLUMN_NAME)
	, TABLE_NAME(PgColumnEnum.TABLE_NAME)
	, TABLE_SCHEMA(PgColumnEnum.TABLE_SCHEMA)
	
	, TABLENAME(PgColumnEnum.TABLENAME)
	, SCHEMANAME(PgColumnEnum.SCHEMANAME)

	, M0("m0", TypeEnum.TEXT, "short column m0 in xml parser")
	, M1("m1", TypeEnum.INTEGER, "short column m1 in xml parser")
	, M2("m2", TypeEnum.TEXT, "short column m2 in xml parser")
	, M3("m3", TypeEnum.TEXT, "short column m3 in xml parser")
	, M4("m4", TypeEnum.TEXT, "short column m4 in xml parser")
	, M5("m5", TypeEnum.TEXT, "short column m5 in xml parser")

	;

	private String columnName;
	private TypeEnum columnType;
	private String columnExplanation;

	private ColumnEnum(String columnName, TypeEnum columnType, String columnExplanation) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.columnExplanation = columnExplanation;
	}
	
	/** contructor from PgColumnEnum */
	private ColumnEnum(PgColumnEnum c) {
		this.columnName = c.getColumnName();
		this.columnType = c.getColumnType();
		this.columnExplanation = c.getColumnExplanation();
	}
	

	public String getColumnName() {
		return columnName;
	}

	public TypeEnum getColumnType() {
		return columnType;
	}

	public String getColumnExplanation() {
		return columnExplanation;
	}

	/**
	 * return the list of columnEnum name
	 * 
	 * @param listOfColumnEnum
	 * @return
	 */
	public static List<String> listColumnEnumByName(Collection<ColumnEnum> listOfColumnEnum) {
		return listOfColumnEnum.stream().map(ColumnEnum::getColumnName).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return this.getColumnName();
	}


	public static SchemaEnum convert(PgSchemaEnum pgSchema)
	{
		return SchemaEnum.valueOf(pgSchema.toString());
	}
	
	public String alias(ViewEnum v)
	{
		return v.getTableName()+SQL.DOT.getSqlCode()+this.columnName;
	}
	
}
