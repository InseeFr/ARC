package fr.insee.arc.web.gui.maintenanceoperation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.maintenanceoperation.service.ServiceViewOperation;

@Controller
public class ControllerViewOperation extends ServiceViewOperation {

    @RequestMapping("/secure/selectOperations")
    public String selectOperationsAction(Model model) {
		return selectOperations(model);
    }
	
    @RequestMapping("/secure/generateErrorMessageInLogsOperations")
    public String generateErrorMessageInLogsOperationsAction(Model model) {
    	return generateErrorMessageInLogsOperations(model);
    }
    
}