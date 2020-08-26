package fr.insee.arc.core.ArchiveLoader;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.utils.LoggerDispatcher;


/**
 * Handle .gz archive
 */
public class GZArchiveLoader extends AbstractArchiveFileLoader {

    public GZArchiveLoader(File fileChargement, String idSource) {
	super(fileChargement, idSource);
    }

    private static final Logger LOGGER = LogManager.getLogger(GZArchiveLoader.class);

    public FilesInputStreamLoad readFileWithoutExtracting() throws Exception {
	LoggerDispatcher.info("begin readFileWithoutExtracting() ", LOGGER);
	this.filesInputStreamLoad = new FilesInputStreamLoad();

	// Loading
	this.filesInputStreamLoad.setTmpInxChargement(new GZIPInputStream(new FileInputStream(this.archiveChargement)));
	this.filesInputStreamLoad.setTmpInxCSV(new GZIPInputStream(new FileInputStream(this.archiveChargement)));
	this.filesInputStreamLoad.setTmpInxNormage(new GZIPInputStream(new FileInputStream(this.archiveChargement)));

	LoggerDispatcher.info("end readFileWithoutExtracting() ", LOGGER);
	return filesInputStreamLoad;

    }

    @Override
    public FilesInputStreamLoad loadArchive() throws Exception {
	LoggerDispatcher.info("begin loadArchive() ", LOGGER);

	readFileWithoutExtracting();

	LoggerDispatcher.info("end loadArchive() ", LOGGER);
	return this.filesInputStreamLoad;

    }

}
