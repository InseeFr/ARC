package fr.insee.arc.utils.dao;

import fr.insee.arc.utils.dataobjects.PgViewEnum;

public class UtilitaireDaoTemporaryTable {
	
	private UtilitaireDaoTemporaryTable() {
		throw new IllegalStateException("Utility class");
	}
	
	// thread temporary pilotage table name
	public static final String TABLE_FAST_UPDATE_CONTAINER = PgViewEnum.TABLE_FAST_UPDATE_CONTAINER.getTableName();
	public static final String TABLE_FAST_UPDATE_IMAGE = PgViewEnum.TABLE_FAST_UPDATE_IMAGE.getTableName();

}
