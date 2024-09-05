package fr.insee.arc.core.service.p6export.provider;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.dao.FileSystemManagement;

public class DirectoryPathExport {

	private DirectoryPathExport() {
		throw new IllegalStateException("Utility class");
	}

	private static final String S3_EXPORT_ROOT = "ARC";

	/**
	 * Methods to provide directories paths
	 * 
	 * @param rootDirectory
	 * @param env
	 * @return
	 */
	public static String directoryExport(String rootDirectory, String env) {
		return FileSystemManagement.directoryPhaseRootSubdirectories(rootDirectory, env, TraitementPhase.EXPORT);
	}

	public static String directoryExport(String rootDirectory, String env, String clientExport, String dateExport) {
		return FileSystemManagement.directoryPhaseRootSubdirectories(rootDirectory, env, TraitementPhase.EXPORT, clientExport,
				dateExport);
	}
	
	public static String s3Export(String env, String clientExport, String dateExport) {
		return FileSystemManagement.directoryRootSubdirectories(S3_EXPORT_ROOT, env, clientExport,
				dateExport);
	}

}
