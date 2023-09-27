package fr.insee.arc.core.service.p1reception.registerarchive.dao;

import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class DirectoriesDao {

	public DirectoriesDao(Sandbox sandbox) {
		this.sandbox = sandbox;
		this.directoryRoot = PropertiesHandler.getInstance().getBatchParametersDirectory();
		this.dirEnCours = DirectoryPath.directoryReceptionEtatEnCours(directoryRoot, sandbox.getSchema());
		this.dirOK = DirectoryPath.directoryReceptionEtatOK(directoryRoot, sandbox.getSchema());
		this.dirKO = DirectoryPath.directoryReceptionEtatKO(directoryRoot, sandbox.getSchema());
	}

	private String directoryRoot;
	private String dirEnCours;
	private String dirOK;
	private String dirKO;
	private String dirEntrepotIn;
	private String dirEntrepotArchive;
	private Sandbox sandbox;

	/**
	 * create global sandbox directories if not exist and register their paths in class
	 */
	public void createSandboxDirectories() {
		// Create target directories if they don't exist
		FileUtilsArc.createDirIfNotexist(this.dirEnCours);
		FileUtilsArc.createDirIfNotexist(this.dirOK);
		FileUtilsArc.createDirIfNotexist(this.dirKO);
	}

	/**
	 * create datawarehouse sandbox directories if not exist and register their paths in class
	 */
	public void createSandboxDatawarehouseDirectories(String entrepot) {

		this.dirEntrepotIn = DirectoryPath.directoryReceptionEntrepot(directoryRoot, sandbox.getSchema(),
				entrepot);
		this.dirEntrepotArchive = DirectoryPath.directoryReceptionEntrepotArchive(directoryRoot,
				sandbox.getSchema(), entrepot);
		
		// créer le répertoire de l'entrepot et son repertoire archive
		FileUtilsArc.createDirIfNotexist(dirEntrepotArchive);
		FileUtilsArc.createDirIfNotexist(dirEntrepotIn);
	}

	public String getDirectoryRoot() {
		return directoryRoot;
	}

	public void setDirectoryRoot(String directoryRoot) {
		this.directoryRoot = directoryRoot;
	}

	public String getDirEnCours() {
		return dirEnCours;
	}

	public void setDirEnCours(String dirEnCours) {
		this.dirEnCours = dirEnCours;
	}

	public String getDirOK() {
		return dirOK;
	}

	public void setDirOK(String dirOK) {
		this.dirOK = dirOK;
	}

	public String getDirKO() {
		return dirKO;
	}

	public void setDirKO(String dirKO) {
		this.dirKO = dirKO;
	}

	public String getDirEntrepotIn() {
		return dirEntrepotIn;
	}

	public void setDirEntrepotIn(String dirEntrepotIn) {
		this.dirEntrepotIn = dirEntrepotIn;
	}

	public String getDirEntrepotArchive() {
		return dirEntrepotArchive;
	}

	public void setDirEntrepotArchive(String dirEntrepotArchive) {
		this.dirEntrepotArchive = dirEntrepotArchive;
	}
	
	
}