package fr.insee.arc_essnet.core.util;

public enum EDateFormat {
    SIMPLE_DATE_FORMAT_SQL("yyyy-MM-dd"),
    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd' 'HH:mm:ss"),
    SIMPLE_DATE_FORMAT_IHM("dd/MM/yyyy"),
    DATE_FORMAT_WITH_SECOND("dd/MM/yyyy HH:mm:ss"),
    DATE_FORMAT_WITH_HOUR("yyyy-MM-dd:HH");
;
    
    private String value;
   
    private EDateFormat(String value) {
	this.value = value;
    }


    public String getValue() {
        return value;
    }
    

}
