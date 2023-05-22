package fr.insee.arc.utils.dao;

public enum SQL {
	SELECT("SELECT"), FROM("FROM"), WHERE("WHERE"), ORDER_BY("ORDER BY"),

	AS("AS"),

	INSERT_INTO("INSERT INTO"),

	ON_CONFLICT_DO_NOTHING("ON CONFLICT DO NOTHING"),

	BEGIN("BEGIN;"), END("END;"),

	AND("AND"), UNION_ALL("UNION ALL"),

	CREATE("CREATE"), DROP("DROP"), TEMPORARY("TEMPORARY"), TABLE("TABLE"), IF_EXISTS("IF EXISTS"), CASCADE("CASCADE"),

	// symbol
	END_QUERY(";", false), BR(System.lineSeparator(), false), CAST_OPERATOR("::", false), COMMA(",", false)

	;

	private String sqlCode;

	private boolean escapeWithSpace;

	private static final String SPACE = " ";

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
			return SPACE + this.sqlCode + SPACE;
		} else {
			return this.sqlCode;
		}
	}

}
