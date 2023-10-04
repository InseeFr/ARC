package fr.insee.arc.utils.exception;

public enum ArcExceptionMessage {

	BATCH_INITIALIZATION_DATE_PARSE_FAILED("Le champ last_init de table de pilotage ne peut être converti en date"),

	FILE_IS_EMPTY("Le fichier %s est vide"),
	FILE_RENAME_FAILED("Le fichier %s n'a pas pu être renommé vers %s "),
	FILE_COPY_FAILED("Le fichier %s n'a pas pu être copié vers %s "),
	FILE_DELETE_FAILED("Le fichier %s n'a pas pu être effacé"),
	FILE_READ_FAILED("Le fichier %s n'a pas pu être lu"),
	FILE_EXTRACT_FAILED("Le fichier archive %s n'a pas pu être extrait"),
	FILE_WRITE_FAILED("Le fichier %s n'a pas pu être écrit"),
	FILE_CLOSE_FAILED("Le fichier %s n'a pas pu être fermé"),
	STREAM_READ_FAILED("Le stream de données n'a pas pu être lu"),
	STREAM_WRITE_FAILED("Le stream de données n'a pas pu être écrit"),
	TGZ_CONVERSION_FAILED("Le fichier %s n'a pu être converti en tgz"),
	
	INVALID_FILE_FORMAT("Format de fichier non pris en charge"),

	
	IMPORTING_JAVA_EXCEPTION_HEADERS_MISSING("ERROR: extra data after last expected column"),
	IMPORTING_JAVA_EXCEPTION_DATA_MISSING("ERROR: missing data for column"),
	IMPORTING_COLUMNS_MISSING("Il manque une ou plusieurs colonnes dans le corps du fichier"),
	IMPORTING_HEADERS_MISSING("Il manque un ou plusieurs entetes dans le fichier"),
	IMPORTING_FAILED("L'import des données a échoué"),

	
	
	XML_KEYVALUE_CONVERSION_FAILED("Le fichier clé/valeur %s n'a pu être converti en xml"),
	XML_SAX_PARSING_FAILED("Le moteur SAX XML n'a pas pu parser le fichier %s"),
	
	JSON_PARSING_FAILED("Le moteur JSON n'a pas pu parser l'élément"),

	LOAD_PARALLEL_INSERT_THREAD_FAILED("L'insertion en parallele des données a échoué"),
	LOAD_KEYVALUE_VAR_NOT_EXISTS_IN_FORMAT_RULES("La rubrique fille %s n'existe pas les règles de formatage"),
	LOAD_RULES_NOT_FOUND("La norme %s n'a pas de règles de chargement"),
	LOAD_SEVERAL_NORM_FOUND("Plusieurs normes ou validité correspondent à l'expression : %s"),
	LOAD_NORM_NOT_FOUND("Aucune norme trouvée pour le fichier %s"),
	LOAD_ZERO_NORM_FOUND("Aucune norme trouvée"),
	
	MAPPING_PRIMARY_KEY_INVALID_FORMAT("La règle de clé primaire pour la variable %s n'est pas de la forme : \"{\"pk:mapping_<famille>_<variable>_ok\"}\""),
	MAPPING_EXPRESSION_INVALID("L'expression \"%s\" est invalide"),
	MAPPING_EXPRESSION_REFERS_NON_EXISTING_TABLES("La règle %s fait référence à des des tables inexistantes"),
	MAPPING_EXPRESSION_GROUP_INVALID("L'expression de groupe %s dans la règle %s est invalide"),
	MAPPING_EXPRESSION_GROUP_MULTI_REFERENCE("L'expression %s comporte plusieurs références au numéro de groupe %s"),
	MAPPING_EXPRESSION_GROUP_ILLEGAL_CALL("Cette méthode ne devrait pas être appelée par la classe RegleMappingGroupe"),
	MAPPING_RULES_NOT_FOUND("Aucune règle de mapping n'a été trouvé dans le jeu de règle"),
	MAPPING_VARIABLE_NOT_FOUND_IN_MODEL("La variable %s n'est pas déclarée dans le modèle de la famille"),
	
	
	NORMAGE_VALIDITE_DATE_PARSE_FAILED ("Le champ validite %s ne peut être converti en date"),
	NORMAGE_INDEPENDANCE_BLOC_INVALID_IDENTIFIER("La rubrique %s n'identifie pas un bloc"),
	NORMAGE_INDEPENDANCE_BLOC_INVALID_FATHER("La rubrique %s n'a pas le même pere que les autres rubriques du bloc"),
	
	
	CONTROLE_XSD_ENUM_EMPTY("L'énumération XSD pour le type de règle de contrôle ENUM_BRUTE ne peut pas être vide"),
	CONTROLE_XSD_RUBRIQUE_RELATION_ALREADY_DEFINED("La relation entre %s et %s est déjà décrite sous forme de séquence dans les règles XSD"),
	CONTROLE_XSD_ALIAS_ALREADY_SET("Un alias %s a déjà été défini pour la colonne %s"),
	CONTROLE_XSD_INVALID_CHILDREN_POSITION("Au moins un élément parmi les enfants de %s a été écrasé parce que les positions sont mal définies"),
	CONTROLE_XSD_INFINITE_LOOP("L'élément %s appartient à une relation cyclique : %s"),
	EXTRACT_XSD_TYPE_UNSOLVED("Le type XSD ne peut pas être determiné : plusieurs types possibles pour le même élémént"),
	
