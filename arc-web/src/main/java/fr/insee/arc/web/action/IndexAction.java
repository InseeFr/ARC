package fr.insee.arc.web.action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.web.dao.IndexDao;
import fr.insee.arc.web.model.NoModel;
import fr.insee.arc.web.model.SessionParameters;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class IndexAction extends ArcAction<NoModel> {

    private static final String ACTION_NAME = "Index";
    private static final String RESULT_SUCCESS = "jsp/index.jsp";
    
    @SuppressWarnings("unchecked")
    @RequestMapping({"/", "/index"})
    public String index(Model model) {
    	
    	// run the initialization script
		ApiInitialisationService.bddScript(null);

		// adding production sandbox to session
		this.envMap=IndexDao.getSandboxList();
		getSession().put(SessionParameters.ENV_MAP, this.envMap);

		if (this.bacASable == null) {
			// by default bacASable is the first element of the linkedhashmap
			List<String> keys=new ArrayList<>(((LinkedHashMap<String,String>) this.envMap).keySet());
			this.bacASable = keys.get(0);
		}
    	
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