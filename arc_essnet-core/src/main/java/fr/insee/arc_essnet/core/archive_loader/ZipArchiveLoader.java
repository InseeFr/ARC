package fr.insee.arc_essnet.core.archive_loader;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import fr.insee.arc_essnet.utils.utils.LoggerDispatcher;
import fr.insee.arc_essnet.utils.utils.ManipString;

/**
 * The Zip archive loader
 * 
 * @author S4LWO8
 *
 */
public class ZipArchiveLoader extends AbstractArchiveFileLoader {

    private static final Logger LOGGER = Logger.getLogger(ZipArchiveLoader.class);

    public ZipArchiveLoader(File fileChargement, String idSource) {
	super(fileChargement, idSource);
	this.fileDecompresor = new ZipDecompressor();

    }

    @Override
    public FilesInputStreamLoad loadArchive(FilesInputStreamLoadKeys[] streamNames)
	    throws IOException, InterruptedException {
	LoggerDispatcher.info("begin loadArchive() ", LOGGER);

	try (ZipFile zipFileChargement = new ZipFile(this.archiveToLoad)) {

	    if (zipFileChargement.size() > THREAD_NUMBER) {
		extractArchive(fileDecompresor);
		this.filesInputStreamLoad = readFile(streamNames);

	    } else {
		readFileWithoutExtracting(streamNames);
	    }
	}

	LoggerDispatcher.info("end loadArchive() ", LOGGER);
	return this.filesInputStreamLoad;

    }

    @Override
    public FilesInputStreamLoad readFileWithoutExtracting(FilesInputStreamLoadKeys[] streamNames) throws IOException {
	LoggerDispatcher.info("begin readFileWithoutExtracting() ", LOGGER);
	this.filesInputStreamLoad = new FilesInputStreamLoad();

	for (FilesInputStreamLoadKeys aStreamName : streamNames) {
	    //Need to be keep open because if the resource is closed, all stream are closed too !
	    @SuppressWarnings({"resource", "squid:S2095"})
	    ZipFile zipInput = new ZipFile(this.archiveToLoad);
		/*
		 * Looking for the file in the archive. idSourceInArchive is like repository_fileName.
		 * The name of the file is after the first _
		 */
		this.filesInputStreamLoad.getMapInputStream().put(aStreamName//
			, zipInput.getInputStream(//
				zipInput.getEntry(ManipString.substringAfterFirst(this.idSourceInArchive, "_"))));
	    }
	

	LoggerDispatcher.info("end readFileWithoutExtracting() ", LOGGER);
	return filesInputStreamLoad;

    }

}
