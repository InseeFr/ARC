package fr.insee.arc.core.databaseobjects;

public enum TableEnum {

	IHM_NORME("ihm_norme", false)
	, IHM_CALENDRIER("ihm_calendrier", false)
	, IHM_JEUDEREGLE("ihm_jeuderegle", false)
	, IHM_CHARGEMENT_REGLE("ihm_chargement_regle", false)
	, IHM_NORMAGE_REGLE("ihm_normage_regle", false)
	, IHM_CONTROLE_REGLE("ihm_controle_regle", false)
	, IHM_FILTRAGE_REGLE("ihm_filtrage_regle", false)
	, IHM_MAPPING_REGLE("ihm_mapping_regle", false)
	, IHM_EXPRESSION("ihm_expression", false)
	, IHM_CLIENT("ihm_client", false)
	, IHM_ENTREPOT("ihm_entrepot", false)
	, IHM_FAMILLE("ihm_famille", false)
	, IHM_MOD_TABLE_METIER("ihm_mod_table_metier", false)
	, IHM_NMCL("ihm_nmcl", false)
	, IHM_SCHEMA_NMCL("ihm_schema_nmcl", false)
	, ID_TABLE_IHM_SEUIL("ihm_seuil", false)
	, PILOTAGE_BATCH("pilotage_batch", false)

	, PILOTAGE_FICHIER("pilotage_fichier", true)
	, PILOTAGE_ARCHIVE("pilotage_archive", true)

	;

	private TableEnum(String tableName, boolean isTableInSanbox) {
		this.tableName = tableName;
		this.isTableInSanbox = isTableInSanbox;
	}

	/**
	 * database real name
	 */
	private String tableName;

	/**
	 * indicate if the table belongs to a sandbox
	 */
	private boolean isTableInSanbox;

	public String getTableName() {
		return tableName;
	}

	public boolean isTableInSanbox() {
		return isTableInSanbox;
	}

}
