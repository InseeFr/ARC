package fr.insee.arc.web.gui.webservice.model;

import org.springframework.stereotype.Component;

import fr.insee.arc.web.gui.all.model.ArcModel;
import fr.insee.arc.web.gui.all.util.VObject;

@Component
public class ModelWebservice implements ArcModel {

    private VObject viewWebserviceContext;

    private VObject viewWebserviceQuery;
    
    public ModelWebservice() {
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
