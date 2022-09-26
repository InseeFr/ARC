package fr.insee.arc.core.databaseobjetcs;

public enum ColumnEnum {
	
	ID_SOURCE("id_source",TypeEnum.TEXT,"the entry filename contatenated with entry repository")
	, EXPR_NOM("expr_nom",TypeEnum.TEXT,"name of the expression")
	, EXPR_VALEUR("expr_valeur",TypeEnum.TEXT,"value of the expression")
	;
	
	private String columnName;
	private TypeEnum columnType;
	private String columnExplanation;

	private ColumnEnum(String columnName, TypeEnum columnType, String columnExplanation) {
		this.columnName = columnName;
		this.columnType= columnType;
		this.columnExplanation = columnExplanation;
	}

	public String getColumnName() {
		return columnName;
	}
	
	public TypeEnum getColumnType() {
		return columnType;
	}

	public String getColumnExplanation() {
		return columnExplanation;
	}

	
}
