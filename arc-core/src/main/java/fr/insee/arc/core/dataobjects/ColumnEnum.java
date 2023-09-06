package fr.insee.arc.core.dataobjects;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public enum ColumnEnum {

	  BLOCKING_THRESHOLD("blocking_threshold", TypeEnum.TEXT, "") // ihm_controle_regle
	, BORNE_INF("borne_inf", TypeEnum.TEXT, "") // ihm_controle_regle
	, BORNE_SUP("borne_sup", TypeEnum.TEXT, "") // ihm_controle_regle
	, CALL_ID("call_id", TypeEnum.INTEGER, "") // ihm_ws_context,ihm_ws_query
	, COMMENTAIRE("commentaire", TypeEnum.TEXT, "") // ihm_controle_regle,ihm_mapping_regle,ihm_normage_regle,ihm_chargement_regle,ihm_expression
	, CONDITION("condition", TypeEnum.TEXT, "") // ihm_controle_regle
	, DATE_INACTIF("date_inactif", TypeEnum.DATE, "") // ihm_jeuderegle
	, DATE_PRODUCTION("date_production", TypeEnum.DATE, "") // ihm_jeuderegle
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
	, EVENT_TIMESTAMP("event_timestamp", TypeEnum.TIMESTAMP, "") // ihm_webservice_log
	, EXPRESSION("expression", TypeEnum.TEXT, "") // ihm_ws_query
	, EXPR_NOM("expr_nom", TypeEnum.TEXT, "") // ihm_expression
	, EXPR_REGLE_COL("expr_regle_col", TypeEnum.TEXT, "") // ihm_mapping_regle
	, EXPR_VALEUR("expr_valeur", TypeEnum.TEXT, "") // ihm_expression
	, FORMAT("format", TypeEnum.TEXT, "") // ihm_chargement_regle
	, HOST_ALLOWED("host_allowed", TypeEnum.TEXT, "") // ihm_webservice_whitelist,ihm_webservice_log
	, ID("id", TypeEnum.TEXT, "") // ext_webservice_type,ext_mod_periodicite,ext_webservice_queryview,ext_export_format,ext_etat,ext_type_normage,ext_type_fichier_chargement,ext_type_controle,ext_etat_jeuderegle
	, IDEP("idep", TypeEnum.TEXT, "") // ihm_user
	, ID_APPLICATION("id_application", TypeEnum.TEXT, "") // ihm_webservice_whitelist,ihm_webservice_log,ihm_client
	, ID_CLASSE("id_classe", TypeEnum.TEXT, "") // ihm_normage_regle,ihm_controle_regle
	, ID_ENTREPOT("id_entrepot", TypeEnum.TEXT, "") // ihm_entrepot
	, ID_FAMILLE("id_famille", TypeEnum.TEXT, "") // ihm_norme,ihm_webservice_log,ihm_mod_variable_metier,ihm_famille,ihm_mod_table_metier,ihm_webservice_whitelist,ihm_client
	, ID_LOADER("id_loader", TypeEnum.TEXT, "") // ihm_entrepot
	, ID_NORME("id_norme", TypeEnum.TEXT, "") // ihm_calendrier,ihm_expression,ihm_controle_regle,ihm_jeuderegle,ihm_norme,ihm_chargement_regle,ihm_normage_regle,ihm_mapping_regle
	, ID_REGLE("id_regle", TypeEnum.BIGINT, "") // ihm_chargement_regle,ihm_expression,ihm_mapping_regle, ihm_controle_regle,ihm_normage_regle
	, ID_REGLE_INT("id_regle", TypeEnum.INTEGER, "") // ihm_controle_regle,ihm_normage_regle
	, ID_WEBSERVICE_LOGGING("id_webservice_logging", TypeEnum.BIGINT, "") // ihm_webservice_log
	, ISENV("isenv", TypeEnum.BOOLEAN, "") // ext_etat_jeuderegle
	, IS_SECURED("is_secured", TypeEnum.TEXT, "") // ihm_webservice_whitelist
	, KEY("key", TypeEnum.TEXT, "") // parameter
	, LAST_INIT("last_init", TypeEnum.TEXT, "") // pilotage_batch
	, MISE_A_JOUR_IMMEDIATE("mise_a_jour_immediate", TypeEnum.BOOLEAN, "") // ext_etat_jeuderegle
	, NOM_COLONNE("nom_colonne", TypeEnum.TEXT, "") // ihm_schema_nmcl
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
	, TYPE_COLONNE("type_colonne", TypeEnum.TEXT, "") // ihm_schema_nmcl
	, TYPE_CONSOLIDATION("type_consolidation", TypeEnum.TEXT, "") // ihm_mod_variable_metier
	, TYPE_FICHIER("type_fichier", TypeEnum.TEXT, "") // ihm_chargement_regle
	, TYPE_NMCL("type_nmcl", TypeEnum.TEXT, "") // ihm_schema_nmcl
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
	
	, DATE_ENTREE("date_entree", TypeEnum.TEXT, "") // date_entree
	
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
	
	, TEST1("test1", TypeEnum.TEXT, "test column 1"), TEST2("test2", TypeEnum.TEXT, "test column 2")

	
	, COLUMN_NAME("column_name", TypeEnum.TEXT, "column name in information schema")
	, TABLE_NAME("table_name", TypeEnum.TEXT, "table name in information schema")
	, TABLE_SCHEMA("table_schema", TypeEnum.TEXT, "schema name in information schema")
	
	, TABLENAME("tablename", TypeEnum.TEXT, "table name in pg_tables")
	, SCHEMANAME("schemaname", TypeEnum.TEXT, "schema name in pg_tables")

	, PHASE_TRAITEMENT("phase_traitement", TypeEnum.TEXT, "phase identifier in pilotage")
	, ETAT_TRAITEMENT("etat_traitement", TypeEnum.TEXT_ARRAY, "phase identifier in pilotage")
	
	
	;

	private String columnName;
	private TypeEnum columnType;
	private String columnExplanation;

	private ColumnEnum(String columnName, TypeEnum columnType, String columnExplanation) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.columnExplanation = columnExplanation;
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

}
