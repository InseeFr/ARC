package fr.insee.arc.web.gui.export.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.export.service.ServiceViewExport;

@Controller
public class ControllerViewExport extends ServiceViewExport {

    @RequestMapping("/secure/selectExport")
    public String selectExportAction(Model model) {
		return selectExport(model);
    }

    @RequestMapping("/secure/addExport")
    public String addExportAction(Model model) {
        return addExport(model);
    }

    @RequestMapping("/secure/deleteExport")
    public String deleteExportAction(Model model) {
         return deleteExport(model);
    }

    @RequestMapping("/secure/updateExport")
    public String updateExportAction(Model model) {
        return updateExport(model);
    }

    @RequestMapping("/secure/sortExport")
    public String sortExportAction(Model model) {
    	return sortExport(model);
    }

    @RequestMapping("/secure/startExport")
    public String startExportAction(Model model) {
    	return startExport(model);
	}
    
    @RequestMapping("/secure/startParquetExport")
    public String startParquetExportAction(Model model) {
    	return startParquetExport(model);
	}

}
