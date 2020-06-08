package fr.insee.arc.core.ArchiveLoader;

import java.io.File;

/**
 * 
 * @author P�pin R�mi
 *
 */
public interface ArchiveExtractor {

    /**
     * 
     * @param archiveFile the archive file we want to decompress
     * @param in
     */
    public void extract(File archiveFile) throws Exception;
    
}
