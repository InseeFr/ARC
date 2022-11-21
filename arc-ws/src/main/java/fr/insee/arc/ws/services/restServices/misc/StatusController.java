package fr.insee.arc.ws.services.restServices.misc;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.arc.utils.webutils.WebUtils;

@RestController

public class StatusController {

    @GetMapping(value = "/healthcheck", produces = "application/json")
    public ResponseEntity<Map<String, Object>> healthCheck() {
    	Map<String,Object> map = new HashMap<>();
    	boolean status = WebUtils.getHealthCheckStatus(map);
		if (!status) {
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
    }


	@GetMapping(value = "/version", produces = "application/json")
	public Map<String, String> version(){
		return WebUtils.fullVersionInformation();
	}

	
}
