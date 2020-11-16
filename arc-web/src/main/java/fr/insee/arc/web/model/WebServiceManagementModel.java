package fr.insee.arc.web.model;

import fr.insee.arc.web.model.viewobjects.ViewWebserviceContext;
import fr.insee.arc.web.model.viewobjects.ViewWebserviceQuery;
import fr.insee.arc.web.util.VObject;

public class WebServiceManagementModel implements ArcModel {

    private VObject viewWebserviceContext;

    private VObject viewWebserviceQuery;
    
    public WebServiceManagementModel() {
    	this.viewWebserviceContext = new ViewWebserviceContext();
    	this.viewWebserviceQuery = new ViewWebserviceQuery();
	}

	public VObject getViewWebserviceContext() {
		return viewWebserviceContext;
	}

	public void setViewWebserviceContext(VObject viewWebserviceContext) {
		this.viewWebserviceContext = viewWebserviceContext;
	}

	public VObject getViewWebserviceQuery() {
		return viewWebserviceQuery;
	}

	public void setViewWebserviceQuery(VObject viewWebserviceQuery) {
		this.viewWebserviceQuery = viewWebserviceQuery;
	}
    
    
	
}
