package fr.insee.arc.web.webusecases.export.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.webusecases.export.service.ServiceViewFileExport;

@Controller
public class ControllerViewFileExport extends ServiceViewFileExport {

    @RequestMapping({"/selectFileExport", "/seeFileExport"})
    public String selectFileExportAction(Model model) {
        return selectFileExport(model);
    }

    @RequestMapping("/sortFileExport")
    public String sortFileExportAction(Model model) {
        return sortFileExport(model);
    }

    @RequestMapping("/deleteFileExport")
    public String deleteFileExportAction(Model model) {
    	return deleteFileExport(model);
    }
    
    @RequestMapping("/updateFileExport")
    public String updateFileExportAction(Model model) {
    	return updateFileExport(model);
    }
    
    @RequestMapping("/downloadFileExport")
    public String downloadFileExportAction(Model model, HttpServletResponse response) {
    	return downloadFileExport(model, response);
    }	
	
	
}
