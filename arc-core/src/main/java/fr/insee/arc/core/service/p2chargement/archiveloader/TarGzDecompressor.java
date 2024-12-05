package fr.insee.arc.core.service.p2chargement.archiveloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.CompressedUtils;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.utils.ManipString;

/**
 * Can decompress tarGZ archive file
 * 
 * @author S4LWO8
 *
 */
public class TarGzDecompressor implements IArchiveExtractor {
	private static final Logger LOGGER = LogManager.getLogger(TarGzDecompressor.class);

	@Override
	public void extract(File archiveFile) throws IOException {
		StaticLoggerDispatcher.info(LOGGER, "decompress()" + archiveFile.getName());
		
		File archiveFileExtractTargetDirectory = new File(archiveFile + ".dir");

		try (FileInputStream fis = new FileInputStream(archiveFile)) {
			try (BufferedInputStream bis = new BufferedInputStream(fis, CompressedUtils.READ_BUFFER_SIZE)) {
				try (GZIPInputStream gzipIn = new GZIPInputStream(bis, CompressedUtils.READ_BUFFER_SIZE)) {
					try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn, CompressedUtils.READ_BUFFER_SIZE)) {
						extract(tarIn, archiveFileExtractTargetDirectory);
					}
				}
			}
		}
		StaticLoggerDispatcher.info(LOGGER, "Untar completed successfully!");
	}

	
	/**
	 * extract file from the TarArchiveInputStream
	 * @param tarIn
	 * @param archiveFileExtractTargetDirectory
	 * @throws IOException
	 */
	private void extract(TarArchiveInputStream tarIn, File archiveFileExtractTargetDirectory) throws IOException
	{
		TarArchiveEntry entry;

		while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
			/** If the entry is a directory, create the directory. **/

			// directories if not empty are automatically read in tar entries list
			if (!entry.isDirectory()) {
				int count;
				byte[] data = new byte[CompressedUtils.WRITE_BUFFER_SIZE];

				// temporary name for the file being uncompress
				try (FileOutputStream fos = new FileOutputStream(archiveFileExtractTargetDirectory.getAbsolutePath() + File.separator
						+ ManipString.redoEntryName(entry.getName()) + ".tmp", false))
				{
					try(BufferedOutputStream dest = new BufferedOutputStream(fos, CompressedUtils.WRITE_BUFFER_SIZE))
					{
						try(GZIPOutputStream zdest = new GZIPOutputStream(dest, CompressedUtils.WRITE_BUFFER_SIZE))
						{
							while ((count = tarIn.read(data, 0, CompressedUtils.READ_BUFFER_SIZE)) != -1) {
								zdest.write(data, 0, count);
							}								
						}
					}
				}

				// rename the file when over makes it thread safe and available for other
				// threads waiting
				try {
					FileUtilsArc.renameTo(
							new File(archiveFileExtractTargetDirectory.getAbsolutePath() + File.separator + ManipString.redoEntryName(entry.getName())
									+ ".tmp"),
							new File(
									archiveFileExtractTargetDirectory.getAbsolutePath() + File.separator + ManipString.redoEntryName(entry.getName()))
					//
					);
				} catch (ArcException e) {
					throw new IOException(e);
				}
			}
		}
	}
	
}
