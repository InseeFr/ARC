package fr.insee.arc.core.ArchiveLoader;

import java.io.IOException;

import fr.insee.arc.utils.exception.ArcException;

/**
 * Interface which centralizes methode to extract files
 */
public interface IArchiveFileLoader {
    /**
     * Unzip archive file.
     * @param extractor how the archive is extract
     * @throws ArcException
     */
    public void extractArchive(ArchiveExtractor extractor) throws ArcException;
    
    /**
     * Read the file without extracting it
     * @return all necessary inputstream
     * @throws ArcException
     */
    public FilesInputStreamLoad readFileWithoutExtracting() throws ArcException;
    
    /**
     * Read the file after extracting it
     * @return all necessary inputstream
     * @throws ArcException
     */
    public FilesInputStreamLoad readFile() throws ArcException;

    
    /**
     * Load the archive file. Call all the previous method if necessary (ie extract it or not) and return
     *  all necessary inputstream
     * @return all necessary inputstream
     * @throws ArcException
     */
    public FilesInputStreamLoad loadArchive() throws ArcException ;
}
