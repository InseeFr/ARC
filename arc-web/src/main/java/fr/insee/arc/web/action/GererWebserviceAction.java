package fr.insee.arc.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.model.IDbConstant;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.web.util.VObject;

@Component
@Results({ @Result(name = "success", location = "/jsp/gererWebservice.jsp"), @Result(name = "index", location = "/jsp/index.jsp") })
public class GererWebserviceAction extends ArcAction implements IDbConstant {

	
	private static final Logger LOGGER = LogManager.getLogger(GererWebserviceAction.class);

	
    @Autowired
    @Qualifier("viewWebserviceContext")
    VObject viewWebserviceContext;

    @Autowired
    @Qualifier("viewWebserviceQuery")
    VObject viewWebserviceQuery;
    
	@Override
	public void putAllVObjects() {
		putVObject(getViewWebserviceContext(),
				t -> initializeWebserviceContext(t, "arc.ihm_ws_context"));
		//
		putVObject(getViewWebserviceQuery(), t -> initializeWebserviceQuery(t, getViewWebserviceContext(),
				"arc.ihm_ws_query"));
	}
    
   

    /**
     * Retourne le nom des tables de WebserviceQuery présentes dans la base ainsi que le descriptif associé à chaque table
     */
    public static void initializeWebserviceContext(VObject c, String t) {
        System.out.println("/* initializeWebserviceContext */");
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();
        StringBuilder requete = new StringBuilder();
        requete.append("\n SELECT * FROM "+t+" ");

        c.initialize(requete.toString(), t, defaultInputFields);

    }

    @Action(value = "/selectWebserviceContext")
    public String selectWebserviceContext() {
        return basicAction();
    }

    @Action(value = "/addWebserviceContext")
    public String addWebserviceContext() {
    	initialize();
        this.viewWebserviceContext.insert();
        return generateDisplay();
    }

    @Action(value = "/updateWebserviceContext")
    public String updateWebserviceContext() {
    	initialize();
    	this.viewWebserviceContext.update();
    	return generateDisplay();
    }

    @Action(value = "/sortWebserviceContext")
    public String sortWebserviceContext() {
    	initialize();
        this.viewWebserviceContext.sort();
        return generateDisplay();
    }

    @Action(value = "/deleteWebserviceContext")
    public String deleteWebserviceContext() {
    	initialize();
        this.viewWebserviceContext.delete();
        return generateDisplay();
    }


    private static void initializeWebserviceQuery(VObject c, VObject d, String t) {
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

            c.initialize(requete.toString(), "arc.ihm_ws_query", defaultInputFields);
        } else {
            c.destroy();
        }
    }

    @Action(value = "/selectWebserviceQuery")
    public String selectWebserviceQuery() {
        return basicAction();
    }

    @Action(value = "/addWebserviceQuery")
    public String addWebserviceQuery() {
    	initialize();
        this.viewWebserviceQuery.insert();
        return generateDisplay();
    }

    @Action(value = "/updateWebserviceQuery")
    public String updateWebserviceQuery() {
    	initialize();
    	this.viewWebserviceQuery.update();
    	return generateDisplay();
    }

    @Action(value = "/sortWebserviceQuery")
    public String sortWebserviceQuery() {
    	initialize();
        this.viewWebserviceQuery.sort();
        return generateDisplay();
    }

    @Action(value = "/deleteWebserviceQuery")
    public String deleteWebserviceQuery() {
    	initialize();
        this.viewWebserviceQuery.delete();
        return generateDisplay();
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
	public void instanciateAllDAOs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProfilsAutorises() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void specificTraitementsPostDAO() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getActionName() {
		// TODO Auto-generated method stub
		return null;
	}

    
    
}
