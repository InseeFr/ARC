package fr.insee.arc.core.databaseobjetcs;

public enum ColumnEnum {
	
	ID_SOURCE("id_source","text","the entry filename contatenated with entry repository");
	
	private String columnName;
	private String columnType;
	private String columnExplanation;

	private ColumnEnum(String columnName, String columnType, String columnExplanation) {
		this.columnName = columnName;
		this.columnType= columnType;
		this.columnExplanation = columnExplanation;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getColumnExplanation() {
		return columnExplanation;
	}

	
}
