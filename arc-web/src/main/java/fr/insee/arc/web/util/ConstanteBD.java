package fr.insee.arc.web.util;

public enum ConstanteBD {
    ARC_PROD("arc.prod")//
    , VALIDITE_SUP("validite_sup")//
    , VALIDITE_INF("validite_inf")//
    , PERIODICITE("periodicite")//
    , ID_NORME("id_norme")//
    , VERSION("version")//
    , STATE("etat")//
    , ID_SOURCE("id_source")//
    , ID("id")//
    , CONTROL("controle")//
    , BROKENRULES("brokenrules")//
    , VALIDITY("validite")//
    , ID_FAMILY("id_famille")//
    , RUBRIQUE_NMCL("rubrique_nmcl")//
    , COMMENTAIRE("commentaire")//
    , ID_CLASS("id_classe")//
    , RUBRIQUE_PERE("rubrique_pere")//
    , RUBRIQUE_FILS("rubrique_fils")//
    , BORNE_INF("borne_inf")//
    , BORNE_SUP("borne_sup")//
    , CONDITION("condition")//
    , PRE_ACTION("pre_action")//
    , ID_REGLE("id_regle")//
    , EXPR_REGLE_FILTRE("expr_regle_filtre")//
    , EXPR_REGLE_COL("expr_regle_col")//
    , VARIABLE_SORTIE("variable_sortie")
    , TYPE_FICHIER ("type_fichier")
    , DELIMITER ("delimiter")
    , FORMAT("format");

    private String value;

    private ConstanteBD(String value) {
	this.value = value;
    }

    public String getValue() {
	return value;
    }

}
