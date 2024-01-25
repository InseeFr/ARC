package fr.insee.arc.web.gui.maintenanceoperation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.maintenanceoperation.service.ServiceViewOperation;

@Controller
public class ControllerViewOperation extends ServiceViewOperation {

    @RequestMapping("/secure/generateErrorMessageInLogsOperations")
    public String generateErrorMessageInLogsOperationsAction(Model model) {
    	return generateErrorMessageInLogsOperations(model);
    }
    
    @RequestMapping("/secure/selectOperations")
    public String selectOperationsAction(Model model) {
		return selectOperations(model);
    }

    @RequestMapping("/secure/addOperations")
    public String addOperationsAction(Model model) {
        return addOperations(model);
    }

    @RequestMapping("/secure/deleteOperations")
    public String deleteOperationsAction(Model model) {
        return deleteOperations(model);
    }

    @RequestMapping("/secure/updateOperations")
    public String updateOperationsAction(Model model) {
        return updateOperations(model);
    }

    @RequestMapping("/secure/sortOperations")
    public String sortOperationsAction(Model model) {
        return sortOperations(model);
    }

    @RequestMapping("/secure/startOperations")
    public String startOperationsAction(Model model) {
        return startOperations(model);
    }

    
}