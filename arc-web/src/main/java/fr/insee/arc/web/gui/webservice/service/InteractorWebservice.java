package fr.insee.arc.web.gui.webservice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.webservice.model.ModelWebservice;
import fr.insee.arc.web.util.VObject;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorWebservice extends ArcWebGenericService<ModelWebservice> implements IDbConstant {

	protected static final String RESULT_SUCCESS = "/jsp/gererWebservice.jsp";
	private static final Logger LOGGER = LogManager.getLogger(InteractorWebservice.class);

	@Autowired
    protected ModelWebservice views;
    
	@Override
	public void putAllVObjects(ModelWebservice model) {
		views.setViewWebserviceContext(vObjectService.preInitialize(model.getViewWebserviceContext()));
		views.setViewWebserviceQuery(vObjectService.preInitialize(model.getViewWebserviceQuery()));
		
		putVObject(views.getViewWebserviceContext(),
				t -> initializeWebserviceContext(t, "arc.ihm_ws_context"));
		//
		putVObject(views.getViewWebserviceQuery(), t -> initializeWebserviceQuery(t, views.getViewWebserviceContext(),
				"arc.ihm_ws_query"));
	}
    
   

    /**
     * Retourne le nom des tables de WebserviceQuery présentes dans la base ainsi que le descriptif associé à chaque table
     */
    public void initializeWebserviceContext(VObject c, String t) {
        loggerDispatcher.debug("/* initializeWebserviceContext */", LOGGER);
        HashMap<String, String> defaultInputFields = new HashMap<>();
        
        
        ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
        requete.append("\n SELECT * FROM "+t+" ");
        
        vObjectService.initialize(c, requete, t, defaultInputFields);

    }



    private void initializeWebserviceQuery(VObject c, VObject d, String t) {
        // visual des Calendriers
    	loggerDispatcher.debug("/* initializeWebserviceQuery */",LOGGER);

        Map<String, ArrayList<String>> selection = d.mapContentSelected();

        if (!selection.isEmpty()) {
            HashMap<String, String> type = d.mapHeadersType();

            // requete de la vue
            ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
            requete.append("\n SELECT * FROM "+t+" ");
            requete.append("\n WHERE service_name " + requete.sqlEqual(selection.get("service_name").get(0), type.get("service_name")) + " ");
            requete.append("\n AND call_id " + requete.sqlEqual(selection.get("call_id").get(0), type.get("call_id")) + " ");
            requete.append("\n ");

            // // construction des valeurs par défaut pour les ajouts
            HashMap<String, String> defaultInputFields = new HashMap<String, String>();
            defaultInputFields.put("service_name", selection.get("service_name").get(0));
            defaultInputFields.put("call_id", selection.get("call_id").get(0));

            this.vObjectService.initialize(c, requete, "arc.ihm_ws_query", defaultInputFields);
        } else {
        	this.vObjectService.destroy(c);
        }
    }


	@Override
	public String getActionName() {
		return "webServiceManagement";
	}

}