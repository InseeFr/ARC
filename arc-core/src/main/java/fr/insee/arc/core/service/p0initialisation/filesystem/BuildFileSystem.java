package fr.insee.arc.core.service.p0initialisation.filesystem;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import fr.insee.arc.core.service.global.dao.DataStorage;
import fr.insee.arc.core.service.p1reception.ApiReceptionService;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class BuildFileSystem {
	
	/**
	 * Build the file system required for arc to proceed for a list of given sandboxes @param envExecutions
	 * @param connexion
	 * @param envExecutions
	 */
	public BuildFileSystem(Connection connexion, String[] envExecutions) {
		super();
		this.connexion = connexion;
		this.envExecutions = envExecutions;
	}

	private Connection connexion;
	
	private String[] envExecutions;
	
	
	
	
	/**
	 * Build directories for the sandbox
	 * 
	 * @param envExecutions
	 */
	public void execute() {
		PropertiesHandler properties = PropertiesHandler.getInstance();

		try {

			List<String> listEntrepot = DataStorage.execQuerySelectDatastorage(connexion);

			for (String envExecution : Arrays.asList(envExecutions)) {

				for (String d : listEntrepot) {
					FileUtilsArc.createDirIfNotexist(ApiReceptionService
							.directoryReceptionEntrepot(properties.getBatchParametersDirectory(), envExecution, d));
					FileUtilsArc.createDirIfNotexist(ApiReceptionService.directoryReceptionEntrepotArchive(
							properties.getBatchParametersDirectory(), envExecution, d));
				}

				FileUtilsArc.createDirIfNotexist(ApiReceptionService
						.directoryReceptionEtatEnCours(properties.getBatchParametersDirectory(), envExecution));
				FileUtilsArc.createDirIfNotexist(ApiReceptionService
						.directoryReceptionEtatOK(properties.getBatchParametersDirectory(), envExecution));
				FileUtilsArc.createDirIfNotexist(ApiReceptionService
						.directoryReceptionEtatKO(properties.getBatchParametersDirectory(), envExecution));
			}

		} catch (ArcException ex) {
			ex.logFullException();
		}

	}

}
