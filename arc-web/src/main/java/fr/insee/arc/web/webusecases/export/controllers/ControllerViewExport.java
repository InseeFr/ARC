package fr.insee.arc.web.webusecases.export.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ControllerViewExport extends HubControllerExport {

    @RequestMapping("/selectExport")
    public String selectExportAction(Model model) {
		return selectExport(model);
    }

    @RequestMapping("/addExport")
    public String addExportAction(Model model) {
        return addExport(model);
    }

    @RequestMapping("/deleteExport")
    public String deleteExportAction(Model model) {
         return deleteExport(model);
    }

    @RequestMapping("/updateExport")
    public String updateExportAction(Model model) {
        return updateExport(model);
    }

    @RequestMapping("/sortExport")
    public String sortExportAction(Model model) {
    	return sortExport(model);
    }

    @RequestMapping("/startExport")
    public String startExportAction(Model model) {
    	return startExport(model);
	}
    
	
}
