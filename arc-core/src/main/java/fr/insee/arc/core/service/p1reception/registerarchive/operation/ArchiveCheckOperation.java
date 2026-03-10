package fr.insee.arc.core.service.p1reception.registerarchive.operation;

import java.io.File;
import java.io.IOException;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementRapport;
import fr.insee.arc.core.model.TraitementTypeFichier;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.Entry;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.FileDescriber;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.FilesDescriber;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.IArchiveStream;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.CompressionExtension;

/**
 * Class to check archive
 * 
 * @author FY2QEQ
 *
 */
public class ArchiveCheckOperation {

	private int erreur;
	private TraitementEtat etat;
	private String rapport;

	IArchiveStream archiveStream;
	Entry currentEntry;

	public ArchiveCheckOperation(IArchiveStream archiveStream) {
		super();
		this.archiveStream = archiveStream;
	}

	/**
	 * Check every file in the tgz archive and returns the archive content.
	 * @throws ArcException 
	 */
	public FilesDescriber checkArchive(File f, String entrepot) throws ArcException {
		// Inscription des fichiers au contenu de l'archive

		FilesDescriber contentTemp = new FilesDescriber();

		setStatus(0, null, null);

		// Check if the archive is fully readable
		try {
			archiveStream.startInputStream(f);
			// default case if archive is empty of real files
			setStatus(1, TraitementEtat.KO, TraitementRapport.INITIALISATION_CORRUPTED_ARCHIVE.toString());

			this.currentEntry = archiveStream.getEntry();

			// Check every entry
			while (currentEntry != null) {

				if (currentEntry.isDirectory()) {
					currentEntry = archiveStream.getEntry();
				} else {
					setStatus(0, TraitementEtat.OK, null);

					String name = currentEntry.getName();

					// prefix entry name with entrepot
					String entryNamePrefixedWithEntrepot = addEntrepotPrefixToEntryName(entrepot, f.getName(), name);

					validateEntry();

					contentTemp.add(new FileDescriber(f.getName(), entryNamePrefixedWithEntrepot , TraitementTypeFichier.DA, etat,
							rapport, null));

					rapport = null;
				}
			}
		} catch (IOException e1) {
			erreur = 1;
			rapport = TraitementRapport.INITIALISATION_CORRUPTED_ARCHIVE.toString();
		} finally {
			archiveStream.close();
		}

		// Inscription de l'archive
		contentTemp.add(
				new FileDescriber(f.getName(), null, erreur == 1 ? TraitementTypeFichier.AC : TraitementTypeFichier.A,
						erreur > 0 ? TraitementEtat.KO : TraitementEtat.OK, rapport, null));

		// If there is any error, all files are marked KO with a special report
		if (erreur > 0) {
			for (FileDescriber fileInfo : contentTemp.getFilesAttribute()) {
				fileInfo.setEtat(TraitementEtat.KO);
				if (fileInfo.getReport() == null) {
					fileInfo.setReport(TraitementRapport.INITIALISATION_FICHIER_OK_ARCHIVE_KO.toString());
				}
			}
		}
		return contentTemp;
	}

	/**
	 * Add entrepot to entry name if required GZ entry get the name of original
	 * archive name so entrepot musn't be added to these ones as it is already in
	 * the archive name
	 * For other archive type, entries name are not dependent from
	 * archive name so entrepot must be prefixed to them
	 * 
	 * @param entrepot
	 * @param name
	 * @param name2
	 * @return
	 * @throws ArcException 
	 */
	protected static String addEntrepotPrefixToEntryName(String entrepot, String archiveName, String entryName) throws ArcException {
		
		if (archiveName.endsWith(CompressionExtension.TAR_GZ.getFileExtension())
				|| archiveName.endsWith(CompressionExtension.TGZ.getFileExtension())) {
			return entrepot + entryName;
		}
		
		if (archiveName.endsWith(CompressionExtension.ZIP.getFileExtension())) {
			return entrepot + entryName;
		}
		
		if (archiveName.endsWith(CompressionExtension.GZ.getFileExtension())) {
			return entryName;
		}
		
		throw new ArcException(ArcExceptionMessage.INVALID_FILE_FORMAT);
	}

	private void setStatus(int erreur, TraitementEtat etat, String rapport) {
		this.erreur = erreur;
		this.etat = etat;
		this.rapport = rapport;
	}

	/**
	 * validate the entry integrity if entry is invalid, mark error and break the
	 * loop over entry
	 * 
	 * @param tarInput
	 */
	private void validateEntry() {
		try {
			currentEntry = archiveStream.getEntry();
		} catch (IOException e) {
			setStatus(2, TraitementEtat.KO, TraitementRapport.INITIALISATION_CORRUPTED_ENTRY.toString());
			currentEntry = null;
		}
	}

}
