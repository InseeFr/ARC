package fr.insee.arc.core.ArchiveLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.service.api.ApiReceptionService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;


/**
 * Handle .gz archive
 */
public class GZArchiveLoader extends AbstractArchiveFileLoader {

    public GZArchiveLoader(File fileChargement, String idSource) {
	super(fileChargement, idSource);
    }

    private static final Logger LOGGER = LogManager.getLogger(GZArchiveLoader.class);

    public FilesInputStreamLoad readFileWithoutExtracting() throws ArcException {
	StaticLoggerDispatcher.info("begin readFileWithoutExtracting() ", LOGGER);
	this.filesInputStreamLoad = new FilesInputStreamLoad();

	// Loading
	try {
		this.filesInputStreamLoad.setTmpInxChargement(new GZIPInputStream(new BufferedInputStream(new FileInputStream(this.archiveChargement),ApiReceptionService.READ_BUFFER_SIZE)));
		this.filesInputStreamLoad.setTmpInxCSV(new GZIPInputStream(new BufferedInputStream(new FileInputStream(this.archiveChargement),ApiReceptionService.READ_BUFFER_SIZE)));
		this.filesInputStreamLoad.setTmpInxNormage(new GZIPInputStream(new BufferedInputStream(new FileInputStream(this.archiveChargement),ApiReceptionService.READ_BUFFER_SIZE)));
	} catch (IOException ioReadException) {
		throw new ArcException(ioReadException, ArcExceptionMessage.FILE_READ_FAILED, this.archiveChargement);
	}
	
	StaticLoggerDispatcher.info("end readFileWithoutExtracting() ", LOGGER);
	return filesInputStreamLoad;

    }

    @Override
    public FilesInputStreamLoad loadArchive() throws ArcException {
	StaticLoggerDispatcher.info("begin loadArchive() ", LOGGER);

	readFileWithoutExtracting();

	StaticLoggerDispatcher.info("end loadArchive() ", LOGGER);
	return this.filesInputStreamLoad;

    }

}
