package fr.insee.arc.web.gui.famillenorme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.famillenorme.service.ServiceViewClient;

@Controller
public class ControllerViewClient extends ServiceViewClient {

	@RequestMapping("/secure/selectClient")
	public String selectClientAction(Model model) {
		return selectClient(model);
	}

	@RequestMapping("/secure/addClient")
	public String addClientAction(Model model) {
		return addClient(model);
	}

	@RequestMapping("/secure/deleteClient")
	public String deleteClientAction(Model model) {
		return deleteClient(model);
	}

	@RequestMapping("/secure/updateClient")
	public String updateClientAction(Model model) {
		return updateClient(model);
	}

	@RequestMapping("/secure/sortClient")
	public String sortClientAction(Model model) {
		return sortClient(model);
	}
}
