package fr.insee.arc.utils.files;

public enum CompressionExtension {

	TGZ(".tgz"), TAR_GZ(".tar.gz"), ZIP(".zip"), GZ(".gz");

	private CompressionExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	private String fileExtension;

	public String getFileExtension() {
		return fileExtension;
	}
	

}
