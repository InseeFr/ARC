package fr.insee.arc.core.service.p2chargement.archiveloader;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.ManipString;

/**
 * Loader have to inherit this class
 */
public abstract class AbstractArchiveFileLoader implements IArchiveFileLoader {
    private static final Logger LOGGER = LogManager.getLogger(AbstractArchiveFileLoader.class);

    /**
     * Archive file to be processed 
     */
    protected File archiveChargement;
    /**
     * Name of the file inside the archive to be loaded
     */
    protected String idSource;
    
    /**
     * All necessary input streams wrapped in a class
     */
    protected FilesInputStreamLoad filesInputStreamLoad;
    
    /**
     * How the archive is extract
     */
    protected IArchiveExtractor fileDecompresor;

    
    /**
     * the program will check every MILLISECOND_DECOMPRESSION_CHECK_INTERVAL to see if files are uncompressed 
     */
    public static final long MILLISECOND_UNCOMPRESSION_CHECK_INTERVAL=500;
    
    
    public AbstractArchiveFileLoader(File fileChargement, String idSource) {
	super();
	this.archiveChargement = fileChargement;
	this.idSource = idSource;
    }
    
    /**
     * Extract the archive. Multithread application, so multiple thread may be
     * extracting the archive at the same time. And <b>WE DO NOT WANT THAT</b> ! So before
     * extracting we check if the directory exists.
     * <ul>
     * <li>do not exists : extraction</li>
     * <li>exists : a thread is already extracting. Wait for the file to be create</li<
     * </ul>
     * Note : an archive can contains multiple file, that's why multiple thread can
     * extract the archive, each one want its file.
     * 
     * @param decompressor
     *            : the way to extract
     * @throws ArcException 
     * @throws IOException 
     * @throws InterruptedException 
     */
    @Override
    public void extractArchive(IArchiveExtractor decompressor) throws ArcException {

	String fileName = ManipString.substringAfterFirst(this.idSource, "_");

	boolean uncompressInProgress=false;
	File dir = new File(this.archiveChargement + ".dir");
	if (!dir.exists()) {
		try {
		    if (dir.mkdir())
		    {
		    	StaticLoggerDispatcher.debug(LOGGER,"$$"+Thread.currentThread().getId()+" is decompressing "+dir.getAbsolutePath());
		    	decompressor.extract(this.archiveChargement);
		    	uncompressInProgress=true;
		    }
		}
		 catch (Exception ex)
		{
			 throw new ArcException(ex, ArcExceptionMessage.FILE_EXTRACT_FAILED, this.archiveChargement).logFullException();
		}
	}
	
	if (!uncompressInProgress) {
			// check if file exists
	    File toRead = new File(dir + File.separator + ManipString.redoEntryName(fileName));
	    while (!toRead.exists()) {
	    	try {
				Thread.sleep(MILLISECOND_UNCOMPRESSION_CHECK_INTERVAL);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			    StaticLoggerDispatcher.error(LOGGER, "Error in thread sleep extractArchive()");
			}
	    	toRead = new File(dir + File.separator + ManipString.redoEntryName(fileName));
	    }
	}


	
    }
    
    /**
     * Open input stream on the file corresponding to idSource
     * 
     * @param streamName
     *            : name of the keys in the FilesInputStreamLoad to create the
     *            input stream
     * @return a FilesInputStreamLoad wrapping the needed input stream
     * @throws IOException 
     */
    public FilesInputStreamLoad readFile() throws ArcException {
	File dir = new File(this.archiveChargement + ".dir");
	String fileName = ManipString.substringAfterFirst(this.idSource, "_");
	File toRead = new File(dir + File.separator + ManipString.redoEntryName(fileName));
	FilesInputStreamLoad filesInputStreamLoadReturned = null;
	try {
		filesInputStreamLoadReturned = new FilesInputStreamLoad (toRead);
	} catch (IOException ioReadException) {
		throw new ArcException(ioReadException, ArcExceptionMessage.FILE_READ_FAILED, toRead);
	}
	return filesInputStreamLoadReturned;
    }

    
    
    public File getFileChargement() {
        return archiveChargement;
    }
    public void setFileChargement(File fileChargement) {
        this.archiveChargement = fileChargement;
    }
    public String getIdSource() {
        return idSource;
    }
    public void setIdSource(String idSource) {
        this.idSource = idSource;
    }
    
    
    
}
