package fr.insee.arc_essnet.core.archive_loader;

import java.io.IOException;

import fr.insee.config.InseeConfig;


/**
 * Iterface which centralises methode to extract files
 * 
 * @author Pépin Rémi
 *
 */
public interface IArchiveFileLoader {
    
    /**
     * If there is more than THREAD_NUMBER file in archive file it will be extract
     */
    public static final int THREAD_NUMBER = InseeConfig.getConfig().getInt("fr.insee.arc.threads.chargement");
    

    /**
     * Unzip archive file.
     * @param extractor how the archive is extract
     * @throws Exception
     */
    public void extractArchive(ArchiveExtractor extractor) throws IOException, InterruptedException;
    
    /**
     * Read the file without extracting it
     * @return all necessary inputstream
     * @throws Exception
     */
    public FilesInputStreamLoad readFileWithoutExtracting(FilesInputStreamLoadKeys[] streamName) throws IOException;
    
    /**
     * Read the file after extracting it
     * @return all necessary inputstream
     * @throws Exception
     */
    public FilesInputStreamLoad readFile(FilesInputStreamLoadKeys[] streamName) throws IOException;

    
    /**
     * Load the archive file. Call all the previous method if necessary (ie extract it or not) and return
     *  all necessary inputstream
     * @return all necessary inputstream
     * @throws Exception
     */
    public FilesInputStreamLoad loadArchive(FilesInputStreamLoadKeys[] streamName) throws IOException, InterruptedException;
}
