package fr.insee.arc.core.dataobjects;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public enum ColumnEnum {
	
	ID_SOURCE("id_source",TypeEnum.TEXT,"the entry filename contatenated with entry repository")
	, EXPR_NOM("expr_nom",TypeEnum.TEXT,"name of the expression")
	, EXPR_VALEUR("expr_valeur",TypeEnum.TEXT,"value of the expression")
	

	, ID("id",TypeEnum.BIGINT,"serial identifier")

	, ID_FAMILLE("id_famille",TypeEnum.INT,"identifier for norm family")
	, ID_NORME("id_norme",TypeEnum.TEXT,"identifier for norm")
	, PERIODICITE("periodicite",TypeEnum.TEXT,"peridicity for norm")
	, DEF_NORME("def_norme",TypeEnum.TEXT,"sql expression to check if file match the norm")
	, DEF_VALIDITE("def_validite",TypeEnum.TEXT,"sql expression to calculate the date validity of the file")
	, ETAT("etat",TypeEnum.TEXT,"sql expression to calculate the date validity of the file")

	, NOM_TABLE("nom_table",TypeEnum.TEXT,"table name for nomenclature tables")
	, DESCRIPTION("description",TypeEnum.TEXT,"description")
	
	, TYPE_NMCL("type_nmcl",TypeEnum.TEXT,"nomenclature type -nmcl,ext-")
	, NOM_COLONNE("nom_colonne",TypeEnum.TEXT,"column name of nomenclature schema")
	, TYPE_COLONNE("type_colonne",TypeEnum.TEXT,"column type of nomenclature schema")
	
	
	, TEST1("test1",TypeEnum.TEXT,"test column 1")
	, TEST2("test2",TypeEnum.TEXT,"test column 2")
	
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
	
	@Override
	public String toString()
	{
		return this.getColumnName();
	}
	
}
