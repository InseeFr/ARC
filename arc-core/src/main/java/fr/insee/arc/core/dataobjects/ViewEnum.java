package fr.insee.arc.core.dataobjects;

import java.util.LinkedHashMap;
import java.util.Map;

public enum ViewEnum {

	// tables de modalités
	 EXT_ETAT("ext_etat", SchemaEnum.METADATA, ColumnEnum.ID, ColumnEnum.VAL) //
	, EXT_ETAT_JEUDEREGLE("ext_etat_jeuderegle", SchemaEnum.METADATA, ColumnEnum.ID, ColumnEnum.VAL, ColumnEnum.ISENV, ColumnEnum.MISE_A_JOUR_IMMEDIATE, ColumnEnum.ENV_DESCRIPTION) //
	, EXT_EXPORT_FORMAT("ext_export_format", SchemaEnum.METADATA, ColumnEnum.ID, ColumnEnum.VAL) //
	, EXT_MOD_PERIODICITE("ext_mod_periodicite", SchemaEnum.METADATA, ColumnEnum.ID, ColumnEnum.VAL) //
	, EXT_MOD_TYPE_AUTORISE("ext_mod_type_autorise", SchemaEnum.METADATA, ColumnEnum.NOM_TYPE, ColumnEnum.DESCRIPTION_TYPE) //
	, EXT_TYPE_CONTROLE("ext_type_controle", SchemaEnum.METADATA, ColumnEnum.ID, ColumnEnum.ORDRE) //
	, EXT_TYPE_FICHIER_CHARGEMENT("ext_type_fichier_chargement", SchemaEnum.METADATA, ColumnEnum.ID, ColumnEnum.ORDRE) //
	, EXT_TYPE_NORMAGE("ext_type_normage", SchemaEnum.METADATA, ColumnEnum.ID, ColumnEnum.ORDRE) //
	, EXT_WEBSERVICE_QUERYVIEW("ext_webservice_queryview", SchemaEnum.METADATA, ColumnEnum.ID, ColumnEnum.VAL) //
	, EXT_WEBSERVICE_TYPE("ext_webservice_type", SchemaEnum.METADATA, ColumnEnum.ID, ColumnEnum.VAL) //
	
