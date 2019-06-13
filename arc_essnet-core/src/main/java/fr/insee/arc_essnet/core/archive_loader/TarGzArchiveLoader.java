package fr.insee.arc_essnet.core.archive_loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.log4j.Logger;

import fr.insee.arc_essnet.core.service.SizeLimiterInputStream;
import fr.insee.arc_essnet.utils.utils.LoggerDispatcher;

/**
 * The targz archive loader
 * 
 * @author S4LWO8
 *
 */
public class TarGzArchiveLoader extends AbstractArchiveFileLoader {

    private static final Logger LOGGER = Logger.getLogger(TarGzArchiveLoader.class);

    public TarGzArchiveLoader(File fileChargement, String idSource) {
	super(fileChargement, idSource);
	this.fileDecompresor = new TarGzDecompressor();

    }

    @Override
    public FilesInputStreamLoad readFileWithoutExtracting(FilesInputStreamLoadKeys[] streamNames) throws IOException {
	LoggerDispatcher.info("begin readFileWithoutExtracting() ", LOGGER);

	this.filesInputStreamLoad = new FilesInputStreamLoad();
	for (FilesInputStreamLoadKeys aStreamName : streamNames) {
	    LoggerDispatcher.info(String.format("Create stream %s", aStreamName), LOGGER);
	    TarArchiveInputStream tarInput = new TarArchiveInputStream(
		    new GZIPInputStream(new FileInputStream(this.archiveToLoad)));

	    this.filesInputStreamLoad.getMapInputStream().put(aStreamName//
		    , new SizeLimiterInputStream(//
			    tarInput//
			    , tarInput.getNextTarEntry().getSize()));

	}

	LoggerDispatcher.info("end readFileWithoutExtracting() ", LOGGER);
	return filesInputStreamLoad;

    }

    @Override
    public FilesInputStreamLoad loadArchive(FilesInputStreamLoadKeys[] streamNames)
	    throws IOException, InterruptedException {
	LoggerDispatcher.info("begin loadArchive() ", LOGGER);
	TarArchiveInputStream tarInput = new TarArchiveInputStream(//
		new GZIPInputStream(//
			new FileInputStream(this.archiveToLoad)));

	// Optimization : if the archive contains more file than the number of thread =>
	// extraction
	if (countNbEntries(tarInput) > THREAD_NUMBER) {
	    extractArchive(fileDecompresor);
	    this.filesInputStreamLoad = readFile(streamNames);
	} else {
	    readFileWithoutExtracting(streamNames);
	}

	LoggerDispatcher.info("end loadArchive() ", LOGGER);
	return this.filesInputStreamLoad;

    }

    public int countNbEntries(TarArchiveInputStream tarInput) throws IOException {
	int count = 0;
	while (null != (tarInput.getNextTarEntry())) {
	    count++;
	}
	return count;

    }

}
