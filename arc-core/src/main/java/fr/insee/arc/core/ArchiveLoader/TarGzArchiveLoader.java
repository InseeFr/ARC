package fr.insee.arc.core.ArchiveLoader;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;


/**
 * The targz archive loader
 */
public class TarGzArchiveLoader extends AbstractArchiveFileLoader {

    private static final Logger LOGGER = LogManager.getLogger(TarGzArchiveLoader.class);

    public TarGzArchiveLoader(File fileChargement, String idSource) {
	super(fileChargement, idSource);
	this.fileDecompresor= new TarGzDecompressor();

    }

    @Override
    public FilesInputStreamLoad readFileWithoutExtracting() {
	return null;
    }

    @Override
    public FilesInputStreamLoad loadArchive() throws ArcException {
	StaticLoggerDispatcher.info("begin loadArchive() ", LOGGER);

	// Mandatory for multithreading to decompress tar.gz archive
	// as it is not possible to address a specific entry in targz
    extractArchive(fileDecompresor);
	this.filesInputStreamLoad = readFile();

	StaticLoggerDispatcher.info("end loadArchive() ", LOGGER);
	return this.filesInputStreamLoad;

    }


}
