package fr.insee.arc.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.web.model.WebServiceManagementModel;
import fr.insee.arc.web.util.VObject;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GererWebserviceAction extends ArcAction<WebServiceManagementModel> implements IDbConstant {

	private static final String RESULT_SUCCESS = "/jsp/gererWebservice.jsp";
	private static final Logger LOGGER = LogManager.getLogger(GererWebserviceAction.class);

    private VObject viewWebserviceContext;

    private VObject viewWebserviceQuery;
    
	@Override
	public void putAllVObjects(WebServiceManagementModel model) {
		setViewWebserviceContext(vObjectService.preInitialize(model.getViewWebserviceContext()));
		setViewWebserviceQuery(vObjectService.preInitialize(model.getViewWebserviceQuery()));
		
		putVObject(getViewWebserviceContext(),
				t -> initializeWebserviceContext(t, "arc.ihm_ws_context"));
		//
		putVObject(getViewWebserviceQuery(), t -> initializeWebserviceQuery(t, getViewWebserviceContext(),
				"arc.ihm_ws_query"));
	}
    
   

    /**
     * Retourne le nom des tables de WebserviceQuery présentes dans la base ainsi que le descriptif associé à chaque table
     */
    public void initializeWebserviceContext(VObject c, String t) {
        loggerDispatcher.debug("/* initializeWebserviceContext */", LOGGER);
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();
        StringBuilder requete = new StringBuilder();
        requete.append("\n SELECT * FROM "+t+" ");

        vObjectService.initialize(c, requete.toString(), t, defaultInputFields);

    }

    @RequestMapping("/selectWebserviceContext")
    public String selectWebserviceContext(Model model) {
        return basicAction(model, RESULT_SUCCESS);
    }

    @RequestMapping("/addWebserviceContext")
    public String addWebserviceContext(Model model) {
        this.vObjectService.insert(viewWebserviceContext);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/updateWebserviceContext")
    public String updateWebserviceContext(Model model) {
    	this.vObjectService.update(viewWebserviceContext);
    	return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/sortWebserviceContext")
    public String sortWebserviceContext(Model model) {
        this.vObjectService.sort(viewWebserviceContext);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/deleteWebserviceContext")
    public String deleteWebserviceContext(Model model) {
        this.vObjectService.delete(viewWebserviceContext);
        return generateDisplay(model, RESULT_SUCCESS);
    }


    private void initializeWebserviceQuery(VObject c, VObject d, String t) {
        // visual des Calendriers
        System.out.println("/* initializeWebserviceQuery */");

        Map<String, ArrayList<String>> selection = d.mapContentSelected();

        if (!selection.isEmpty()) {
            HashMap<String, String> type = d.mapHeadersType();

            // requete de la vue
            StringBuilder requete = new StringBuilder();
            requete.append("\n SELECT * FROM "+t+" ");
            requete.append("\n WHERE service_name " + ManipString.sqlEqual(selection.get("service_name").get(0), type.get("service_name")) + " ");
            requete.append("\n AND call_id " + ManipString.sqlEqual(selection.get("call_id").get(0), type.get("call_id")) + " ");
            requete.append("\n ");

            // // construction des valeurs par défaut pour les ajouts
            HashMap<String, String> defaultInputFields = new HashMap<String, String>();
            defaultInputFields.put("service_name", selection.get("service_name").get(0));
            defaultInputFields.put("call_id", selection.get("call_id").get(0));

            this.vObjectService.initialize(c, requete.toString(), "arc.ihm_ws_query", defaultInputFields);
        } else {
        	this.vObjectService.destroy(c);
        }
    }

    @RequestMapping("/selectWebserviceQuery")
    public String selectWebserviceQuery(Model model) {
        return basicAction(model, RESULT_SUCCESS);
    }

    @RequestMapping("/addWebserviceQuery")
    public String addWebserviceQuery(Model model) {
        this.vObjectService.insert(viewWebserviceQuery);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/updateWebserviceQuery")
    public String updateWebserviceQuery(Model model) {
    	this.vObjectService.update(viewWebserviceQuery);
    	return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/sortWebserviceQuery")
    public String sortWebserviceQuery(Model model) {
        this.vObjectService.sort(viewWebserviceQuery);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/deleteWebserviceQuery")
    public String deleteWebserviceQuery(Model model) {
        this.vObjectService.delete(viewWebserviceQuery);
        return generateDisplay(model, RESULT_SUCCESS);
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


	@Override
	public String getActionName() {
		return "webServiceManagement";
	}

}