package fr.insee.arc.core.ArchiveLoader;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author rémi pépin
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
