package fr.insee.arc.core.service.p4controle.bo;

/**
 * code des types de controle proposés par ARC 
 * @author FY2QEQ
 *
 */
public enum ControleTypeCode {
	NUM, DATE, ALPHANUM, CARDINALITE, CONDITION, REGEXP, ENUM_BRUTE, ENUM_TABLE;

	public static ControleTypeCode getEnum(String code) {
		switch (code) {
        case "NUM":
            return NUM;
        case "DATE":
            return DATE;
        case "ALPHANUM":
            return ALPHANUM;
        case "CARDINALITE":
            return CARDINALITE;
        case "CONDITION":
        	return CONDITION;
        case "REGEXP":
            return REGEXP;
        case "ENUM_BRUTE":
            return ENUM_BRUTE;
        case "ENUM_TABLE":
        	return ENUM_TABLE;
        default:
            return null;
		}
	}

}
