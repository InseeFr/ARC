package fr.insee.arc.core.databaseobjects;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public enum ColumnEnum {
	
	ID_SOURCE("id_source",TypeEnum.TEXT,"the entry filename contatenated with entry repository")
	, EXPR_NOM("expr_nom",TypeEnum.TEXT,"name of the expression")
	, EXPR_VALEUR("expr_valeur",TypeEnum.TEXT,"value of the expression")
	
	, ID_FAMILLE("id_famille",TypeEnum.INT,"identifier for norm family")
	
	, ID_NORME("id_norme",TypeEnum.INT,"identifier for norm")

	
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
	
	/**
	 * return the list of columnEnum name
	 * @param listOfColumnEnum
	 * @return
	 */
	public static List<String> listColumnEnumByName (Collection<ColumnEnum> listOfColumnEnum)
	{
		return listOfColumnEnum.stream().map(ColumnEnum::getColumnName).collect(Collectors.toList());
	}
	
}
