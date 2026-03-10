package fr.insee.arc.core.service.p2chargement.archiveloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.files.CompressedUtils;

/**
 * Just a map with inputstream in it. We have to read throught the file multiple time, this object is here to
 * carry all the necessary inputstram.
 */
public class FilesInputStreamLoad {
    private static final Logger LOGGER = LogManager.getLogger(FilesInputStreamLoad.class);

    
    // why a list of inputstream ?
    // It is necessary to close the streams correctly
    // as tar gz ou zip readers are composed by severals stream that must be individually close
    // We can't use try resource with interface but it is the same deal with try-ressource : stream must be declared one by one
    private List<InputStream> tmpInxChargement;

    private List<InputStream> tmpInxNormage;

    private List<InputStream> tmpInxCSV;
    
    private File theFileToRead;
    
    // not really nice but ZipFIle need be close, not only the underlying InputStream...
	private ZipFile zipFileChargement;
	private ZipFile zipFileNormage;
	private ZipFile zipFileCSV;
    
    
    public FilesInputStreamLoad(File theFileToRead) throws IOException {
	super();
	try {	
	    tmpInxChargement = new ArrayList<>();
	    tmpInxNormage = new ArrayList<>();
	    tmpInxCSV = new ArrayList<>();
	    
		setTmpInxChargement(new FileInputStream(theFileToRead));
		setTmpInxChargement(new BufferedInputStream(getTmpInxChargement(),CompressedUtils.READ_BUFFER_SIZE));
		setTmpInxChargement(new GZIPInputStream(getTmpInxChargement()));
		
		setTmpInxNormage(new FileInputStream(theFileToRead));
		setTmpInxNormage(new BufferedInputStream(getTmpInxNormage(),CompressedUtils.READ_BUFFER_SIZE));
		setTmpInxNormage(new GZIPInputStream(getTmpInxNormage()));
		
		setTmpInxCSV(new FileInputStream(theFileToRead));
		setTmpInxCSV(new BufferedInputStream(getTmpInxCSV(),CompressedUtils.READ_BUFFER_SIZE));
		setTmpInxCSV(new GZIPInputStream(getTmpInxCSV()));
		
	} catch (FileNotFoundException e) {
	    StaticLoggerDispatcher.error(LOGGER, "Can't instanciate FilesInputStreamLoad for file " + theFileToRead.getName());
	    throw e;
	}
	this.theFileToRead = theFileToRead;
    }
    
    public FilesInputStreamLoad() {
	super();
	    tmpInxChargement = new ArrayList<>();
	    tmpInxNormage = new ArrayList<>();
	    tmpInxCSV = new ArrayList<>();
    }
    
    public FilesInputStreamLoad(ZipFile zipFileChargement, ZipFile zipFileNormage, ZipFile zipFileCSV) {
		super();
	    tmpInxChargement = new ArrayList<>();
	    tmpInxNormage = new ArrayList<>();
	    tmpInxCSV = new ArrayList<>();
		this.zipFileChargement = zipFileChargement;
		this.zipFileNormage = zipFileNormage;
		this.zipFileCSV = zipFileCSV;
	}

	public void closeAll() throws IOException {
	 try {
		 close(tmpInxChargement);
		 close(tmpInxNormage);
		 close(tmpInxCSV);
		 close(zipFileChargement); 
		 close(zipFileNormage); 
		 close(zipFileCSV);  
	} catch (IOException e) {
	    StaticLoggerDispatcher.error(LOGGER, "Can't close all FilesInputStreamLoad for file " + theFileToRead.getName());
	    throw e;
	}
    }
    
    public InputStream getTmpInxChargement() {
        return tmpInxChargement.get(tmpInxChargement.size()-1);
    }
    public void setTmpInxChargement(InputStream tmpInxChargement) {
        this.tmpInxChargement.add(tmpInxChargement);
    }
    public InputStream getTmpInxNormage() {
        return tmpInxNormage.get(tmpInxNormage.size()-1);
    }
    public void setTmpInxNormage(InputStream tmpInxNormage) {
        this.tmpInxNormage.add(tmpInxNormage);
    }
    public InputStream getTmpInxCSV() {
        return tmpInxCSV.get(tmpInxCSV.size()-1);
    }
    public void setTmpInxCSV(InputStream tmpInxCSV) {
        this.tmpInxCSV.add(tmpInxCSV);
    }
    public File getTheFileToRead() {
        return theFileToRead;
    }
    public void setTheFileToRead(File theFileToRead) {
        this.theFileToRead = theFileToRead;
    }
    
    /**
     * close inputStreams
     */
    private void close (List<InputStream> inputStreamEntries) throws IOException
    {
    	// order is important
    	for (int i=inputStreamEntries.size()-1;i>-1;i--)
    	{
    		if (inputStreamEntries.get(i)!=null)
    		{
    			inputStreamEntries.get(i).close();
    		}
    	}
    }

    /**
     * close ZipFIle
     */
    private void close(ZipFile zipfile) throws IOException
    {
    	if (zipfile!=null)
    	{
    		zipfile.close();
    	}
    }
    
}
