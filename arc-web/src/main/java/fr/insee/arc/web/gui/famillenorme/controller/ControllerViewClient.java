package fr.insee.arc.web.gui.famillenorme.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.famillenorme.service.ServiceViewClient;

@Controller
public class ControllerViewClient extends ServiceViewClient {

	@RequestMapping("/selectClient")
	public String selectClientAction(Model model) {
		return selectClient(model);
	}

	@RequestMapping("/addClient")
	public String addClientAction(Model model) {
		return addClient(model);
	}

	@RequestMapping("/deleteClient")
	public String deleteClientAction(Model model) {
		return deleteClient(model);
	}

	@RequestMapping("/updateClient")
	public String updateClientAction(Model model) {
		return updateClient(model);
	}

	@RequestMapping("/sortClient")
	public String sortClientAction(Model model) {
		return sortClient(model);
	}
}
