package fr.insee.arc.core.service.p1reception.provider;

import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.s3.ArcS3;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class DirectoriesReception {

	public DirectoriesReception(Sandbox sandbox) {
		this.sandbox = sandbox;
		this.directoryRoot = PropertiesHandler.getInstance().getBatchParametersDirectory();
		this.directoryReceptionEnCours = DirectoryPath.directoryReceptionEtatEnCours(directoryRoot, sandbox.getSchema());
		this.directoryReceptionOK = DirectoryPath.directoryReceptionEtatOK(directoryRoot, sandbox.getSchema());
		this.directoryReceptionKO = DirectoryPath.directoryReceptionEtatKO(directoryRoot, sandbox.getSchema());
	}

	private String directoryRoot;
	private String directoryReceptionEnCours;
	private String directoryReceptionOK;
	private String directoryReceptionKO;

	private String entrepot;
	private String directoryEntrepotIn;
	private String directoryEntrepotArchive;
	private String s3EntrepotIn;
	
	private Sandbox sandbox;

	/**
	 * create global sandbox directories if not exist and register their paths in class
	 */
	public void createSandboxDirectories() {
		// Create target directories if they don't exist
		FileUtilsArc.createDirIfNotexist(this.directoryRoot);
		FileUtilsArc.createDirIfNotexist(this.directoryReceptionEnCours);
		FileUtilsArc.createDirIfNotexist(this.directoryReceptionOK);
		FileUtilsArc.createDirIfNotexist(this.directoryReceptionKO);
	}

	/**
	 * create entrepot directories for sandbox if not exist and register their paths in class
	 * @throws ArcException 
	 */
	public DirectoriesReception buildSandboxEntrepotDirectories(String entrepot) throws ArcException {
		this.entrepot = entrepot;
		this.directoryEntrepotIn = DirectoryPath.directoryReceptionEntrepot(directoryRoot, sandbox.getSchema(),
				entrepot);
		this.directoryEntrepotArchive = DirectoryPath.directoryReceptionEntrepotArchive(directoryRoot,
				sandbox.getSchema(), entrepot);
		this.s3EntrepotIn = DirectoryPath.s3ReceptionEntrepot(sandbox.getSchema(),entrepot);
		return this;
	}
	
	public void createSandboxEntrepotDirectories() throws ArcException {
		ArcS3.INPUT_BUCKET.createDirectory(s3EntrepotIn);
		FileUtilsArc.createDirIfNotexist(directoryEntrepotArchive);
		FileUtilsArc.createDirIfNotexist(directoryEntrepotIn);
	}
	

	public String getDirectoryRoot() {
		return directoryRoot;
	}

	public void setDirectoryRoot(String directoryRoot) {
		this.directoryRoot = directoryRoot;
	}

	public String getDirectoryReceptionEnCours() {
		return directoryReceptionEnCours;
	}

	public void setDirectoryReceptionEnCours(String directoryReceptionEnCours) {
		this.directoryReceptionEnCours = directoryReceptionEnCours;
	}

	public String getDirectoryReceptionOK() {
		return directoryReceptionOK;
	}

	public void setDirectoryReceptionOK(String directoryReceptionOK) {
		this.directoryReceptionOK = directoryReceptionOK;
	}

	public String getDirectoryReceptionKO() {
		return directoryReceptionKO;
	}

	public void setDirectoryReceptionKO(String directoryReceptionKO) {
		this.directoryReceptionKO = directoryReceptionKO;
	}

	public String getDirectoryEntrepotIn() {
		return directoryEntrepotIn;
	}

	public void setDirectoryEntrepotIn(String directoryEntrepotIn) {
		this.directoryEntrepotIn = directoryEntrepotIn;
	}

	public String getDirectoryEntrepotArchive() {
		return directoryEntrepotArchive;
	}

	public void setDirectoryEntrepotArchive(String directoryEntrepotArchive) {
		this.directoryEntrepotArchive = directoryEntrepotArchive;
	}

	public String getS3EntrepotIn() {
		return s3EntrepotIn;
	}

	public void setS3EntrepotIn(String s3EntrepotIn) {
		this.s3EntrepotIn = s3EntrepotIn;
	}

	public String getEntrepot() {
		return entrepot;
	}

	public void setEntrepot(String entrepot) {
		this.entrepot = entrepot;
	}

}