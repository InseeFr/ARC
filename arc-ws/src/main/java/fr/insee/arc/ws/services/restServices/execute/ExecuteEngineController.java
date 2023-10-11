package fr.insee.arc.ws.services.restServices.execute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.ws.services.restServices.execute.model.ExecuteParameterModel;
import fr.insee.arc.ws.services.restServices.execute.model.ResponseAttributes;
import fr.insee.arc.ws.services.restServices.execute.operation.ExecuteEngineOperation;
import fr.insee.arc.ws.services.restServices.execute.view.ReturnView;

@RestController
public class ExecuteEngineController {

    private static final Logger LOGGER = LogManager.getLogger(ExecuteEngineController.class);
	
	@RequestMapping(value = "/execute/engine/{serviceName}/{serviceId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReturnView> executeEngineClient(
			@PathVariable String serviceName,
			@PathVariable int serviceId,
			@RequestBody(required = true) ExecuteParameterModel bodyPojo
	)
	{

		 // date, timestamp, identifier
		ResponseAttributes responseAttributes = new ResponseAttributes(serviceName, serviceId);
		// the return view expected by client
		ReturnView returnView = new ReturnView();
		// service instance
		ExecuteEngineOperation service = new ExecuteEngineOperation(returnView, responseAttributes, bodyPojo);
		
		StaticLoggerDispatcher.info(LOGGER, responseAttributes.getIdentifiantLog() + " launching phases");
		
		try {
		
			service.executeEngineClient();

		} catch (ArcException e) {
			StaticLoggerDispatcher.error(LOGGER, responseAttributes.getIdentifiantLog(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnView);
		}
		StaticLoggerDispatcher.info(LOGGER, responseAttributes.getIdentifiantLog() + " done");
		return ResponseEntity.status(HttpStatus.OK).body(returnView);

	}
	
	@RequestMapping(value = "/execute/engine/{serviceName}/{serviceId}/{sandbox}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReturnView> executeEngineClient(
			@PathVariable String serviceName,
			@PathVariable int serviceId,
			@PathVariable int sandbox,
			@RequestBody(required = true) ExecuteParameterModel p
	)
	{
		p.sandbox="arc_"+sandbox;
		return  executeEngineClient(serviceName,serviceId,p);
	}

}