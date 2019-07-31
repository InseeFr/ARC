package fr.insee.arc.core.archive_loader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

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
		    
		    // temporary name for the file being uncompress
		    FileOutputStream fos = new FileOutputStream(
			    dir.getAbsolutePath() + File.separator + ManipString.redoEntryName(entry.getName()) + ".tmp", false);
		    BufferedOutputStream dest = new BufferedOutputStream(fos, 32738);
		    GZIPOutputStream zdest=new GZIPOutputStream(dest);
		    try {
			while ((count = zipIn.read(data, 0, 32738)) != -1) {
				zdest.write(data, 0, count);
				}
	    	} finally {
	    		zdest.close();
	    		fos.close();
		    }
		    
		    // rename the file when over makes it thread safe and available for other threads waiting
		    new File( dir.getAbsolutePath() + File.separator + ManipString.redoEntryName(entry.getName()) + ".tmp")
				.renameTo(new File(dir.getAbsolutePath() + File.separator + ManipString.redoEntryName(entry.getName())));
		
		}
	    }

	}

	LoggerDispatcher.info("Untar completed successfully!", LOGGER);

    }

}
