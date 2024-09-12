package fr.insee.arc.ws.services.importServlet.provider;

import fr.insee.arc.core.service.global.dao.FileSystemManagement;

public class DirectoryPathExportWs {

	private DirectoryPathExportWs() {
		throw new IllegalStateException("Utility class");
	}

	private static final String WS_EXPORT_ROOT = "EXPORT_WS";

	/**
	 * Methods to provide directories paths
	 * 
	 * @param rootDirectory
	 * @param env
	 * @return
	 */

	public static String directoryExport(String rootDirectory, String env) {
		return FileSystemManagement.directoryRootSubdirectories(rootDirectory, env, WS_EXPORT_ROOT);
	}

}
