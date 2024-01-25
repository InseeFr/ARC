package fr.insee.arc.web.gui.export.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.export.service.ServiceViewFileExport;

@Controller
public class ControllerViewFileExport extends ServiceViewFileExport {

    @RequestMapping({"/secure/selectFileExport", "/secure/seeFileExport"})
    public String selectFileExportAction(Model model) {
        return selectFileExport(model);
    }

    @RequestMapping("/secure/sortFileExport")
    public String sortFileExportAction(Model model) {
        return sortFileExport(model);
    }

    @RequestMapping("/secure/deleteFileExport")
    public String deleteFileExportAction(Model model) {
    	return deleteFileExport(model);
    }
    
    @RequestMapping("/secure/updateFileExport")
    public String updateFileExportAction(Model model) {
    	return updateFileExport(model);
    }
    
    @RequestMapping("/secure/downloadFileExport")
    public String downloadFileExportAction(Model model, HttpServletResponse response) {
    	return downloadFileExport(model, response);
    }	
	
	
}
