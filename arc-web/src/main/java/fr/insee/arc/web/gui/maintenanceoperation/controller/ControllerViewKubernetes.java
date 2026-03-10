package fr.insee.arc.web.gui.maintenanceoperation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.maintenanceoperation.service.ServiceViewKubernetes;

@Controller
public class ControllerViewKubernetes extends ServiceViewKubernetes {

    @RequestMapping("/secure/createDatabases")
    public String createDatabasesAction(Model model) {
		return createDatabases(model);
    }
    
    @RequestMapping("/secure/deleteDatabases")
    public String deleteDatabasesAction(Model model) {
		return deleteDatabases(model);
    }

}