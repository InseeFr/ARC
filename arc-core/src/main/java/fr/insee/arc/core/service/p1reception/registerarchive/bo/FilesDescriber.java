package fr.insee.arc.core.service.p1reception.registerarchive.bo;

import java.util.ArrayList;
import java.util.List;

public class FilesDescriber {

	private List<FileDescriber> filesAttribute;

	public FilesDescriber() {
		super();
		filesAttribute = new ArrayList<>();
	}
	
	// add a file
	public void add(FileDescriber fileAttribute)
	{
		filesAttribute.add(fileAttribute);
	}

	// add a list of file
	public void addAll(FilesDescriber filesAttribute)
	{
		this.filesAttribute.addAll(filesAttribute.getFilesAttribute());
	}
	
	public List<FileDescriber> getFilesAttribute() {
		return filesAttribute;
	}

	public void setFilesAttribute(List<FileDescriber> filesAttribute) {
		this.filesAttribute = filesAttribute;
	}
	
	@Override
	public String toString()
	{
		return getFilesAttribute().toString();
	}
	
}
