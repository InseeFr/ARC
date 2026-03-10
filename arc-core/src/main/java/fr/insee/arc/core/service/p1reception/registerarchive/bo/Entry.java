package fr.insee.arc.core.service.p1reception.registerarchive.bo;

public class Entry {

	private boolean isDirectory;
	
	private String name;
	
	public Entry(boolean isDirectory, String name) {
		super();
		this.isDirectory = isDirectory;
		this.name = name;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
