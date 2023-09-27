package fr.insee.arc.core.service.p1reception.provider;

import java.io.File;
import java.time.Year;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.dao.FileSystemManagement;

public class DirectoryPath {
	
	
	/**
	 * Methods to provide directories paths
	 * 
	 * @param rootDirectory
	 * @param env
	 * @return
	 */
	public static String directoryReceptionRoot(String rootDirectory, String env) {
		return FileSystemManagement.directoryPhaseRoot(rootDirectory, env, TraitementPhase.RECEPTION);
	}

	public static String directoryReceptionEntrepot(String rootDirectory, String env, String entrepot) {
		return FileSystemManagement.directoryPhaseEntrepot(rootDirectory, env, TraitementPhase.RECEPTION,
				entrepot);
	}

	public static String directoryReceptionEntrepotArchive(String rootDirectory, String env, String entrepot) {
		return FileSystemManagement.directoryPhaseEntrepotArchive(rootDirectory, env, TraitementPhase.RECEPTION,
				entrepot);
	}

	public static String directoryReceptionEntrepotArchiveOld(String rootDirectory, String env, String entrepot) {
		return FileSystemManagement.directoryPhaseEntrepotArchiveOld(rootDirectory, env,
				TraitementPhase.RECEPTION, entrepot);
	}
	
	public static String directoryReceptionEntrepotArchiveOldYearStamped(String rootDirectory, String env, String entrepot) {
		return directoryReceptionEntrepotArchiveOld(rootDirectory, env, entrepot) + File.separator + 	Year.now().getValue();
	}

	public static String directoryReceptionEtat(String rootDirectory, String env, TraitementEtat e) {
		return FileSystemManagement.directoryPhaseEtat(rootDirectory, env, TraitementPhase.RECEPTION, e);
	}

	public static String directoryReceptionEtatOK(String rootDirectory, String env) {
		return FileSystemManagement.directoryPhaseEtatOK(rootDirectory, env, TraitementPhase.RECEPTION);
	}

	public static String directoryReceptionEtatKO(String rootDirectory, String env) {
		return FileSystemManagement.directoryPhaseEtatKO(rootDirectory, env, TraitementPhase.RECEPTION);
	}

	public static String directoryReceptionEtatEnCours(String rootDirectory, String env) {
		return FileSystemManagement.directoryPhaseEtatEnCours(rootDirectory, env, TraitementPhase.RECEPTION);
	}

}
