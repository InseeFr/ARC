package fr.insee.arc.core.archive_loader;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Pépin Rémi
 *
 */
public interface ArchiveExtractor {

    /**
     * 
     * @param archiveFile the archive file we want to decompress
     * @param in
     */
    public void extract(File archiveFile) throws IOException;
    
}
