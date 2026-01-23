package fr.insee.arc.core.service.global.thread;

import fr.insee.arc.core.dataobjects.ViewEnum;

/**
 * This class store the temporary table used in phase thread
 * Main purpose is to avoid the recalculation of the value of getFullName()
 */
public class ThreadTemporaryTable {
	
	private ThreadTemporaryTable() {
		throw new IllegalStateException("Utility class");
	}
	
	// thread temporary pilotage table name
	public static final String TABLE_PILOTAGE_THREAD = ViewEnum.TABLE_PILOTAGE_THREAD.getFullName();
	public static final String TABLE_TEMP_CHARGEMENT_A = ViewEnum.TMP_CHARGEMENT_ARC.getFullName();
	public static final String TABLE_TEMP_CHARGEMENT_B = ViewEnum.TMP_CHARGEMENT_BRUT.getFullName();
	
	public static final String TABLE_NORMAGE_DATA_TEMP = ViewEnum.TMP_NORMAGE_DATA_TEMP.getFullName();
	public static final String TABLE_NORMAGE_OK_TEMP = ViewEnum.TMP_NORMAGE_OK_TEMP.getFullName();
	public static final String TABLE_NORMAGE_KO_TEMP = ViewEnum.TMP_NORMAGE_KO_TEMP.getFullName();
	public static final String TABLE_NORMAGE_RUBRIQUES_DANS_REGLES = ViewEnum.TMP_NORMAGE_RUBRIQUES_DANS_REGLES.getFullName();
	
	public static final String TABLE_CONTROLE_DATA_TEMP = ViewEnum.TMP_CONTROLE_DATA_TEMP.getFullName();
	public static final String TABLE_CONTROLE_MARK_TEMP = ViewEnum.TMP_CONTROLE_MARK_TEMP.getFullName();
	public static final String TABLE_CONTROLE_META_TEMP = ViewEnum.TMP_CONTROLE_META_TEMP.getFullName();
	public static final String TABLE_CONTROLE_ROW_TOTAL_COUNT_TEMP = ViewEnum.TMP_CONTROLE_ROW_TOTAL_COUNT_TEMP.getFullName();
	
	public static final String TABLE_MAPPING_DATA_TEMP = ViewEnum.TMP_MAPPING_DATA_TEMP.getFullName();
	public static final String TABLE_MAPPING_IDS_LINK_TEMP = ViewEnum.TMP_MAPPING_IDS_LINK_TEMP.getFullName();

	
}
