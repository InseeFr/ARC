package fr.insee.arc.core.archive_loader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.log4j.Logger;

import fr.insee.arc.utils.utils.LoggerDispatcher;
import fr.insee.arc.utils.utils.ManipString;

public class ZipDecompressor implements ArchiveExtractor {

    private static final Logger LOGGER = Logger.getLogger(ZipDecompressor.class);

    @Override
    public void extract(File archiveFile) throws IOException {
	LoggerDispatcher.info("decompress()" + archiveFile.getName(), LOGGER);
	FileInputStream archiveInputStream = null;

	archiveInputStream = new FileInputStream(archiveFile);
	File dir = new File(archiveFile + ".dir");

	try (ZipArchiveInputStream zipIn = new ZipArchiveInputStream(archiveInputStream)) {
	    ZipArchiveEntry entry;

	    while ((entry = (ZipArchiveEntry) zipIn.getNextEntry()) != null) {
		/** If the entry is a directory, create the directory. **/
		if (entry.isDirectory()) {
		    File f = new File(entry.getName());
		    boolean created = f.mkdir();
		    if (!created) {
			LoggerDispatcher.error("Unable to create directory '%s', during extraction of archive contents.\n",
				LOGGER);
		    }
		} else {
		    int count;
		    byte data[] = new byte[32738];
		    FileOutputStream fos = new FileOutputStream(
			    dir.getAbsolutePath() + File.separator + ManipString.redoEntryName(entry.getName()), false);
		    try (BufferedOutputStream dest = new BufferedOutputStream(fos, 32738)) {
			while ((count = zipIn.read(data, 0, 32738)) != -1) {
			    dest.write(data, 0, count);
			}
		    }
		    LoggerDispatcher.info("File " + ManipString.redoEntryName(entry.getName()) + "is created ", LOGGER);
		}
	    }

	}

	LoggerDispatcher.info("Untar completed successfully!", LOGGER);

    }

}
