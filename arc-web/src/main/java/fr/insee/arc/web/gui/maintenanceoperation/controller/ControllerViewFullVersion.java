package fr.insee.arc.web.gui.maintenanceoperation.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.utils.webutils.WebAttributesName;

@Controller
public class ControllerViewFullVersion {
	
	@RequestMapping("/debug/version")
	public ResponseEntity<Map<String, String>> fullVersionAction(){
		Map<String, String> map = WebAttributesName.fullVersionInformation();
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	
}
