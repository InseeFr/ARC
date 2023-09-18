package fr.insee.arc.core.service.p2chargement.archiveloader;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.ManipString;

/**
 * The Zip archive loader
 */
public class ZipArchiveLoader extends AbstractArchiveFileLoader {

    private static final Logger LOGGER = LogManager.getLogger(ZipArchiveLoader.class);

    public ZipArchiveLoader(File fileChargement, String idSource) {
	super(fileChargement, idSource);
	this.fileDecompresor= new ZipDecompressor();

    }

    @Override
	public FilesInputStreamLoad loadArchive() throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "begin loadArchive() ");
		readFileWithoutExtracting();
		StaticLoggerDispatcher.info(LOGGER, "end loadArchive() ");
		return this.filesInputStreamLoad;
	}

    @SuppressWarnings("resource")
    @Override
    public FilesInputStreamLoad readFileWithoutExtracting() throws ArcException {
	StaticLoggerDispatcher.info(LOGGER, "begin readFileWithoutExtracting() ");
	ZipFile zipFileChargement;
	try {
		zipFileChargement = new ZipFile(this.archiveChargement);
		ZipFile zipFileNormage = new ZipFile(this.archiveChargement);
		ZipFile zipFileCSV = new ZipFile(this.archiveChargement);

		this.filesInputStreamLoad = new FilesInputStreamLoad();

		// seek the file inside archive to be loaded
		// idSource format is datastorage_filename
		// thus the real filename can be found as the second token of idSource
		
		String entryName = ManipString.substringAfterFirst(this.idSource, "_");
		
		this.filesInputStreamLoad.setTmpInxChargement(zipFileChargement
			.getInputStream(zipFileChargement.getEntry(entryName)));
		this.filesInputStreamLoad.setTmpInxCSV(zipFileNormage
			.getInputStream(zipFileNormage.getEntry(entryName)));
		this.filesInputStreamLoad.setTmpInxNormage(
			zipFileCSV.getInputStream(zipFileCSV.getEntry(entryName)));
		
	} catch (IOException ioReadException) {
		throw new ArcException(ioReadException, ArcExceptionMessage.FILE_READ_FAILED, this.idSource);
	}
		
	StaticLoggerDispatcher.info(LOGGER, "end readFileWithoutExtracting() ");
	return filesInputStreamLoad;

    }

}