	// tables de règles
	, IHM_CALENDRIER("ihm_calendrier", SchemaEnum.METADATA, ColumnEnum.ID_NORME, ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.ETAT) //
	, IHM_CHARGEMENT_REGLE("ihm_chargement_regle", SchemaEnum.METADATA, ColumnEnum.ID_REGLE, ColumnEnum.ID_NORME, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION, ColumnEnum.PERIODICITE, ColumnEnum.TYPE_FICHIER, ColumnEnum.DELIMITER, ColumnEnum.FORMAT, ColumnEnum.COMMENTAIRE) //
	, IHM_CLIENT("ihm_client", SchemaEnum.METADATA, ColumnEnum.ID_FAMILLE, ColumnEnum.ID_APPLICATION) //
	, IHM_CONTROLE_REGLE("ihm_controle_regle", SchemaEnum.METADATA, ColumnEnum.ID_REGLE_INT, ColumnEnum.ID_NORME, ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION, ColumnEnum.ID_CLASSE, ColumnEnum.RUBRIQUE_PERE, ColumnEnum.RUBRIQUE_FILS, ColumnEnum.BORNE_INF, ColumnEnum.BORNE_SUP, ColumnEnum.CONDITION, ColumnEnum.PRE_ACTION, ColumnEnum.TODO, ColumnEnum.COMMENTAIRE, ColumnEnum.XSD_ORDRE, ColumnEnum.XSD_LABEL_FILS, ColumnEnum.XSD_ROLE, ColumnEnum.BLOCKING_THRESHOLD, ColumnEnum.ERROR_ROW_PROCESSING) //
	, IHM_ENTREPOT("ihm_entrepot", SchemaEnum.METADATA, ColumnEnum.ID_ENTREPOT, ColumnEnum.ID_LOADER) //
	, IHM_EXPRESSION("ihm_expression", SchemaEnum.METADATA, ColumnEnum.ID_REGLE, ColumnEnum.ID_NORME, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION, ColumnEnum.PERIODICITE, ColumnEnum.EXPR_NOM, ColumnEnum.EXPR_VALEUR, ColumnEnum.COMMENTAIRE) //
	, IHM_FAMILLE("ihm_famille", SchemaEnum.METADATA, ColumnEnum.ID_FAMILLE) //
	, IHM_JEUDEREGLE("ihm_jeuderegle", SchemaEnum.METADATA, ColumnEnum.ID_NORME, ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION, ColumnEnum.ETAT, ColumnEnum.DATE_PRODUCTION, ColumnEnum.DATE_INACTIF) //
	, IHM_MAPPING_REGLE("ihm_mapping_regle", SchemaEnum.METADATA, ColumnEnum.ID_REGLE, ColumnEnum.ID_NORME, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION, ColumnEnum.PERIODICITE, ColumnEnum.VARIABLE_SORTIE, ColumnEnum.EXPR_REGLE_COL, ColumnEnum.COMMENTAIRE) //
	, IHM_MOD_TABLE_METIER("ihm_mod_table_metier", SchemaEnum.METADATA, ColumnEnum.ID_FAMILLE, ColumnEnum.NOM_TABLE_METIER, ColumnEnum.DESCRIPTION_TABLE_METIER) //
	, IHM_MOD_VARIABLE_METIER("ihm_mod_variable_metier", SchemaEnum.METADATA, ColumnEnum.ID_FAMILLE, ColumnEnum.NOM_TABLE_METIER, ColumnEnum.NOM_VARIABLE_METIER, ColumnEnum.TYPE_VARIABLE_METIER, ColumnEnum.DESCRIPTION_VARIABLE_METIER, ColumnEnum.TYPE_CONSOLIDATION) //
	, IHM_NMCL("ihm_nmcl", SchemaEnum.METADATA, ColumnEnum.NOM_TABLE, ColumnEnum.DESCRIPTION) //
	, IHM_NORMAGE_REGLE("ihm_normage_regle", SchemaEnum.METADATA, ColumnEnum.ID_REGLE_INT, ColumnEnum.ID_NORME, ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION, ColumnEnum.ID_CLASSE, ColumnEnum.RUBRIQUE, ColumnEnum.RUBRIQUE_NMCL, ColumnEnum.TODO, ColumnEnum.COMMENTAIRE) //
	, IHM_NORME("ihm_norme", SchemaEnum.METADATA, ColumnEnum.ID_FAMILLE, ColumnEnum.ID_NORME, ColumnEnum.PERIODICITE, ColumnEnum.DEF_NORME, ColumnEnum.DEF_VALIDITE, ColumnEnum.ETAT) //
	, IHM_SCHEMA_NMCL("ihm_schema_nmcl", SchemaEnum.METADATA, ColumnEnum.TYPE_NMCL, ColumnEnum.NOM_COLONNE, ColumnEnum.TYPE_COLONNE) //
	, IHM_USER("ihm_user", SchemaEnum.METADATA, ColumnEnum.IDEP, ColumnEnum.PROFIL) //
	, IHM_WEBSERVICE_LOG("ihm_webservice_log", SchemaEnum.METADATA, ColumnEnum.ID_WEBSERVICE_LOGGING, ColumnEnum.ID_FAMILLE, ColumnEnum.ID_APPLICATION, ColumnEnum.HOST_ALLOWED, ColumnEnum.EVENT_TIMESTAMP) //
	, IHM_WEBSERVICE_WHITELIST("ihm_webservice_whitelist", SchemaEnum.METADATA, ColumnEnum.HOST_ALLOWED, ColumnEnum.ID_FAMILLE, ColumnEnum.ID_APPLICATION, ColumnEnum.IS_SECURED) //
	, IHM_WS_CONTEXT("ihm_ws_context", SchemaEnum.METADATA, ColumnEnum.SERVICE_NAME, ColumnEnum.SERVICE_TYPE, ColumnEnum.CALL_ID, ColumnEnum.ENVIRONMENT, ColumnEnum.TARGET_PHASE, ColumnEnum.NORME, ColumnEnum.VALIDITE, ColumnEnum.PERIODICITE) //
	, IHM_WS_QUERY("ihm_ws_query", SchemaEnum.METADATA, ColumnEnum.QUERY_ID, ColumnEnum.QUERY_NAME, ColumnEnum.EXPRESSION, ColumnEnum.QUERY_VIEW, ColumnEnum.SERVICE_NAME, ColumnEnum.CALL_ID) //
	
	// tables de paramètre
	, PARAMETER("parameter", SchemaEnum.METADATA, ColumnEnum.KEY, ColumnEnum.VAL, ColumnEnum.DESCRIPTION) //
	, PILOTAGE_BATCH("pilotage_batch", SchemaEnum.METADATA, ColumnEnum.LAST_INIT, ColumnEnum.OPERATION) //
	
