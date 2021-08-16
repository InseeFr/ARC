package fr.insee.arc.core.util;

/**
 * Enum of the date formats used by ARC
 * Provides the corresponding format between the application level and the datastore level
 * @author FY2QEQ
 *
 */
public enum EDateFormat {
	
    DATE_DASH("yyyy-MM-dd","YYYY-MM-DD"),
;
    
    private String applicationFormat;
    private String datastoreFormat;
   
    private EDateFormat(String applicationFormat, String datastoreFormat) {
		this.applicationFormat = applicationFormat;
		this.datastoreFormat= datastoreFormat;
    }

	public String getApplicationFormat() {
		return applicationFormat;
	}

	public void setApplicationFormat(String applicationFormat) {
		this.applicationFormat = applicationFormat;
	}

	public String getDatastoreFormat() {
		return datastoreFormat;
	}

	public void setDatastoreFormat(String datastoreFormat) {
		this.datastoreFormat = datastoreFormat;
	}

    
}