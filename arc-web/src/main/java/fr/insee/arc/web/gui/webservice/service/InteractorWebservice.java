package fr.insee.arc.web.gui.webservice.service;

import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.webservice.dao.WebserviceDao;
import fr.insee.arc.web.gui.webservice.model.ModelWebservice;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorWebservice extends ArcWebGenericService<ModelWebservice, WebserviceDao> {

	protected static final String RESULT_SUCCESS = "/jsp/gererWebservice.jsp";
	private static final Logger LOGGER = LogManager.getLogger(InteractorWebservice.class);

	@Autowired
    protected ModelWebservice views;
    
	@Override
	public void putAllVObjects(ModelWebservice model) {
		
		views.setViewWebserviceContext(vObjectService.preInitialize(model.getViewWebserviceContext()));
		views.setViewWebserviceQuery(vObjectService.preInitialize(model.getViewWebserviceQuery()));
		
		putVObject(views.getViewWebserviceContext(), t -> initializeWebserviceContext(t));
		putVObject(views.getViewWebserviceQuery(), t -> initializeWebserviceQuery(t, views.getViewWebserviceContext()));
	}
    
    /**
     * Retourne le nom des tables de WebserviceQuery présentes dans la base ainsi que le descriptif associé à chaque table
     */
    public void initializeWebserviceContext(VObject viewWsContext) {
        LoggerHelper.debug(LOGGER, "/* initializeWebserviceContext */");
        dao.initializeWebserviceContext(viewWsContext);

    }

    private void initializeWebserviceQuery(VObject viewWsQuery, VObject viewWsContext) {
        // visual des Calendriers
    	LoggerHelper.debug(LOGGER, "/* initializeWebserviceQuery */");

    	// get the webservice selected records
        Map<String, ArrayList<String>> viewWsContextSelectedRecords = viewWsContext.mapContentSelected();

        // if a webservice is selected, trigger the call to dao to construct query view
        if (!viewWsContextSelectedRecords.isEmpty()) {
        	
        	dao.setSelectedRecords(viewWsContextSelectedRecords);
			dao.initializeWebserviceQuery(viewWsQuery);
			
        } else {
        	vObjectService.destroy(viewWsQuery);
        }
    }


	@Override
	public String getActionName() {
		return "webServiceManagement";
	}

}