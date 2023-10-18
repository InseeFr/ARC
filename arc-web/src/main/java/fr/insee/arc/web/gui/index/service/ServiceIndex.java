package fr.insee.arc.web.gui.index.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.utils.webutils.WebUtils;

@Service
public class ServiceIndex extends IndexAction {
  
	
	public String index(Model model) {
		getSession().put("console", "");
		return generateDisplay(model, RESULT_SUCCESS);
	}
	
	public String selectIndex(Model model) {
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public ResponseEntity<Map<String, Object>> healthcheck() {
		Map<String, Object> map = new HashMap<>();
		boolean status = WebUtils.getHealthCheckStatus(map);
		if (!status) {
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);

	}
	
}
