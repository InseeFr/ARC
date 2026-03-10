package fr.insee.arc.web.gui.famillenorme.ddi.databaseobjects;

/**
 * Énumération des types autorisés dans ARC dont il existe un équivalent dans DDI.
 * 
 * @author Z84H10
 *
 */
public enum ModelVariableTypeEnum {

	/**
	 * Type correspondant à un texte ou une chaîne de caractères. Les types DDI {@code Text} et {@code Code} sont associés à ce type. C'est aussi le type par défaut.
	 */
	TEXT("text")
	/**
	 * Type correspondant à un nombre décimal. Les types DDI {@code Float}, {@code Decimal} et {@code Double} sont associés à ce type.
	 */
	, FLOAT("float")
	/**
	 * Type correspondant à un nombre entier. Les types DDI {@code Integer}, {@code Long} et {@code Short} sont associés à ce type.
	 */
	, BIGINT("bigint")
	/**
	 * Type correspondant à une date et/ou une heure. Le type DDI {@code DateTime} est associé à ce type.
	 */
	, DATE("date")
	;
	// types dont il existe un équivalent DDI
	// autres types : text[], float[], bigint[], date[], boolean, interval, timestamp without time zone
	
	private ModelVariableTypeEnum(String typeName) {
		this.typeName = typeName;
	}

	private String typeName;

	public String getTypeName() {
		return typeName;
	}
	
}
