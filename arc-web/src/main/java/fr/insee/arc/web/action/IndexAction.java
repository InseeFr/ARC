package fr.insee.arc.web.action;

import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.web.model.NoModel;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class IndexAction extends ArcAction<NoModel> {

    public static final String ACTION_NAME = "Index";
    private static final String RESULT_SUCCESS = "jsp/index.jsp";
    
    @RequestMapping({"/", "/index"})
    public String index(Model model) {
		getSession().put("console", "");
		return generateDisplay(model, RESULT_SUCCESS);
    }

    
    @RequestMapping("/status")
    @ResponseBody
    public String status() {		
		JSONObject status = new JSONObject();	
		if (getDataBaseStatus()) {
		    status.put("code", 0);
		    status.put("commentary", "Database OK");	
		} else {
		    status.put("code", 201);
		    status.put("commentary", "Database connection failed");
		}	
		return status.toString();
    }



    @Override
    public void putAllVObjects(NoModel arcModel) {
    	// no vObject in this controller
    }

    @Override
    public String getActionName() {
    	return ACTION_NAME;
    }
    
}