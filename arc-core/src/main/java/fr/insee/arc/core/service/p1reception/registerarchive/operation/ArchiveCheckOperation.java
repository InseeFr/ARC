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

/**
 * Class to check archive
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
	 */
	public FilesDescriber checkArchive(File f, String entrepot) {
		// Inscription des fichiers au contenu de l'archive

		FilesDescriber contentTemp = new FilesDescriber();

		setStatus(0, null, null);

		// Check if the archive is fully readable
		try 
		{
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
					
					validateEntry();

					contentTemp.add(new FileDescriber(f.getName(), entrepot + name,
							TraitementTypeFichier.DA, etat, rapport, null));

					rapport = null;
				}
			}
		} catch (IOException e1) {
			erreur = 1;
			rapport = TraitementRapport.INITIALISATION_CORRUPTED_ARCHIVE.toString();
		}
		finally
		{
			archiveStream.close();
		}

		// Inscription de l'archive
		contentTemp.add(new FileDescriber(f.getName(), null,
				erreur == 1 ? TraitementTypeFichier.AC : TraitementTypeFichier.A,
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