	MULTITHREADING_CLASS_NOT_USEABLE("Ce type de class n'est pas pris en charge dans le multithreading"),
	MULTITHREADING_CONNECTIONS_CLOSE_FAILED("Error in closing threads connections"),
		
	HASH_FAILED("Le hashage de %s a échoué"),
	
	DATABASE_ROLLBACK_FAILED("Le rollback de la base de données a échoué"),
	DATABASE_INITIALISATION_SCRIPT_FAILED("Le script de mis a jour de la base de donnée n'a pu être lu"),
	
	DDI_FAMILY_ALREADY_EXISTS("Les familles présentent dans le DDI existe déjà"),
	DDI_PARSING_FAILED("Le fichier xml DDI n'a pas pu être parsé correctement"),
	
	DATABASE_CONNECTION_FAILED("La connexion a la base de données a échoué"),
	DATABASE_CONNECTION_COORDINATOR_FAILED("La connexion a la base de données coordinator a échoué"),
	DATABASE_CONNECTION_EXECUTOR_FAILED("La connexion a une base de données executor a échoué"),

	
	SQL_DATE_PARSE_FAILED("L'expression %s n'a pas pu être converti au format date java %s"),
	SQL_EXECUTE_FAILED("L'excution de la requête dans la base de données a échoué"),
	
	WS_RETRIEVE_DATA_FAMILY_FORBIDDEN("Vous ne pouvez pas accéder à cette famille de norme"),

	IHM_NMCL_COLUMN_IN_FILE_BUT_NOT_IN_SCHEMA("La colonne %s n'est pas déclarée dans le schéma"),
	IHM_NMCL_COLUMN_IN_SCHEMA_BUT_NOT_IN_FILE("La colonne est déclarée dans le schéma mais absente du fichier"),
	IHM_NMCL_IMPORT_FAILED("Le fichier n'a pu être lu. Il doit être au format csv non compressé, le nom des colonnes en première ligne, le type des colonne en 2ieme ligne."),
	
	GENERIC_BEAN_KEY_VALUE_FAILED("GenericBean keyValue : the set hasn't exactly 2 elements and cannot be mapped to key>value format"),
	GENERIC_BEAN_DUPLICATE_KEY("GenericBean keyValue : duplicate key %s"),
	
	SPRING_BEAN_PROPERTIES_NOTFOUND("Spring bean properties not found. Creating a blank singleton PropertiesHandler to be filled with right attributes."),
	
	GUI_FAMILLENORME_VARIABLE_NULL("Une variable a un nom null"),
	GUI_FAMILLENORME_VARIABLE_NO_TARGET_TABLE("Vous avez oublié de spécifier les tables cibles pour votre variable"), 
	GUI_FAMILLENORME_VARIABLE_ALREADY_EXISTS("La variable existe déjà. Pour la modifier, passez par la ligne correspondante du tableau variable*table.\nAucune variable n'a été ajoutée.\n"),
	GUI_EXPORT_TABLE_NOT_EXISTS("La table n'existe pas")
	
	;
	
	private String message;

	private ArcExceptionMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	/**
	 * format an exception
	 * @param parameters
	 * @return
	 */
	public String formatException(Object...parameters)
	{
		return String.format(this.getMessage(),parameters);
	}

}
