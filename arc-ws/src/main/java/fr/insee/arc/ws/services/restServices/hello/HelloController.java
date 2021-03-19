package fr.insee.arc.ws.services.restServices.hello;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.utils.LoggerHelper;

@RestController

public class HelloController {

    private static final Logger LOGGER = LogManager.getLogger(HelloController.class);

    @Autowired
    private PropertiesHandler properties;
	
	@RequestMapping(value = "/hello")
	public ResponseEntity<String> sayHello() {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		LoggerHelper.info(LOGGER, "Hello from ARC had been sent");
		
		return new ResponseEntity<>("{\"msg\": \"Hello from ARC\"}", httpHeaders, HttpStatus.OK);
	}
	

	@GetMapping(value = "/version", produces = "application/json")
	public Map<String, String> version(){
		Map<String,String> map = new HashMap<>();
		map.put("version", properties.getVersion());
		map.put("buildDate", properties.getVersionDate());
		return map;
	}

	
}
