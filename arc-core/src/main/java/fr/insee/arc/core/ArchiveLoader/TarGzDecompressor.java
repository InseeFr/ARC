package fr.insee.arc.core.ArchiveLoader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.service.ApiReceptionService;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.utils.ManipString;

/**
 * Can decompress tarGZ archive file
 * 
 * @author S4LWO8
 *
 */
public class TarGzDecompressor implements ArchiveExtractor {
	private static final Logger LOGGER = LogManager.getLogger(TarGzDecompressor.class);

	@Override
	public void extract(File archiveFile) throws IOException {
		StaticLoggerDispatcher.info("decompress()" + archiveFile.getName(), LOGGER);
		File dir = new File(archiveFile + ".dir");

		try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(
				new BufferedInputStream(new FileInputStream(archiveFile), ApiReceptionService.READ_BUFFER_SIZE));
				TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
			TarArchiveEntry entry;

			while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
				/** If the entry is a directory, create the directory. **/

				// directories if not empty are automatically read in tar entries list
				if (!entry.isDirectory()) {
					int count;
					byte data[] = new byte[32738];

					// temporary name for the file being uncompress
					try (FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath() + File.separator
							+ ManipString.redoEntryName(entry.getName()) + ".tmp", false);
							BufferedOutputStream dest = new BufferedOutputStream(fos, 32738);
							GZIPOutputStream zdest = new GZIPOutputStream(dest);) {
						while ((count = tarIn.read(data, 0, 32738)) != -1) {
							zdest.write(data, 0, count);
						}
					}

					// rename the file when over makes it thread safe and available for other
					// threads waiting
					try {
						FileUtilsArc.renameTo(
								new File(dir.getAbsolutePath() + File.separator + ManipString.redoEntryName(entry.getName())
										+ ".tmp"),
								new File(
										dir.getAbsolutePath() + File.separator + ManipString.redoEntryName(entry.getName()))
						//
						);
					} catch (ArcException e) {
						throw new IOException(e);
					}
				}
			}

		}

		StaticLoggerDispatcher.info("Untar completed successfully!", LOGGER);

	}

}
