package fr.insee.arc.utils.dao;

public enum SQL {
	INSERT_INTO(" INSERT INTO "),ON_CONFLICT_DO_NOTHING (" ON CONFLICT DO NOTHING "),BEGIN("BEGIN; "),END("END; ");
	
	private String sqlCode;

	private SQL(String sqlCode) {
		this.sqlCode = sqlCode;
	}

	public String getSqlCode() {
		return sqlCode;
	}

	@Override
	public String toString()
	{
		return this.sqlCode;
	}
	
	
}
