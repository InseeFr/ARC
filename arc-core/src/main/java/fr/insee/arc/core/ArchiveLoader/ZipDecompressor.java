package fr.insee.arc.core.ArchiveLoader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.core.util.StaticLoggerDispatcher;

public class ZipDecompressor implements ArchiveExtractor {

    private static final Logger LOGGER = LogManager.getLogger(ZipDecompressor.class);

    @Override
    public void extract(File archiveFile) throws Exception {
	StaticLoggerDispatcher.info("decompress()" + archiveFile.getName(), LOGGER);
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
			StaticLoggerDispatcher.error("Unable to create directory '%s', during extraction of archive contents.\n",
				LOGGER);
		    }
		} else {
		    int count;
		    byte data[] = new byte[32738];
		    
		    // temporary name for the file being uncompress
		    try(
			    FileOutputStream fos = new FileOutputStream(
				    dir.getAbsolutePath() + File.separator + ManipString.redoEntryName(entry.getName()) + ".tmp", false);
			    BufferedOutputStream dest = new BufferedOutputStream(fos, 32738);
			    GZIPOutputStream zdest=new GZIPOutputStream(dest);
		    		)
		    {
			while ((count = zipIn.read(data, 0, 32738)) != -1) {
				zdest.write(data, 0, count);
				}
	    	}
		    
		    // rename the file when over makes it thread safe and available for other threads waiting
		    new File( dir.getAbsolutePath() + File.separator + ManipString.redoEntryName(entry.getName()) + ".tmp")
				.renameTo(new File(dir.getAbsolutePath() + File.separator + ManipString.redoEntryName(entry.getName())));
		
		}
	    }

	}

	StaticLoggerDispatcher.info("Untar completed successfully!", LOGGER);

    }

}
