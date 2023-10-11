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
import fr.insee.arc.ws.services.restServices.execute.operation.ExecuteServiceOperation;
import fr.insee.arc.ws.services.restServices.execute.view.ReturnView;

@RestController
public class ExecuteServiceController {

	private static final Logger LOGGER = LogManager.getLogger(ExecuteServiceController.class);

	@RequestMapping(value = "/execute/service/{serviceName}/{serviceId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReturnView> executeServiceClient(@PathVariable String serviceName,
			@PathVariable int serviceId, @RequestBody(required = true) ExecuteParameterModel bodyPojo) {

		// date, timestamp, identifier
		ResponseAttributes responseAttributes = new ResponseAttributes(serviceName, serviceId);
		// the return view expected by client
		ReturnView returnView = new ReturnView();
		// service instance
		ExecuteServiceOperation service = new ExecuteServiceOperation(returnView, responseAttributes, bodyPojo);

		StaticLoggerDispatcher.info(LOGGER, responseAttributes.getIdentifiantLog() + " received");

		try {
			service.executeServiceClient();

		} catch (ArcException e) {
			StaticLoggerDispatcher.error(LOGGER, responseAttributes.getIdentifiantLog(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnView);
		}
		StaticLoggerDispatcher.info(LOGGER, responseAttributes.getIdentifiantLog() + " done");
		return ResponseEntity.status(HttpStatus.OK).body(returnView);

	}

	@RequestMapping(value = "/execute/service/reset/{env}/{targetPhase}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> resetServiceClient(@PathVariable String env, @PathVariable String targetPhase) {

		StaticLoggerDispatcher.info(LOGGER, "Reset client on sandbox " + env + " to phase " + targetPhase);

		try {

			ExecuteServiceOperation service = new ExecuteServiceOperation();
			service.resetServiceClient(env, targetPhase);

		} catch (ArcException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(HttpStatus.INTERNAL_SERVER_ERROR.toString());
		}

		StaticLoggerDispatcher.info(LOGGER, "reset client in schema done");

		return ResponseEntity.status(HttpStatus.OK).body(HttpStatus.OK.toString());

	}

	@RequestMapping(value = "/execute/service/build/{env}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> buildSandbox(@PathVariable String env) {

		StaticLoggerDispatcher.info(LOGGER, "Build the sandbox file system and database on " + env);

		ExecuteServiceOperation service = new ExecuteServiceOperation();
		service.buildSandbox(env);

		StaticLoggerDispatcher.info(LOGGER, "Build the sandbox file system and database " + env);

		return ResponseEntity.status(HttpStatus.OK).body(HttpStatus.OK.toString());

	}

	@RequestMapping(value = "/execute/service/synchonize/{env}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> synchronizeSandbox(@PathVariable String env) {

		StaticLoggerDispatcher.info(LOGGER, "Synchronize sandbox rules on " + env);
		try {

			ExecuteServiceOperation service = new ExecuteServiceOperation();
			service.synchronizeSandbox(env);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(HttpStatus.INTERNAL_SERVER_ERROR.toString());
		}

		return ResponseEntity.status(HttpStatus.OK).body(HttpStatus.OK.toString());

	}

}
