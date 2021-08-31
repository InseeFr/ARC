package fr.insee.arc.web.action;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.model.TestLoggers;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.web.model.MaintenanceOperationsModel;
import fr.insee.arc.web.util.VObject;


@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MaintenanceOperationsAction extends ArcAction<MaintenanceOperationsModel>  {

	private static final String RESULT_SUCCESS = "/jsp/maintenanceOperations.jsp";

	private static final Logger LOGGER = LogManager.getLogger(MaintenanceOperationsAction.class);

    private VObject viewOperations;

	@Override
	protected void putAllVObjects(MaintenanceOperationsModel arcModel) {
		setViewOperations(this.vObjectService.preInitialize(arcModel.getViewOperations()));
		
		putVObject(getViewOperations(), t -> initializeOperations());
	}

    public void initializeOperations() {
        HashMap<String, String> defaultInputFields = new HashMap<>();
        this.vObjectService.initialize(viewOperations, new PreparedStatementBuilder("SELECT true"),  "arc.operations", defaultInputFields);
    }
    
	private static final String ORIGIN="WEB GUI";

    @RequestMapping("/generateErrorMessageInLogsOperations")
    public String generateErrorMessageInLogsOperations(Model model) {
    	TestLoggers.sendLoggersTest(ORIGIN);
		return generateDisplay(model, RESULT_SUCCESS);
    }
    
    @RequestMapping("/selectOperations")
    public String selectOperations(Model model) {

		return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/addOperations")
    public String addOperations(Model model) {
        this.vObjectService.insert(viewOperations);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/deleteOperations")
    public String deleteOperations(Model model) {
         this.vObjectService.delete(viewOperations);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/updateOperations")
    public String updateOperations(Model model) {
        this.vObjectService.update(viewOperations);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/sortOperations")
    public String sortOperations(Model model) {
        this.vObjectService.sort(viewOperations);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    @RequestMapping("/startOperations")
    public String startOperations(Model model) throws Exception {
          	
    	
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public VObject getViewOperations() {
        return this.viewOperations;
    }

    public void setViewOperations(VObject viewOperations) {
        this.viewOperations = viewOperations;
    }


	@Override
	public String getActionName() {
		return "manageOperations";
	}

    
    
}