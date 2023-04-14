package fr.insee.arc.core.service.api.query;

import java.io.File;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;

public class ServiceFileSystemManagement {

	private ServiceFileSystemManagement() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Directory management
	 */
	private static final String DIRECTORY_EXPORT_QUALIFIIER = "EXPORT";

	private static final String DIRECTORY_TOKEN = "_";

	private static final String DIRECTORY_ARCHIVE_QUALIFIIER = "ARCHIVE";

	private static final String DIRECTORY_OLD_QUALIFIIER = "OLD";

	public static String directoryEnvRoot(String rootDirectory, String env) {
		return rootDirectory + File.separator + env.replace(".", "_").toUpperCase();
	}

	public static String directoryPhaseRoot(String rootDirectory, String env, TraitementPhase t) {
		return directoryEnvRoot(rootDirectory, env) + File.separator + t.toString();
	}

	public static String directoryEnvExport(String rootDirectory, String env) {
		return directoryEnvRoot(rootDirectory, env) + File.separator + DIRECTORY_EXPORT_QUALIFIIER;
	}

	public static String directoryPhaseEntrepot(String rootDirectory, String env, TraitementPhase t, String entrepot) {
		return directoryPhaseRoot(rootDirectory, env, t) + DIRECTORY_TOKEN + entrepot;
	}

	public static String directoryPhaseEntrepotArchive(String rootDirectory, String env, TraitementPhase t,
			String entrepot) {
		return directoryPhaseEntrepot(rootDirectory, env, t, entrepot) + DIRECTORY_TOKEN + DIRECTORY_ARCHIVE_QUALIFIIER;
	}

	public static String directoryPhaseEntrepotArchiveOld(String rootDirectory, String env, TraitementPhase t,
			String entrepot) {
		return directoryPhaseEntrepotArchive(rootDirectory, env, t, entrepot) + File.separator
				+ DIRECTORY_OLD_QUALIFIIER;
	}

	public static String directoryPhaseEtat(String rootDirectory, String env, TraitementPhase t, TraitementEtat e) {
		return directoryPhaseRoot(rootDirectory, env, t) + DIRECTORY_TOKEN + e.toString();
	}

	public static String directoryPhaseEtatOK(String rootDirectory, String env, TraitementPhase t) {
		return directoryPhaseEtat(rootDirectory, env, t, TraitementEtat.OK);
	}

	public static String directoryPhaseEtatKO(String rootDirectory, String env, TraitementPhase t) {
		return directoryPhaseEtat(rootDirectory, env, t, TraitementEtat.KO);
	}

	public static String directoryPhaseEtatEnCours(String rootDirectory, String env, TraitementPhase t) {
		return directoryPhaseEtat(rootDirectory, env, t, TraitementEtat.ENCOURS);
	}

}
