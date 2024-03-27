package fr.insee.arc.core.service.p6export.provider;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.dao.FileSystemManagement;

public class DirectoryPathExport {

	private DirectoryPathExport() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Methods to provide directories paths
	 * 
	 * @param rootDirectory
	 * @param env
	 * @return
	 */
	public static String directoryExport (String rootDirectory, String env, String dateExport) {
		return FileSystemManagement.directoryPhaseRootSubdirectories(rootDirectory, env, TraitementPhase.EXPORT, dateExport);
	}

	public static String s3Export(String env, String dateExport) {
		return FileSystemManagement.directoryPhaseRootSubdirectories("", env, TraitementPhase.EXPORT, dateExport);
	}

	
}
