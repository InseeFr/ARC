package fr.insee.arc.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.model.TestLoggers;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.web.model.MaintenanceOperationsModel;
import fr.insee.arc.web.util.VObject;


@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MaintenanceOperationsAction extends ArcAction<MaintenanceOperationsModel>  {

	private static final String RESULT_SUCCESS = "/jsp/maintenanceOperations.jsp";

	private static final Logger LOGGER = LogManager.getLogger(MaintenanceOperationsAction.class);

	private static final String IHM_CLIENT = "ihmClient";
	private static final String IHM_CLIENTS = "ihmClients";

    private VObject viewOperations;
    private List<String> ihmClients;
    private String ihmClient;

	@Override
	protected void putAllVObjects(MaintenanceOperationsModel arcModel) {
		setViewOperations(this.vObjectService.preInitialize(arcModel.getViewOperations()));
		putVObject(getViewOperations(), t -> initializeOperations());
		setIhmClients(UtilitaireDao.get("arc").getList(null, "SELECT DISTINCT id_application FROM arc.ihm_client", new ArrayList<>()));
		setIhmClient(arcModel.getIhmClient() == null ? "" : arcModel.getIhmClient());
	}

	public void initializeOperations() {
        HashMap<String, String> defaultInputFields = new HashMap<>();
        this.vObjectService.initialize(viewOperations, new PreparedStatementBuilder("SELECT true"),  "arc.operations", defaultInputFields);
        
    }
	
	@Override
    public void extraModelAttributes(Model model) {
        model.addAttribute(IHM_CLIENT, ihmClient);
        model.addAttribute(IHM_CLIENTS, ihmClients);
    }
	
	private static final String ORIGIN="WEB GUI";

    @RequestMapping("/generateErrorMessageInLogsOperations")
    public String generateErrorMessageInLogsOperations(Model model) {
    	TestLoggers.sendLoggersTest(ORIGIN);
		return generateDisplay(model, RESULT_SUCCESS);
    }
    
    @RequestMapping("/deleteLastImportRequestOperations")
    public String deleteLastImportRequestOperations(@ModelAttribute("ihmUser") String ihmUser, Model model) {
        System.out.println(ihmUser);
        
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

    
    /**
     * @return the ihmClients
     */
	@ModelAttribute("ihmClients")
    public List<String> getIhmClients() {
        return ihmClients;
    }

    
    /**
     * @param ihmClients the ihmClients to set
     */
    public void setIhmClients(List<String> ihmClients) {
        this.ihmClients = ihmClients;
    }

    
    /**
     * @return the ihmClient
     */

    public String getIhmClient() {
        return ihmClient;
    }

    
    /**
     * @param ihmClient the ihmClient to set
     */
    public void setIhmClient(String ihmClient) {
        this.ihmClient = ihmClient;
    }

//    public VObject getViewIhmClient() {
//        return this.viewIhmClient;
//    }

//    public void setViewIhmClient(VObject viewIhmClient) {
//        this.viewIhmClient = viewIhmClient;
//    }

    
    
}