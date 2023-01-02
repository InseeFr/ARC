package fr.insee.arc.web.gui.maintenanceoperation.service;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.core.model.TestLoggers;


@Service
public class ServiceViewOperation extends InteractorMaintenanceOperations {
    
	private static final String ORIGIN="WEB GUI";

    public String generateErrorMessageInLogsOperations(Model model) {
    	TestLoggers.sendLoggersTest(ORIGIN);
		return generateDisplay(model, RESULT_SUCCESS);
    }
    
    public String selectOperations(Model model) {

		return generateDisplay(model, RESULT_SUCCESS);
    }

    public String addOperations(Model model) {
        this.vObjectService.insert(views.getViewOperations());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String deleteOperations(Model model) {
         this.vObjectService.delete(views.getViewOperations());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String updateOperations(Model model) {
        this.vObjectService.update(views.getViewOperations());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String sortOperations(Model model) {
        this.vObjectService.sort(views.getViewOperations());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String startOperations(Model model) {
        return generateDisplay(model, RESULT_SUCCESS);
    }

    
}