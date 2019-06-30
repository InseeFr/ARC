package fr.insee.arc.core.archive_loader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.log4j.Logger;

import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.ManipString;

/**
 * Can decompress tarGZ archive file
 * 
 * @author S4LWO8
 *
 */
public class TarGzDecompressor implements ArchiveExtractor {
    private static final Logger LOGGER = Logger.getLogger(TarGzDecompressor.class);

    @Override
    public void extract(File archiveFile) throws IOException  {
	LoggerDispatcher.info("decompress()" + archiveFile.getName(), LOGGER);
	FileInputStream archiveInputStream = null;

	archiveInputStream = new FileInputStream(archiveFile);
	File dir = new File(archiveFile + ".dir");

	GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(archiveInputStream);
	try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
	    TarArchiveEntry entry;

	    while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
		/** If the entry is a directory, create the directory. **/
		if (entry.isDirectory()) {
		    File f = new File(entry.getName());
		    boolean created = f.mkdir();
		    if (!created) {
			LoggerDispatcher.info(String.format("Unable to create directory '%s', during extraction of archive contents.%n",
				f.getAbsolutePath()), LOGGER);
		    }
		} else {
		    int count;
		    byte[] data = new byte[32738];
		    FileOutputStream fos = new FileOutputStream(
			    dir.getAbsolutePath() + File.separator + ManipString.redoEntryName(entry.getName()), false);
		    try (BufferedOutputStream dest = new BufferedOutputStream(fos, 32738)) {
			while ((count = tarIn.read(data, 0, 32738)) != -1) {
			    dest.write(data, 0, count);
			}
		    }
		}
	    }

	}

	LoggerDispatcher.info("Untar completed successfully!", LOGGER);

    }

}
