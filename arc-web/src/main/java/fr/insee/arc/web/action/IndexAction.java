package fr.insee.arc.web.action;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.insee.arc.web.model.NoModel;

@Controller
public class IndexAction extends ArcAction<NoModel> {

    private static final String ACTION_NAME = "Index";
    private static final String RESULT_SUCCESS = "jsp/index.jsp";
    
    @RequestMapping({"/", "/index"})
    public String index() {
		getSession().put("console", "");
		return generateDisplay(RESULT_SUCCESS);
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