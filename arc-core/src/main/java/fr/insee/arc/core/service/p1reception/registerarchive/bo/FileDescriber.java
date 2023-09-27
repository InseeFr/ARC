package fr.insee.arc.core.service.p1reception.registerarchive.bo;

import java.util.Arrays;
import java.util.List;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementTypeFichier;

public class FileDescriber {

	private String containerName;
	private String fileName;
	private TraitementTypeFichier typeOfFile;
	private TraitementEtat etat;
	private String report;
	private String virtualContainer;

	public FileDescriber(String containerName, String fileName, TraitementTypeFichier typeOfFile, TraitementEtat etat, String report,
			String virtualContainer) {
		super();

		this.containerName = containerName;
		this.fileName = fileName;
		this.typeOfFile = typeOfFile;
		this.etat = etat;
		this.report = report;
		this.virtualContainer = virtualContainer;
	}

	public List<String> fileAttributes() {
		return Arrays
				.asList(containerName, fileName, typeOfFile.toString(), etat.toString(), report, virtualContainer);
	}

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public TraitementTypeFichier getTypeOfFile() {
		return typeOfFile;
	}

	public void setTypeOfFile(TraitementTypeFichier typeOfFile) {
		this.typeOfFile = typeOfFile;
	}

	public TraitementEtat getEtat() {
		return etat;
	}

	public void setEtat(TraitementEtat etat) {
		this.etat = etat;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public String getVirtualContainer() {
		return virtualContainer;
	}

	public void setVirtualContainer(String virtualContainer) {
		this.virtualContainer = virtualContainer;
	}

	@Override
	public String toString()
	{
		return fileAttributes().toString();
	}
	
	
}
