package fr.insee.arc.core.service.p1reception.registerfiles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementRapport;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p0initialisation.pilotage.SynchronizeDataByPilotageOperation;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.FileDescriber;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.FilesDescriber;
import fr.insee.arc.core.service.p1reception.registerfiles.dao.FileRegistrationDao;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;

public class FileRegistration {

	private static final Logger LOGGER = LogManager.getLogger(FileRegistration.class);

	public FileRegistration(Sandbox sandbox, String tablePilTemp) {
		super();
		this.sandbox = sandbox;
		dao = new FileRegistrationDao(sandbox, tablePilTemp);
	}

	private FileRegistrationDao dao;

	private Sandbox sandbox;

	/**
	 * Enregistrer les fichiers en entrée Déplacer les fichier reçus dans les
	 * repertoires OK ou pas OK selon le bordereau Supprimer les fichiers déjà
	 * existants de la table de pilotage Marquer les fichiers dans la table de
	 * pilotage
	 * 
	 * @throws ArcException
	 */
	public void registerAndDispatchFiles(FilesDescriber providedArchiveContent) throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "registerAndDispatchFiles");

		// la bean (fileName,type, etat) contient pour chaque fichier, le type
		// du fichier et l'action à réaliser

		FilesDescriber archiveContent = findDuplicates(providedArchiveContent);

		dao.createTemporaryResultTable();

		if (archiveContent.getFilesAttribute().isEmpty()) {
			return;
		}

		dao.execQueryRegisterFiles(archiveContent);

		List<String> idSourceMarkedToReplay = dao.execQuerySelectFilesMarkedToReplay();

		dao.execQueryDeleteFilesMarkedToReplay(idSourceMarkedToReplay);

		dao.execQueryInsertRegisteredFilesInPilotage();

		// synchronize data files if some files had been marked for replay
		// as they must be deleted from data
		if (idSourceMarkedToReplay != null) {
			SynchronizeDataByPilotageOperation synchronizationInstance = new SynchronizeDataByPilotageOperation(
					this.sandbox);
			synchronizationInstance.dropUnusedDataTablesAllNods(idSourceMarkedToReplay);
			synchronizationInstance.deleteUnusedDataRecordsAllNods(idSourceMarkedToReplay);
		}
	}

	/**
	 * Find the duplicates files in the database
	 * 
	 * @param fileList
	 * @return
	 * @throws ArcException
	 */
	private FilesDescriber findDuplicates(FilesDescriber fileList) throws ArcException {

		FilesDescriber content = new FilesDescriber();
		content.addAll(fileList);

		//  Détection des doublons de fichiers
		// Note : l'insertion est redondante mais au niveau métier, c'est
		// beaucoup plus logique
		StaticLoggerDispatcher.info(LOGGER, "Recherche de doublons de fichiers");

		dao.createTemporaryResultTable();

		dao.execQueryTemporaryInsertRegisteredFiles(content);

		List<String> listIdsourceDoublons = dao.execQueryFindDuplicateFiles();

		markDuplicateFilesInFilesDescriber(content, listIdsourceDoublons);

		List<String> listContainerARejouer = new ArrayList<>();
		List<String> listIdsourceARejouer = new ArrayList<>();

		dao.execQueryFindFilesMarkedAsReplay(listContainerARejouer, listIdsourceARejouer);

		content = rebuildFilesDescriberWithFilesToReplay(content, listContainerARejouer, listIdsourceARejouer);

		// detection des doublons d'archive
		// Génération d'un numéro pour l'archive en cas de doublon pour la versionner
		dao.execQueryInsertCorruptedArchiveInPilotage(content);

		List<String> listContainerDoublons = new ArrayList<>();
		ArrayList<String> listVersionContainerDoublons = new ArrayList<>();

		dao.execQueryVersionDuplicateArchives(listContainerDoublons, listVersionContainerDoublons);

		markArchivesVersion(content, listContainerDoublons, listVersionContainerDoublons);

		return content;
	}

	private void markArchivesVersion(FilesDescriber content, List<String> listContainerDoublons,
			ArrayList<String> listVersionContainerDoublons) {

		// set the num

		if (!listContainerDoublons.isEmpty()) {
			for (FileDescriber z : content.getFilesAttribute()) {
				if (z.getContainerName() != null) {
					z.setVirtualContainer(
							listVersionContainerDoublons.get(listContainerDoublons.indexOf(z.getContainerName())));
				}
			}
		}
	}

	private void markDuplicateFilesInFilesDescriber(FilesDescriber content, List<String> listIdsourceDoublons) {
		// on va parcourir la liste des fichiers
		// si on retrouve l'id_source dans la liste, on le marque en erreur et comme
		// dupliqué
		if (!listIdsourceDoublons.isEmpty()) {
			for (FileDescriber f : content.getFilesAttribute()) {
				// si le nom de fichier est renseigné et retrouvé dans la liste
				// on passe l'état à KO et on marque l'anomalie
				if (f.getFileName() != null && listIdsourceDoublons.contains(f.getFileName())) {
					f.setEtat(TraitementEtat.KO);
					f.setReport(TraitementRapport.INITIALISATION_DUPLICATE.toString());
				}
			}
		}
	}

	/**
	 * Complex operation. When some files in an archive are marked as replay, the
	 * whole archive is move back to reception en cours. But only the marked files
	 * must be registered, and other must be ignored and not considered as duplicate.
	 * Tough the other archives in reception_encours must be registered as usual.
	 * This method add these files and their archive to the register list.
	 * 
	 * @param content
	 * @param listContainerARejouer
	 * @param listIdsourceARejouer
	 * @return
	 */
	private FilesDescriber rebuildFilesDescriberWithFilesToReplay(FilesDescriber content,
			List<String> listContainerARejouer, List<String> listIdsourceARejouer) {

		if (listIdsourceARejouer.isEmpty()) {
			return content;
		}

		FilesDescriber reworkContent = new FilesDescriber();
		for (FileDescriber z : content.getFilesAttribute()) {
			// si le fichier est dans la liste des doublons à ignorer, on le l'ajoute pas à
			// la nouvelle liste
			if (z.getFileName() != null) {
				if (listContainerARejouer.contains(z.getContainerName())) {
					// si on trouve le fichier à rejouer, on l'ajoute; on ignore les autres
					if (listIdsourceARejouer.contains(z.getContainerName() + File.separator + z.getFileName())) {
						reworkContent.add(z);
					}
				} else {
					reworkContent.add(z);
				}
			} else {
				// bien ajouter toutes les archives à la nouvelle liste
				reworkContent.add(z);
			}
		}

		return reworkContent;
	}
}
