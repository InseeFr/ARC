package fr.insee.arc.core.service.p1reception.registerarchive.operation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import fr.insee.arc.core.service.p1reception.provider.DirectoriesReception;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.CompressedUtils;
import fr.insee.arc.utils.files.CompressionExtension;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.utils.ManipString;

/**
 * This class rework the archive that had been received It renames it so that
 * old archive with the same name are not overwritten and securely kept It
 * convert simple file into archived file
 * 
 * @author FY2QEQ
 *
 */
public class ReworkArchiveOperation {

	public ReworkArchiveOperation(DirectoriesReception directories, String entrepot, File inputFile) {
		super();
		this.directories = directories;
		this.entrepot = entrepot;
		this.inputFile = inputFile;
	}

	private String entrepot;
	private File inputFile;
	private DirectoriesReception directories;

	private boolean isArchive;
	private File fileOutArchive;
	
	private int reworkedArchiveSize;
	private String reworkedArchiveName;
	private File reworkedArchiveFile;


	/**
	 * Update that the file is an archive or not Rework the file name not to overlap
	 * old files
	 */
	public void qualifyAndRename() {
		// Archiver le fichier
		// on regarde si le fichier existe déjà dans le repertoire archive; si c'est le
		// cas, on va renommer

		for (int i = 1; i < Integer.MAX_VALUE; i++) {

			// on reprend le nom du fichier
			reworkedArchiveName = inputFile.getName();
			isArchive = true;

			// les fichiers non archivés sont archivés
			if (CompressedUtils.isNotArchive(reworkedArchiveName)) {
				reworkedArchiveName = new StringBuilder(reworkedArchiveName).append(CompressionExtension.TAR_GZ.getFileExtension()).toString();
				isArchive = false;
			}

			// on ajoute un index au nom du fichier toto.tar.gz devient toto#1.tar.gz
			if (i > 1) {
				reworkedArchiveName = ManipString.substringBeforeFirst(reworkedArchiveName, ".") + "#" + i + "."
						+ ManipString.substringAfterFirst(reworkedArchiveName, ".");
			}

			fileOutArchive = new File(directories.getDirectoryEntrepotArchive() + File.separator + reworkedArchiveName);

			if (!fileOutArchive.exists()) {
				break;
			}
		}
	}

	/**
	 * rework archive file, save it in the archive directory and move it to the encours directory  
	 * plain not compressed file are compressed before
	 * @return fileSize : the file size of the reworked archive file
	 * @throws ArcException
	 */
	public void reworkArchive() throws ArcException {
		// si le fichier n'existe pas dans le repertoire d'archive
		// on le copie dans archive avec son nouveau nom
		// on change le nom du fichier initial avec son nouveau nom indexé
		// on le déplace dans encours
		// on enregistre le fichier dans la table d'archive
		// on sort de la boucle d'indexation
		if (isArchive) {
			// copie dans archive avec le nouveau nom
			try {
				Files.copy(Paths.get(inputFile.getAbsolutePath()), Paths.get(fileOutArchive.getAbsolutePath()));
			} catch (IOException exception) {
				throw new ArcException(exception, ArcExceptionMessage.FILE_COPY_FAILED, inputFile, fileOutArchive);
			}
			// déplacer le fichier dans encours
			FileUtilsArc.deplacerFichier(directories.getDiretoryEntrepotIn(), directories.getDirectoryReceptionEnCours(),
					inputFile.getName(), entrepot + "_" + reworkedArchiveName);
		} else {
			// on génére le tar.gz dans archive
			CompressedUtils.generateTarGzFromFile(inputFile, fileOutArchive, inputFile.getName());
			// on copie le tar.gz dans encours
			File fOut = new File(directories.getDirectoryReceptionEnCours() + File.separator + entrepot + "_" + reworkedArchiveName);
			try {
				Files.copy(Paths.get(fileOutArchive.getAbsolutePath()), Paths.get(fOut.getAbsolutePath()));
			} catch (IOException exception) {
				throw new ArcException(exception, ArcExceptionMessage.FILE_COPY_FAILED, fileOutArchive, fOut);
			}
			// on efface le fichier source
			FileUtilsArc.delete(inputFile);
		}
		
		this.reworkedArchiveFile = new File(directories.getDirectoryReceptionEnCours() + File.separator + entrepot + "_" + reworkedArchiveName);
		this.reworkedArchiveSize = (int) (fileOutArchive.length() / 1024 / 1024);
	}

	public int getReworkedArchiveSize() {
		return reworkedArchiveSize;
	}

	public String getReworkedArchiveName() {
		return reworkedArchiveName;
	}

	public File getReworkedArchiveFile() {
		return reworkedArchiveFile;
	}

	public void setReworkedArchiveFile(File reworkedArchiveFile) {
		this.reworkedArchiveFile = reworkedArchiveFile;
	}

	
	
}
