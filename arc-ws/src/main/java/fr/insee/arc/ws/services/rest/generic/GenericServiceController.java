package fr.insee.arc.ws.services.rest.generic;

import java.sql.Connection;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.core.util.LoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.ws.services.rest.generic.pojo.GenericPojo;
import fr.insee.arc.ws.services.rest.generic.view.ReturnView;

@RestController
public class GenericServiceController {
	
    private static final Logger LOGGER = LogManager.getLogger(GenericServiceController.class);

    @Autowired
    @Qualifier("properties")
    public  PropertiesHandler properties;

	@Autowired
	private LoggerDispatcher loggerDispatcher;
    
	@RequestMapping(value = "/execute/service/{serviceName}/{serviceId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReturnView> executeEngineClient(
			@PathVariable String serviceName,
			@PathVariable int serviceId,
			@RequestBody(required = true) GenericPojo p
	)
	{
		Date firstContactDate=new Date();
		ReturnView returnView=new ReturnView();
		
		
		String identifiantLog = "(" + serviceName + ", " + serviceId + ")";
		
		loggerDispatcher.info(identifiantLog + " received", LOGGER);
	
		try {
		
		try (Connection c = UtilitaireDao.get("arc").getDriverConnexion()) {
						
			GenericRulesDao.fillRules(c, p, serviceName, serviceId);
			
			StringBuilder requete;
			
			String env = p.sandbox;
			String repertoire = properties.getBatchParametersDirectory();
	
			ApiInitialisationService.synchroniserSchemaExecution(null, "arc.ihm", env);
			
			for (int i = 2; i <= Integer.parseInt(p.targetPhase); i++) {
				ApiServiceFactory.getService(TraitementPhase.getPhase(i).toString(), "arc.ihm", env,
							repertoire, "10000000").invokeApi();
				}
			
			
			GenericRulesDao.buildResponse(c, p, returnView, firstContactDate);

		}
	} catch (Exception e) {
		loggerDispatcher.error(identifiantLog, e, LOGGER);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnView);
	}
	loggerDispatcher.info(identifiantLog + " done", LOGGER);
	return ResponseEntity.status(HttpStatus.OK).body(returnView);		
	
	}

}
