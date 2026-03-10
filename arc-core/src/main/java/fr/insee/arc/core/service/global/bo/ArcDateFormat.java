package fr.insee.arc.core.service.global.bo;

/**
 * Enum of the date formats used by ARC
 * Provides the corresponding format between the application level and the datastore level
 * @author FY2QEQ
 *
 */
public enum ArcDateFormat {
	
	
	// java to database conversion
    DATE_FORMAT_CONVERSION("yyyy-MM-dd","YYYY-MM-DD"),
    DATE_HOUR_FORMAT_CONVERSION("yyyy-MM-dd:HH", "YYYY-MM-DD:HH24"),
    TIMESTAMP_FORMAT_CONVERSION("dd/MM/yyyy HH:mm:ss", "DD/MM/YYYY HH24:MI:SS"),

    // view presentation
    TIMESTAMP_FORMAT_VIEW("dd/MM/yyyy HH:mm:ss", "YYYY-MM-DD HH24:MI:SS")
;
	
    
    private String applicationFormat;
    private String datastoreFormat;
   
    private ArcDateFormat(String applicationFormat, String datastoreFormat) {
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