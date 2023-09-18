package fr.insee.arc.core.service.p2chargement.archiveloader;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author rémi pépin
 *
 */
public interface IArchiveExtractor {

    /**
     * 
     * @param archiveFile the archive file we want to decompress
     * @param in
     */
    public void extract(File archiveFile) throws IOException;
    
}
