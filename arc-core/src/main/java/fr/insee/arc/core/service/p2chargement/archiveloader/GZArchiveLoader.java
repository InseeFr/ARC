package fr.insee.arc.core.service.p2chargement.archiveloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.CompressedUtils;


/**
 * Handle .gz archive
 */
public class GZArchiveLoader extends AbstractArchiveFileLoader {

    public GZArchiveLoader(File fileChargement, String idSource) {
	super(fileChargement, idSource);
    }

    private static final Logger LOGGER = LogManager.getLogger(GZArchiveLoader.class);

    public FilesInputStreamLoad readFileWithoutExtracting() throws ArcException {
	StaticLoggerDispatcher.info(LOGGER, "begin readFileWithoutExtracting() ");
	this.filesInputStreamLoad = new FilesInputStreamLoad();

	// Loading
	try {
		this.filesInputStreamLoad.setTmpInxChargement(new GZIPInputStream(new BufferedInputStream(new FileInputStream(this.archiveChargement),CompressedUtils.READ_BUFFER_SIZE)));
		this.filesInputStreamLoad.setTmpInxCSV(new GZIPInputStream(new BufferedInputStream(new FileInputStream(this.archiveChargement),CompressedUtils.READ_BUFFER_SIZE)));
		this.filesInputStreamLoad.setTmpInxNormage(new GZIPInputStream(new BufferedInputStream(new FileInputStream(this.archiveChargement),CompressedUtils.READ_BUFFER_SIZE)));
	} catch (IOException ioReadException) {
		throw new ArcException(ioReadException, ArcExceptionMessage.FILE_READ_FAILED, this.archiveChargement);
	}
	
	StaticLoggerDispatcher.info(LOGGER, "end readFileWithoutExtracting() ");
	return filesInputStreamLoad;

    }

    @Override
    public FilesInputStreamLoad loadArchive() throws ArcException {
	StaticLoggerDispatcher.info(LOGGER, "begin loadArchive() ");

	readFileWithoutExtracting();

	StaticLoggerDispatcher.info(LOGGER, "end loadArchive() ");
	return this.filesInputStreamLoad;

    }

}
