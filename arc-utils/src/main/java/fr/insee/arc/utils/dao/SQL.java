package fr.insee.arc.utils.dao;

public enum SQL {
	SELECT("SELECT"), FROM("FROM"), WHERE("WHERE"), ORDER_BY("ORDER BY"), DISTINCT("DISTINCT"),

	AS("AS"),
	
	COMMIT("COMMIT"),

	INSERT_INTO("INSERT INTO"), DELETE ("DELETE FROM"),
	
	UPDATE("UPDATE"), SET("SET"),

	ON_CONFLICT_DO_NOTHING("ON CONFLICT DO NOTHING"),

	BEGIN("BEGIN;"), END("END;"),

	AND("AND"), OR("OR"), UNION("UNION"), UNION_ALL("UNION ALL"), IN("IN"), NOT("NOT"),
	
	LIMIT("LIMIT"), OFFSET("OFFSET"),

	CREATE("CREATE"), DROP("DROP"), TRUNCATE("TRUNCATE"),
	
	TEMPORARY("TEMPORARY"), TABLE("TABLE"), IF_NOT_EXISTS("IF NOT EXISTS"), IF_EXISTS("IF EXISTS"), CASCADE("CASCADE"),
	
	LIKE("LIKE"),
	
	IS_NULL("IS NULL"), IS_NOT_NULL("IS NOT NULL"),
	
	GROUP_BY("GROUP BY"),
	
	ALTER("ALTER"), RENAME_TO("RENAME TO"),
	
	TRUE("TRUE"), FALSE("FALSE"),
	
	EXISTS("EXISTS"), 
	
	UNNEST("UNNEST"),
	
	VALUES("VALUES"),
	
	SPACE(" "),
	
	// symbol
	END_QUERY(";", false), BR(System.lineSeparator(), false), CAST_OPERATOR("::", false), COMMA(",", false), DOT(".",false)
	
	;

	private String sqlCode;

	private boolean escapeWithSpace;

	private SQL(String sqlCode, boolean escapeWithSpace) {
		this.sqlCode = sqlCode;
		this.escapeWithSpace = escapeWithSpace;
	}

	private SQL(String sqlCode) {
		this.sqlCode = sqlCode;
		this.escapeWithSpace = true;
	}

	public String getSqlCode() {
		return sqlCode;
	}

	@Override
	public String toString() {
		if (escapeWithSpace) {
			return SPACE.sqlCode + this.sqlCode + SPACE.sqlCode;
		} else {
			return this.sqlCode;
		}
	}

}
