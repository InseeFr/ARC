package fr.insee.arc.ws.services.restServices.execute;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p0initialisation.ResetEnvironmentService;
import fr.insee.arc.core.service.p0initialisation.dbmaintenance.BddPatcher;
import fr.insee.arc.core.service.p0initialisation.filesystem.BuildFileSystem;
import fr.insee.arc.core.service.p0initialisation.metadata.SynchronizeRulesAndMetadataOperation;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.core.util.LoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.ws.services.restServices.execute.pojo.ExecuteParameterPojo;
import fr.insee.arc.ws.services.restServices.execute.pojo.ExecuteQueryPojo;
import fr.insee.arc.ws.services.restServices.execute.view.ReturnView;

@RestController
public class ExecuteServiceController {
	
    private static final Logger LOGGER = LogManager.getLogger(ExecuteServiceController.class);

	@Autowired
	private LoggerDispatcher loggerDispatcher;
    
	@RequestMapping(value = "/execute/service/{serviceName}/{serviceId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReturnView> executeServiceClient(
			@PathVariable String serviceName,
			@PathVariable int serviceId,
			@RequestBody(required = true) ExecuteParameterPojo bodyPojo
	)
	{
		Date firstContactDate=new Date();
		ReturnView returnView=new ReturnView();
		
		bodyPojo.sandbox=bodyPojo.sandbox!=null?bodyPojo.sandbox.replace(".", "_"):bodyPojo.sandbox;
		
		String identifiantLog = "(" + serviceName + ", " + serviceId + ")";
		loggerDispatcher.info(identifiantLog + " received", LOGGER);
	
		try {
		
		try (Connection connection = UtilitaireDao.get(0).getDriverConnexion()) {
						
			ExecuteRulesDao.fillRules(connection, bodyPojo, serviceName, serviceId);
						
			String env = bodyPojo.sandbox;
			String repertoire = PropertiesHandler.getInstance().getBatchParametersDirectory();
			String warehouse=bodyPojo.warehouse==null?"DEFAULT":bodyPojo.warehouse;
			
			if (TraitementPhase.getPhase(bodyPojo.targetPhase).equals(TraitementPhase.RECEPTION))
			{
				try(FileOutputStream fos=new FileOutputStream(DirectoryPath.directoryReceptionEntrepot(repertoire, env, warehouse) + File.separator + bodyPojo.fileName))
				{
					IOUtils.write(bodyPojo.fileContent, fos, StandardCharsets.UTF_8);
				}

			}
			
			ApiServiceFactory.getService(TraitementPhase.getPhase(bodyPojo.targetPhase), env,
							repertoire, Integer.MAX_VALUE, null).invokeApi();
			
			
			ExecuteRulesDao.buildResponse(connection, bodyPojo, returnView, firstContactDate);

		}
	} catch (Exception e) {
		loggerDispatcher.error(identifiantLog, e, LOGGER);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnView);
	}
	loggerDispatcher.info(identifiantLog + " done", LOGGER);
	return ResponseEntity.status(HttpStatus.OK).body(returnView);		
	
	}
	
	
	@RequestMapping(value = "/reset/service", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReturnView> resetServiceClient(
			@RequestBody(required = true) ExecuteParameterPojo bodyPojo
	)
	{
		Date firstContactDate=new Date();
		ReturnView returnView=new ReturnView();
		
		bodyPojo.sandbox=bodyPojo.sandbox!=null?bodyPojo.sandbox.replace(".", "_"):bodyPojo.sandbox;
		bodyPojo.queries=bodyPojo.queries==null?new ArrayList<ExecuteQueryPojo>():bodyPojo.queries;

		try {
		
		try (Connection connection = UtilitaireDao.get(0).getDriverConnexion()) {
						
						
			String env = bodyPojo.sandbox;
			String repertoire = PropertiesHandler.getInstance().getBatchParametersDirectory();

			ResetEnvironmentService.backToTargetPhase(TraitementPhase.getPhase(bodyPojo.targetPhase), env, repertoire, new ArrayList<>());
			
			ExecuteRulesDao.buildResponse(connection, bodyPojo, returnView, firstContactDate);

		}
	} catch (Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnView);
	}
	return ResponseEntity.status(HttpStatus.OK).body(returnView);		
	
	}
	
	@RequestMapping(value = "/execute/service", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReturnView> executeServiceClient(
			@RequestBody(required = true) ExecuteParameterPojo bodyPojo
	)
	{
		Date firstContactDate=new Date();
		ReturnView returnView=new ReturnView();
	
		bodyPojo.sandbox=bodyPojo.sandbox!=null?bodyPojo.sandbox.replace(".", "_"):bodyPojo.sandbox;
		bodyPojo.queries=bodyPojo.queries==null?new ArrayList<ExecuteQueryPojo>():bodyPojo.queries;
		
		try {
		
		try (Connection connection = UtilitaireDao.get(0).getDriverConnexion()) {
												
			String env = bodyPojo.sandbox;
			String repertoire = PropertiesHandler.getInstance().getBatchParametersDirectory();
			String warehouse=bodyPojo.warehouse==null?"DEFAULT":bodyPojo.warehouse;

			if (TraitementPhase.getPhase(bodyPojo.targetPhase).equals(TraitementPhase.RECEPTION))
			{

				try(FileOutputStream fos=new FileOutputStream(DirectoryPath.directoryReceptionEntrepot(repertoire, env, warehouse) + File.separator + bodyPojo.fileName))
				{
					IOUtils.write(bodyPojo.fileContent, fos, StandardCharsets.UTF_8);
				}

			}
			
			ApiServiceFactory.getService(TraitementPhase.getPhase(bodyPojo.targetPhase), env,
							repertoire, Integer.MAX_VALUE, null).invokeApi();
			
			
			ExecuteRulesDao.buildResponse(connection, bodyPojo, returnView, firstContactDate);

		}
	} catch (Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnView);
	}
	return ResponseEntity.status(HttpStatus.OK).body(returnView);		
	
	}
	
	@RequestMapping(value = "/execute/service/build/{env}/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReturnView> build(
			@PathVariable String env
	)
	{

		ReturnView returnView=new ReturnView();
		
		BddPatcher patcher = new BddPatcher();
		patcher.bddScript(null);
		patcher.bddScript(null, env);
		new BuildFileSystem(null,new String[] {env}).execute();

		return ResponseEntity.status(HttpStatus.OK).body(returnView);		

	}

	@RequestMapping(value = "/execute/service/synchonize/{env}/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReturnView> synchronize(
			@PathVariable String env
	)
	{
		ReturnView returnView=new ReturnView();
		try {
			new SynchronizeRulesAndMetadataOperation(new Sandbox(null, env)).synchroniserSchemaExecutionAllNods();
			return ResponseEntity.status(HttpStatus.OK).body(returnView);		

		} catch (ArcException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnView);		
		}		
	}

}