	// tables de pilotage
	, PILOTAGE_FICHIER("pilotage_fichier", SchemaEnum.SANDBOX)
	, PILOTAGE_ARCHIVE("pilotage_archive", SchemaEnum.SANDBOX)
	
	// table d'export
	, EXPORT("export", SchemaEnum.SANDBOX, ColumnEnum.FILE_NAME, ColumnEnum.ZIP, ColumnEnum.TABLE_TO_EXPORT, ColumnEnum.HEADERS, ColumnEnum.NULLS, ColumnEnum.FILTER_TABLE, ColumnEnum.ORDER_TABLE, ColumnEnum.NOMENCLATURE_EXPORT, ColumnEnum.COLUMNS_ARRAY_HEADER, ColumnEnum.COLUMNS_ARRAY_VALUE, ColumnEnum.ETAT) //
	
	// tables représentant le contenu des vobject (utilisées pour les tests)
	, VIEW_PILOTAGE_FICHIER("pilotage_fichier", SchemaEnum.SANDBOX, ColumnEnum.DATE_ENTREE, ColumnEnum.PHASE_TRAITEMENT, ColumnEnum.ETAT_TRAITEMENT)
	
	// family model table in sandbox
	, MOD_TABLE_METIER("mod_table_metier", SchemaEnum.SANDBOX_GENERATED , ColumnEnum.ID_FAMILLE, ColumnEnum.NOM_TABLE_METIER, ColumnEnum.DESCRIPTION_TABLE_METIER) //
	, MOD_VARIABLE_METIER("mod_variable_metier", SchemaEnum.SANDBOX_GENERATED, ColumnEnum.ID_FAMILLE, ColumnEnum.NOM_TABLE_METIER, ColumnEnum.NOM_VARIABLE_METIER, ColumnEnum.TYPE_VARIABLE_METIER, ColumnEnum.DESCRIPTION_VARIABLE_METIER, ColumnEnum.TYPE_CONSOLIDATION) //
	
	// rule model tables in sandbox
	, NORME("norme", SchemaEnum.SANDBOX_GENERATED, ColumnEnum.ID_FAMILLE, ColumnEnum.ID_NORME, ColumnEnum.PERIODICITE, ColumnEnum.DEF_NORME, ColumnEnum.DEF_VALIDITE, ColumnEnum.ETAT) //
	, CALENDRIER("calendrier", SchemaEnum.SANDBOX_GENERATED, ColumnEnum.ID_NORME, ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.ETAT) //
	, JEUDEREGLE("jeuderegle", SchemaEnum.SANDBOX_GENERATED, ColumnEnum.ID_NORME, ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION, ColumnEnum.ETAT, ColumnEnum.DATE_PRODUCTION, ColumnEnum.DATE_INACTIF) //

	// tables utilisés pour les tests

	, TABLE_TEST_IN_PUBLIC("table_test_in_public",SchemaEnum.PUBLIC,ColumnEnum.TEST1,ColumnEnum.TEST2)
	, TABLE_TEST_OUT_PUBLIC("table_test_out_public",SchemaEnum.PUBLIC,ColumnEnum.TEST1,ColumnEnum.TEST2)
	
	, TABLE_TEST_IN_TEMPORARY("table_test_in_temp",SchemaEnum.TEMPORARY,ColumnEnum.TEST1,ColumnEnum.TEST2)
	, TABLE_TEST_OUT_TEMPORARY("table_test_out_temp",SchemaEnum.TEMPORARY,ColumnEnum.TEST1,ColumnEnum.TEST2)

	
	// view for table aliases or temporary table in query
	, T1("t1",SchemaEnum.TEMPORARY), T2("t2",SchemaEnum.TEMPORARY), T3("t3",SchemaEnum.TEMPORARY)
	
	;

	private ViewEnum(String tableName, SchemaEnum location, ColumnEnum...columns) {
		this.tableName = tableName;
		this.tableLocation = location;
		
		this.columns=new LinkedHashMap<>();
		for (ColumnEnum col:columns)
		{
			this.columns.put(col, col);
			
		}
		
	}

	/**
	 * database real name
	 */
	private String tableName;

	/**
	 * indicate if the table belongs to a sandbox
	 */
	private SchemaEnum tableLocation;

	private Map<ColumnEnum,ColumnEnum> columns;

	public String getTableName() {
		return tableName;
	}
	
	public SchemaEnum getTableLocation() {
		return tableLocation;
	}

	public Map<ColumnEnum,ColumnEnum> getColumns() {
		return columns;
	}
	
}
