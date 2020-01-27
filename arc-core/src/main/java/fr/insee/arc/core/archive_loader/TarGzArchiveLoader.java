package fr.insee.arc.core.archive_loader;

import java.io.File;
import java.io.IOException;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.log4j.Logger;

import fr.insee.arc.utils.utils.LoggerDispatcher;

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
		return null;
	}
	
	@Override
	public FilesInputStreamLoad loadArchive(FilesInputStreamLoadKeys[] streamNames)
			throws IOException, InterruptedException {
		LoggerDispatcher.info("begin loadArchive() ", LOGGER);

		// Mandatory for multithreading to decompress tar.gz archive
		// as it is not possible to address a specific entry in targz
		extractArchive(fileDecompresor);
		this.filesInputStreamLoad = readFile(streamNames);

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
