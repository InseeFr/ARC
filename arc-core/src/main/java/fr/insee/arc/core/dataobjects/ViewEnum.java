package fr.insee.arc.core.dataobjects;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public enum ViewEnum {

	IHM_NORME("ihm_norme", false, ColumnEnum.ID_FAMILLE, ColumnEnum.ID_NORME, ColumnEnum.PERIODICITE, ColumnEnum.DEF_NORME, ColumnEnum.DEF_VALIDITE, ColumnEnum.ETAT)
	, IHM_CALENDRIER("ihm_calendrier", false, ColumnEnum.ID_NORME, ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.ETAT)
	, IHM_JEUDEREGLE("ihm_jeuderegle", false, ColumnEnum.ID_NORME, ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION, ColumnEnum.ETAT, ColumnEnum.DATE_PRODUCTION, ColumnEnum.DATE_INACTIF)
	, IHM_CHARGEMENT_REGLE("ihm_chargement_regle", false, ColumnEnum.ID_REGLE, ColumnEnum.ID_NORME, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION, ColumnEnum.PERIODICITE, ColumnEnum.TYPE_FICHIER, ColumnEnum.DELIMITER, ColumnEnum.FORMAT, ColumnEnum.COMMENTAIRE)
	, IHM_NORMAGE_REGLE("ihm_normage_regle", false, ColumnEnum.ID_NORME, ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION, ColumnEnum.ID_CLASSE, ColumnEnum.RUBRIQUE, ColumnEnum.RUBRIQUE_NMCL, ColumnEnum.ID_REGLE, ColumnEnum.TODO, ColumnEnum.COMMENTAIRE)
	, IHM_CONTROLE_REGLE("ihm_controle_regle", false, ColumnEnum.ID_NORME, ColumnEnum.PERIODICITE, ColumnEnum.VALIDITE_INF, ColumnEnum.VALIDITE_SUP, ColumnEnum.VERSION, ColumnEnum.ID_CLASSE, ColumnEnum.RUBRIQUE_PERE, ColumnEnum.RUBRIQUE_FILS, ColumnEnum.BORNE_INF, ColumnEnum.BORNE_SUP, ColumnEnum.CONDITION, ColumnEnum.PRE_ACTION, ColumnEnum.ID_REGLE, ColumnEnum.TODO, ColumnEnum.COMMENTAIRE, ColumnEnum.XSD_ORDRE, ColumnEnum.XSD_LABEL_FILS, ColumnEnum.XSD_ROLE, ColumnEnum.BLOCKING_THRESHOLD, ColumnEnum.ERROR_ROW_PROCESSING)
	, IHM_FILTRAGE_REGLE("ihm_filtrage_regle", false)
	, IHM_MAPPING_REGLE("ihm_mapping_regle", false)
	, IHM_EXPRESSION("ihm_expression", false)
	, IHM_CLIENT("ihm_client", false)
	, IHM_ENTREPOT("ihm_entrepot", false)
	, IHM_FAMILLE("ihm_famille", false)
	, IHM_MOD_TABLE_METIER("ihm_mod_table_metier", false)
	, IHM_MOD_VARIABLE_METIER("ihm_mod_variable_metier", false)
	, IHM_NMCL("ihm_nmcl", false)
	, IHM_SCHEMA_NMCL("ihm_schema_nmcl", false)
	, ID_TABLE_IHM_SEUIL("ihm_seuil", false)
	, PILOTAGE_BATCH("pilotage_batch", false)

	, PILOTAGE_FICHIER("pilotage_fichier", true)
	, PILOTAGE_ARCHIVE("pilotage_archive", true)
	
	, TEST("test",false,ColumnEnum.TEST1,ColumnEnum.TEST2)

	;

	private ViewEnum(String tableName, boolean isTableInSanbox, ColumnEnum...columns) {
		this.tableName = tableName;
		this.isTableInSanbox = isTableInSanbox;
		this.columns=new LinkedHashSet<>(Arrays.asList(columns));
	}

	/**
	 * database real name
	 */
	private String tableName;

	/**
	 * indicate if the table belongs to a sandbox
	 */
	private boolean isTableInSanbox;
	
	private Set<ColumnEnum> columns;

	public String getTableName() {
		return tableName;
	}

	public boolean isTableInSanbox() {
		return isTableInSanbox;
	}

	public Set<ColumnEnum> getColumns() {
		return columns;
	}

	public void setColumns(Set<ColumnEnum> columns) {
		this.columns = columns;
	}

	
	
}
