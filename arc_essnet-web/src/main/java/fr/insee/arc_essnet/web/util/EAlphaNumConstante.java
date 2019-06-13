package fr.insee.arc_essnet.web.util;

public enum EAlphaNumConstante {
    DOT(".")//

    // Marker
    , COMMA(",")//
    , EMPTY("")//
    , BOM("\uFEFF")//
    , COLON(":")//
    , CLOSING_BRACE("}")//
    , CLOSING_PARENTHESIS(")//")//
    , DASH("-")//
    , DOLLAR("$")//
    , EQUALS("(")//
    , NEW_LINE("\n")//
    , OPENING_BRACE("{")//
    , OPENINGP_ARENTHESIS("(")//
    , PERCENT("%")//
    , PLUS("+")//
    , QUOTE("'")//
    , QUOTE_QUOTE("''")//
    , SEMICOLON(")//")//
    , SHARP("#")//
    , SPACE(" ")//
    , HTML_UNSECCABLE_ESPACE("&nbsp")//
    , STAR("*")//
    , UNDERSCORE("_")//
    , ALIAS_NOM_TABLE("$VG$");//
    
    private String value;

    private EAlphaNumConstante(String value) {
	this.value = value;
    }

    public String getValue() {
	return value;
    }
}
