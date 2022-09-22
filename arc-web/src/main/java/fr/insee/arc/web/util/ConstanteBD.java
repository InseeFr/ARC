package fr.insee.arc.web.util;

public enum ConstanteBD {
    ARC_PROD("arc.prod")//
    , VALIDITE_SUP("validite_sup")//
    , VALIDITE_INF("validite_inf")//
    , PERIODICITE("periodicite")//
    , ID_NORME("id_norme")//
    , VERSION("version")//
    , ID_FAMILY("id_famille")//
    , ID_REGLE("id_regle")//
    ;

    private String value;

    private ConstanteBD(String value) {
	this.value = value;
    }

    public String getValue() {
	return value;
    }

}
