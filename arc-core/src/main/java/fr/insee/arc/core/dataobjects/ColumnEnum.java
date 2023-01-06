package fr.insee.arc.core.dataobjects;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public enum ColumnEnum {

	ID_SOURCE("id_source", TypeEnum.TEXT, "the entry filename contatenated with entry repository"),
	EXPR_NOM("expr_nom", TypeEnum.TEXT, "name of the expression"),
	EXPR_VALEUR("expr_valeur", TypeEnum.TEXT, "value of the expression")

	, ID("id", TypeEnum.BIGINT, "serial identifier")

	, ID_FAMILLE("id_famille", TypeEnum.INT, "identifier for norm family"),
	ID_NORME("id_norme", TypeEnum.TEXT, "identifier for norm"),
	PERIODICITE("periodicite", TypeEnum.TEXT, "periodicity for norm"),
	DEF_NORME("def_norme", TypeEnum.TEXT, "sql expression to check if file match the norm"),
	DEF_VALIDITE("def_validite", TypeEnum.TEXT, "sql expression to calculate the date validity of the file"),
	ETAT("etat", TypeEnum.TEXT, "sql expression to calculate the date validity of the file")

	, NOM_TABLE("nom_table", TypeEnum.TEXT, "table name for nomenclature tables"),
	DESCRIPTION("description", TypeEnum.TEXT, "description")

	, TYPE_NMCL("type_nmcl", TypeEnum.TEXT, "nomenclature type -nmcl,ext-"),
	NOM_COLONNE("nom_colonne", TypeEnum.TEXT, "column name of nomenclature schema"),
	TYPE_COLONNE("type_colonne", TypeEnum.TEXT, "column type of nomenclature schema")

	, VALIDITE_INF("validite_inf", TypeEnum.DATE, "first date of validity"),
	VALIDITE_SUP("validite_sup", TypeEnum.DATE, "last date of validity")

	, VERSION("version", TypeEnum.TEXT, "version of the ruleset"),
	DATE_PRODUCTION("date_production", TypeEnum.DATE, "date of production for the ruleset"),
	DATE_INACTIF("date_inactif", TypeEnum.DATE, "first date of inactivity for the ruleset")

	, ID_REGLE("id_regle", TypeEnum.TEXT, "identifier for ruleset"),
	TYPE_FICHIER("type_fichier", TypeEnum.TEXT, "type of file loaded"),
	DELIMITER("delimiter", TypeEnum.TEXT, "delimiter used in the loaded file"),
	FORMAT("format", TypeEnum.TEXT, "format used in the loaded file"),
	COMMENTAIRE("commentaire", TypeEnum.TEXT, "comment on the loaded file")

	, ID_CLASSE("id_classe", TypeEnum.TEXT, "identifier for class"), RUBRIQUE("rubrique", TypeEnum.TEXT, "") // expliquer
																												// la
																												// colonne
	, RUBRIQUE_NMCL("rubrique_nmcl", TypeEnum.TEXT, "") // expliquer la colonne
	, TODO("todo", TypeEnum.TEXT, "") // expliquer la colonne

	, RUBRIQUE_PERE("rubrique_pere", TypeEnum.TEXT, "") // expliquer la colonne
	, RUBRIQUE_FILS("rubrique_fils", TypeEnum.TEXT, "") // expliquer la colonne
	, BORNE_INF("borne_inf", TypeEnum.TEXT, "infimum") // compléter
	, BORNE_SUP("borne_sup", TypeEnum.TEXT, "supremum") // compléter
	, CONDITION("condition", TypeEnum.TEXT, "condition on which to control the data"),
	PRE_ACTION("pre_action", TypeEnum.TEXT, "action to take before control"), XSD_ORDRE("xsd_ordre", TypeEnum.INT, "") // expliquer
																														// la
																														// colonne
	, XSD_LABEL_FILS("xsd_label_fils", TypeEnum.TEXT, "") // expliquer la colonne
	, XSD_ROLE("xsd_role", TypeEnum.TEXT, "") // expliquer la colonne
	, BLOCKING_THRESHOLD("blocking_threshold", TypeEnum.TEXT, "") // expliquer la colonne
	, ERROR_ROW_PROCESSING("error_row_processing", TypeEnum.TEXT, "") // expliquer la colonne

	, MODULE_ORDER("module_order", TypeEnum.INT, "index of rules module")
	, MODULE_NAME("module_name", TypeEnum.TEXT, "name of rules module")
	
	, TEST1("test1", TypeEnum.TEXT, "test column 1"), TEST2("test2", TypeEnum.TEXT, "test column 2")

	;

	private String columnName;
	private TypeEnum columnType;
	private String columnExplanation;

	private ColumnEnum(String columnName, TypeEnum columnType, String columnExplanation) {
		this.columnName = columnName;
		this.columnType = columnType;
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
	 * 
	 * @param listOfColumnEnum
	 * @return
	 */
	public static List<String> listColumnEnumByName(Collection<ColumnEnum> listOfColumnEnum) {
		return listOfColumnEnum.stream().map(ColumnEnum::getColumnName).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return this.getColumnName();
	}

}
