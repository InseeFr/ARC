package fr.insee.arc.core.archive_loader;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.insee.arc.core.model.TypeArchive;
import fr.insee.arc.utils.utils.LoggerDispatcher;


/**
 * Factory to choose the good loader
 * @author S4LWO8
 *
 */
public class ArchiveLoaderFactory {
    private Map<TypeArchive, IArchiveFileLoader> map = new EnumMap<>(TypeArchive.class);
    private static final Logger LOGGER = Logger.getLogger(ArchiveLoaderFactory.class);


    public ArchiveLoaderFactory(File fileChargement, String fileName) {
        this.map.put(TypeArchive.ZIP,
               new ZipArchiveLoader(fileChargement, fileName));
        this.map.put(TypeArchive.TARGZ,
        	new TarGzArchiveLoader(fileChargement, fileName));
        this.map.put(TypeArchive.GZ,
        	new GZArchiveLoader(fileChargement, fileName));

    }
    
    public IArchiveFileLoader getLoader(TypeArchive typeArchive){
        LoggerDispatcher.info("** getLoader from type **", LOGGER);
        return this.map.get(typeArchive);
    }

    
    public IArchiveFileLoader getLoader(String container){
        LoggerDispatcher.info("** getChargeur from container**", LOGGER);
        IArchiveFileLoader returned = null;
	    if (container.endsWith(".tar.gz") || container.endsWith(".tgz")) {
		returned = getLoader(TypeArchive.TARGZ);

	    } else if (container.endsWith(".gz")) {
		returned = getLoader(TypeArchive.GZ);

	    } else if (container.endsWith(".zip")) {
		returned = getLoader(TypeArchive.ZIP);
	    }
	    return returned;
    }
}