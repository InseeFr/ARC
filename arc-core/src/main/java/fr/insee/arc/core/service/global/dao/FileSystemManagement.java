package fr.insee.arc.core.service.global.dao;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.structure.GenericBean;

public class FileSystemManagement {

	private FileSystemManagement() {
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

	/**
	 * delete a file if it is the same as another reference file
	 * @param fileToDeleteIfSame
	 * @param fileToCompare
	 * @throws ArcException
	 */
	public static void deleteFileIfSameAs(File fileToDeleteIfSame, File fileToCompare) throws ArcException
	{
		try {
			if (fileToCompare.exists() && FileUtils.contentEquals(fileToCompare, fileToDeleteIfSame)) {
				FileUtilsArc.delete(fileToDeleteIfSame);
			}
		} catch (IOException exception) {
			throw new ArcException(exception, ArcExceptionMessage.FILE_DELETE_FAILED, fileToDeleteIfSame);
		}
	}
	
}