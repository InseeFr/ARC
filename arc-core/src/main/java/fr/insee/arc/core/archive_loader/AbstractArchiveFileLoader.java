package fr.insee.arc.core.archive_loader;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.ManipString;



/**
 * Loader have to inherit this class
 * 
 * @author S4LWO8
 *
 */
public abstract class AbstractArchiveFileLoader implements IArchiveFileLoader {
    private static final Logger LOGGER = Logger.getLogger(AbstractArchiveFileLoader.class);

    protected File archiveToLoad;
    protected String idSourceInArchive;

    /**
     * All necessary inpustream wraped in a class
     */
    protected FilesInputStreamLoad filesInputStreamLoad;

    /**
     * How the archive is extract
     */
    protected ArchiveExtractor fileDecompresor;

    public AbstractArchiveFileLoader(File fileChargement, String idSource) {
	super();
	this.archiveToLoad = fileChargement;
	this.idSourceInArchive = idSource;
    }

    /**
     * Extract the archive. Multithread application, so multiple thread may be
     * extracting the archive at the same time. And <b>WE DO NOT WANT THAT</b> ! So before
     * extracting we check if the directory exists.
     * <ul>
     * <li>do not exists : extraction</li>
     * <li>exists : a thread is already exctracting. Wait for the file to be create</li<
     * </ul>
     * Note : an archive can contains multiple file, that's why multiple thread can
     * extract the archive, each one want its file.
     * 
     * @param decompressor
     *            : the way to extract
     * @throws IOException 
     * @throws InterruptedException 
     * @throws Exception 
     */
    @Override
    public void extractArchive(ArchiveExtractor decompressor) throws IOException, InterruptedException  {

	String fileName = ManipString.substringAfterFirst(this.idSourceInArchive, "_");

	File dir = new File(this.archiveToLoad + ".dir");
	if (!dir.exists()) {
	    LoggerDispatcher.info(String.format("directory %s do not exist, have to extract",dir.getName()), LOGGER);
	    dir.mkdir();
	    decompressor.extract(this.archiveToLoad);
	} else {
	    File toRead = new File(dir + File.separator + ManipString.redoEntryName(fileName));
	    LoggerDispatcher.info(String.format("directory %s exists, wait for file %s to be created",dir.getName(), toRead.getName()), LOGGER);
	    while (!toRead.exists()) {
		toRead = new File(dir + File.separator + ManipString.redoEntryName(fileName));
		Thread.sleep(500);
	    }
	}

    }

    /**
     * Open inpustream to a file 
     * 
     * @param streamName
     *            : name of the keys in the FilesInputStreamLoad to create the
     *            inpustream
     * @return a FilesInputStreamLoad wrapping the needed inpustream
     * @throws Exception
     */
    public FilesInputStreamLoad readFile(FilesInputStreamLoadKeys[] streamName) throws IOException {
	File dir = new File(this.archiveToLoad + ".dir");
	String fileName = ManipString.substringAfterFirst(this.idSourceInArchive, "_");
	File toRead = new File(dir + File.separator + ManipString.redoEntryName(fileName));
	FilesInputStreamLoad output = null;
	try {
	    output = new FilesInputStreamLoad(toRead, streamName);
	} catch (Exception ex) {
	    LoggerDispatcher.error("loadArchive() " + ex, LOGGER);
	}
	return output;
    }

    public File getFileChargement() {
	return archiveToLoad;
    }

    public void setFileChargement(File fileChargement) {
	this.archiveToLoad = fileChargement;
    }

    public String getIdSource() {
	return idSourceInArchive;
    }

    public void setIdSource(String idSource) {
	this.idSourceInArchive = idSource;
    }

}
