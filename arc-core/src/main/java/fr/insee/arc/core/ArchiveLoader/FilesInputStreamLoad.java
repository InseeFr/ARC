package fr.insee.arc.core.ArchiveLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.service.ApiReceptionService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;

/**
 * Just a map with inputstream in it. We have to read throught the file multiple time, this object is here to
 * carry all the necessary inputstram.
 */
public class FilesInputStreamLoad {
    private static final Logger LOGGER = LogManager.getLogger(FilesInputStreamLoad.class);

    
    private InputStream tmpInxChargement ;
    private InputStream tmpInxNormage ;
    private InputStream tmpInxCSV ;
    private File theFileToRead;
    
    
    
    
    public FilesInputStreamLoad(File theFileToRead) throws Exception {
	super();
	try {
	    this.tmpInxChargement =  new GZIPInputStream(new BufferedInputStream(new FileInputStream(theFileToRead),ApiReceptionService.READ_BUFFER_SIZE));
	    this.tmpInxNormage =  new GZIPInputStream(new BufferedInputStream(new FileInputStream(theFileToRead),ApiReceptionService.READ_BUFFER_SIZE));
	    this.tmpInxCSV =  new GZIPInputStream(new BufferedInputStream(new FileInputStream(theFileToRead),ApiReceptionService.READ_BUFFER_SIZE));
	} catch (FileNotFoundException e) {
	    StaticLoggerDispatcher.error("Can't instanciate FilesInputStreamLoad for file " + theFileToRead.getName(), LOGGER);
	    throw e;
	}
	this.theFileToRead = theFileToRead;
    }
    
    public FilesInputStreamLoad() {
	super();
    }
    
    public void closeAll() throws IOException {
	 try {
		 if (this.tmpInxChargement!=null)
		 {
			 this.tmpInxChargement.close();
		 }
		 if (this.tmpInxNormage!=null)
		 {
			 this.tmpInxNormage.close();
		 }
		 if (this.tmpInxCSV!=null)
		 {
			 this.tmpInxCSV.close();
		 }
	} catch (IOException e) {
	    StaticLoggerDispatcher.error("Can't close all FilesInputStreamLoad for file " + theFileToRead.getName(), LOGGER);
	    throw e;
	}
    }
    
    public InputStream getTmpInxChargement() {
        return tmpInxChargement;
    }
    public void setTmpInxChargement(InputStream tmpInxChargement) {
        this.tmpInxChargement = tmpInxChargement;
    }
    public InputStream getTmpInxNormage() {
        return tmpInxNormage;
    }
    public void setTmpInxNormage(InputStream tmpInxNormage) {
        this.tmpInxNormage = tmpInxNormage;
    }

    public InputStream getTmpInxCSV() {
        return tmpInxCSV;
    }
    public void setTmpInxCSV(InputStream tmpInxCSV) {
        this.tmpInxCSV = tmpInxCSV;
    }
    public File getTheFileToRead() {
        return theFileToRead;
    }
    public void setTheFileToRead(File theFileToRead) {
        this.theFileToRead = theFileToRead;
    }
    
    
}
