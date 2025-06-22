package fr.insee.arc.ws.services.restServices.misc;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.arc.utils.webutils.WebAttributesName;

@RestController

public class StatusController {

    @GetMapping(value = "/healthcheck", produces = "application/json")
    public ResponseEntity<Map<String, Object>> healthCheck() {
    	Map<String,Object> map = new HashMap<>();
    	boolean status = WebAttributesName.getHealthCheckStatus(map);
		if (!status) {
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(map, HttpStatus.OK);
    }

	@GetMapping(value = "/version", produces = "application/json")
	public ResponseEntity<Map<String, String>> version(){
		return new ResponseEntity<>(WebAttributesName.lightVersionInformation(), HttpStatus.OK);
	}
    
    /**
     * Controller to check if security is up for uri with execute/**
     * @return
     */
    @GetMapping(value = "/execute/healthcheck", produces = "application/json")
    public ResponseEntity<Map<String, Object>> healthCheckExecute() {
		return healthCheck();
    }


}
