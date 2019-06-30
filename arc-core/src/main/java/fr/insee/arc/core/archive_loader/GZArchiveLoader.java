package fr.insee.arc.core.archive_loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

import fr.insee.arc.utils.utils.LoggerDispatcher;


/**
 * Handle .gz archive
 * @author S4LWO8
 *
 */
public class GZArchiveLoader extends AbstractArchiveFileLoader {

    public GZArchiveLoader(File fileChargement, String idSource) {
	super(fileChargement, idSource);
    }

    private static final Logger LOGGER = Logger.getLogger(GZArchiveLoader.class);

    public FilesInputStreamLoad readFileWithoutExtracting(FilesInputStreamLoadKeys[] streamNames) throws IOException  {
	LoggerDispatcher.info("begin readFileWithoutExtracting() ", LOGGER);
	
	this.filesInputStreamLoad = new FilesInputStreamLoad();

	// Open stream
	for (FilesInputStreamLoadKeys aStreamName : streamNames) {
	    this.filesInputStreamLoad.getMapInputStream().put(aStreamName, new GZIPInputStream(new FileInputStream(this.archiveToLoad)));
	}

	LoggerDispatcher.info("end readFileWithoutExtracting() ", LOGGER);
	return filesInputStreamLoad;

    }
    
    @Override
    public FilesInputStreamLoad loadArchive(FilesInputStreamLoadKeys[] streamNames) throws IOException {
	LoggerDispatcher.info("begin loadArchive() ", LOGGER);

	readFileWithoutExtracting(streamNames);

	LoggerDispatcher.info("end loadArchive() ", LOGGER);
	return this.filesInputStreamLoad;

    }

}
