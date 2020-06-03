package fr.insee.arc.core.ArchiveLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.log4j.Logger;

import fr.insee.arc.core.service.engine.SizeLimiterInputStream;
import fr.insee.arc.utils.utils.LoggerDispatcher;


/**
 * The targz archive loader
 */
public class TarGzArchiveLoader extends AbstractArchiveFileLoader {

    private static final Logger LOGGER = Logger.getLogger(TarGzArchiveLoader.class);

    public TarGzArchiveLoader(File fileChargement, String idSource) {
	super(fileChargement, idSource);
	this.fileDecompresor= new TarGzDecompressor();

    }

    @Override
    public FilesInputStreamLoad readFileWithoutExtracting() throws Exception {
	return null;
    }

    @Override
    public FilesInputStreamLoad loadArchive() throws Exception {
	LoggerDispatcher.info("begin loadArchive() ", LOGGER);

	// Mandatory for multithreading to decompress tar.gz archive
	// as it is not possible to address a specific entry in targz
    extractArchive(fileDecompresor);
	this.filesInputStreamLoad = readFile();

	LoggerDispatcher.info("end loadArchive() ", LOGGER);
	return this.filesInputStreamLoad;

    }


}
