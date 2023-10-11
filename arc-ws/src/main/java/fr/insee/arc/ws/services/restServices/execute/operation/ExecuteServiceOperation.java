package fr.insee.arc.ws.services.restServices.execute.operation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.DataWarehouse;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p0initialisation.ResetEnvironmentService;
import fr.insee.arc.core.service.p0initialisation.dbmaintenance.BddPatcher;
import fr.insee.arc.core.service.p0initialisation.filesystem.BuildFileSystem;
import fr.insee.arc.core.service.p0initialisation.metadata.SynchronizeRulesAndMetadataOperation;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.ws.services.restServices.execute.model.ExecuteParameterModel;
import fr.insee.arc.ws.services.restServices.execute.model.ResponseAttributes;
import fr.insee.arc.ws.services.restServices.execute.view.ReturnView;

public class ExecuteServiceOperation {

	private ReturnView returnView;
	private ResponseAttributes responseAttributes;
	private ExecuteParameterModel bodyPojo;

	public ExecuteServiceOperation(ReturnView returnView, ResponseAttributes responseAttributes,
			ExecuteParameterModel bodyPojo) {
		super();
		this.returnView = returnView;
		this.responseAttributes = responseAttributes;
		this.bodyPojo = bodyPojo;
	}

	public ExecuteServiceOperation() {
		super();
	}

	public void executeServiceClient() throws ArcException {

		try (Connection connection = UtilitaireDao.get(0).getDriverConnexion()) {

			ExecuteRulesDao.fillRules(connection, bodyPojo, responseAttributes.getServiceName(),
					responseAttributes.getServiceId());

			String env = bodyPojo.sandbox;
			String repertoire = PropertiesHandler.getInstance().getBatchParametersDirectory();
			String warehouse = bodyPojo.warehouse == null ? DataWarehouse.DEFAULT.getName() : bodyPojo.warehouse;

			if (TraitementPhase.getPhase(bodyPojo.targetPhase).equals(TraitementPhase.RECEPTION)) {
				try (FileOutputStream fos = new FileOutputStream(
						DirectoryPath.directoryReceptionEntrepot(repertoire, env, warehouse) + File.separator
								+ bodyPojo.fileName)) {
					IOUtils.write(bodyPojo.fileContent, fos, StandardCharsets.UTF_8);
				}

			}

			ApiServiceFactory.getService(TraitementPhase.getPhase(bodyPojo.targetPhase), env, repertoire,
					Integer.MAX_VALUE, null).invokeApi();

			ExecuteRulesDao.buildResponse(connection, bodyPojo, returnView, responseAttributes.getFirstContactDate());

		} catch (IOException e) {
			throw new ArcException(ArcExceptionMessage.FILE_READ_FAILED);
		} catch (SQLException e) {
			throw new ArcException(ArcExceptionMessage.DATABASE_CONNECTION_FAILED);
		}

	}

	/**
	 * reset the sandbox to a target phase
	 * 
	 * @param env         : sandbox identifier
	 * @param targetPhase
	 * @throws ArcException
	 */
	public void resetServiceClient(String env, String targetPhase) throws ArcException {

		try (Connection connection = UtilitaireDao.get(0).getDriverConnexion()) {

			String repertoire = PropertiesHandler.getInstance().getBatchParametersDirectory();

			ResetEnvironmentService.backToTargetPhase(TraitementPhase.getPhase(targetPhase), env, repertoire,
					new ArrayList<>());

		} catch (SQLException e) {
			throw new ArcException(ArcExceptionMessage.DATABASE_CONNECTION_FAILED);
		}
	}

	/**
	 * Build a sandbox from scratch it build the file system and build database
	 * 
	 * @param env : sandbox identifier
	 */
	public void buildSandbox(String env) {

		BddPatcher patcher = new BddPatcher();
		patcher.bddScript(null);
		patcher.bddScript(null, env);
		new BuildFileSystem(null, new String[] { env }).execute();

	}

	/**
	 * Synchronize the rules of sandbox with the one provided by user in arc
	 * metadata schema
	 * 
	 * @param env : sandbox identifier
	 * @throws ArcException
	 */
	public void synchronizeSandbox(String env) throws ArcException {
		try (Connection connection = UtilitaireDao.get(0).getDriverConnexion()) {
			new SynchronizeRulesAndMetadataOperation(new Sandbox(connection, env)).synchroniserSchemaExecutionAllNods();
		} catch (SQLException e) {
			throw new ArcException(ArcExceptionMessage.DATABASE_CONNECTION_FAILED);
		}
	}

}
