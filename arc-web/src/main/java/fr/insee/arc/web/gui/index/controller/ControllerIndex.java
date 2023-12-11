package fr.insee.arc.web.gui.index.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.web.gui.index.service.ServiceIndex;

@Controller
public class ControllerIndex extends ServiceIndex {
	
	@RequestMapping({ "/index" })
	public String indexAction(Model model) {
		return index(model);
	}

	@RequestMapping("/healthcheck")
	public ResponseEntity<Map<String, Object>> healthcheckAction() {
		return healthcheck();
	}

}
