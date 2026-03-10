package fr.insee.arc.web.gui.nomenclature.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import fr.insee.arc.web.gui.nomenclature.service.ServiceViewListNomenclatures;

@Controller
public class ControllerViewListNomenclatures extends ServiceViewListNomenclatures {

    @RequestMapping("/secure/selectListNomenclatures")
    public String selectListNomenclaturesAction(Model model) {
        return selectListNomenclatures(model);
    }

    @RequestMapping("/secure/addListNomenclatures")
    public String addListNomenclaturesAction(Model model) {
		return addListNomenclatures(model);
    }

    @RequestMapping("/secure/updateListNomenclatures")
    public String updateListNomenclaturesAction(Model model) {
        return updateListNomenclatures(model);
    }

    @RequestMapping("/secure/sortListNomenclatures")
    public String sortListNomenclaturesAction(Model model) {
        return sortListNomenclatures(model);
    }

    @RequestMapping("/secure/deleteListNomenclatures")
    public String deleteListNomenclaturesAction(Model model) {
        return deleteListNomenclatures(model);
    }

    @RequestMapping("/secure/downloadListNomenclatures")
    public String downloadListNomenclaturesAction(Model model, HttpServletResponse response) {
    	return downloadListNomenclatures(model, response);
    }

    @RequestMapping("/secure/importListNomenclatures")
    public String importListNomenclaturesAction(Model model, MultipartFile fileUpload) {    	
    	return importListNomenclatures(model, fileUpload);
    }

}
