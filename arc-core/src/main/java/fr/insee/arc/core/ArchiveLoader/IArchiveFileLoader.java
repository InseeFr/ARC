package fr.insee.arc.core.ArchiveLoader;

/**
 * Interface which centralizes methode to extract files
 */
public interface IArchiveFileLoader {
    /**
     * Unzip archive file.
     * @param extractor how the archive is extract
     * @throws Exception
     */
    public void extractArchive(ArchiveExtractor extractor) throws Exception;
    
    /**
     * Read the file without extracting it
     * @return all necessary inputstream
     * @throws Exception
     */
    public FilesInputStreamLoad readFileWithoutExtracting() throws Exception;
    
    /**
     * Read the file after extracting it
     * @return all necessary inputstream
     * @throws Exception
     */
    public FilesInputStreamLoad readFile() throws Exception;

    
    /**
     * Load the archive file. Call all the previous method if necessary (ie extract it or not) and return
     *  all necessary inputstream
     * @return all necessary inputstream
     * @throws Exception
     */
    public FilesInputStreamLoad loadArchive() throws Exception ;
}
