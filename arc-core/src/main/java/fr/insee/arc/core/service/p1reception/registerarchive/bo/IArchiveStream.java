package fr.insee.arc.core.service.p1reception.registerarchive.bo;

import java.io.File;
import java.io.IOException;

public interface IArchiveStream {
	
	public void startInputStream(File f) throws IOException;
	
	public Entry getEntry() throws IOException;
	
	public void close();
	
}
