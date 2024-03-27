package fr.insee.arc.utils.database;

public class TableToRetrieve {

	private ArcDatabase nod;
	
	private String tableName;

	public TableToRetrieve() {
		super();
	}

	public TableToRetrieve(ArcDatabase nod, String tableName) {
		super();
		this.nod = nod;
		this.tableName = tableName;
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

	public void setNod(ArcDatabase nod) {
		this.nod = nod;
	}
	
}
