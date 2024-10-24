package fr.insee.arc.ws.services.restServices.misc;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.arc.core.util.TestLoggers;

@RestController

public class TestLoggersController {

	private static final String ORIGIN="WEB SERVICE";
    
	@RequestMapping(value = "/testLoggers")
	public ResponseEntity<String> sayHello() {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		
		return new ResponseEntity<>("{\"msg\": \""+TestLoggers.sendLoggersTest(ORIGIN)+"\"}", httpHeaders, HttpStatus.OK);
	}
	

	
}
