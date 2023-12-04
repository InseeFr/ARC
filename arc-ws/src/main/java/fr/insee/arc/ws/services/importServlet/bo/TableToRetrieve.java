package fr.insee.arc.ws.services.importServlet.bo;

import fr.insee.arc.core.dataobjects.ArcDatabase;

public class TableToRetrieve {
	
	private ArcDatabase nod;
	
	private String tableName;

	public TableToRetrieve() {
		super();
	}
	
	public TableToRetrieve(String nod, String tableName) {
		super();
		this.nod = ArcDatabase.valueOf(nod);
		this.tableName = tableName;
	}

	public ArcDatabase getNod() {
		return nod;
	}

	public String getTableName() {
		return tableName;
	}
	
}
