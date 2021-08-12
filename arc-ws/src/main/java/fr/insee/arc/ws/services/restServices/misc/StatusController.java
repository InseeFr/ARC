package fr.insee.arc.ws.services.restServices.misc;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.webutils.WebUtils;

@RestController

public class StatusController {

    @Autowired
    private PropertiesHandler properties;

    @GetMapping(value = "/healthcheck", produces = "application/json")
    public ResponseEntity<Map<String, Object>> healthCheck() {
    	Map<String,Object> map = WebUtils.getHealthCheckStatus();
		map.put("version", properties.getVersion());
		if (!map.getOrDefault("status", "").equals("up")) {
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
        return new ResponseEntity<>(map, HttpStatus.OK);
    }


	@GetMapping(value = "/version", produces = "application/json")
	public Map<String, String> version(){
		return properties.fullVersionInformation();
	}

	
}
