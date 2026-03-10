package fr.insee.arc.utils.files;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.utils.LoggerHelper;

public class CompressedUtils {

	private static final Logger LOGGER = LogManager.getLogger(CompressedUtils.class);
	
	public static final int READ_BUFFER_SIZE = 81920;
	public static final int WRITE_BUFFER_SIZE = 81920;
	
	private CompressedUtils() {
		throw new IllegalStateException("Utility class");
	}
	
	
	/**
	 *
	 * @param fileIn
	 * @param fileOut
	 * @param entryName
	 * @throws IOException
	 */
	public static void generateTarGzFromFile(File fileIn, File fileOut, String entryName) throws ArcException {

		try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(fileIn), READ_BUFFER_SIZE);) {
			try (TarArchiveOutputStream taos = new TarArchiveOutputStream(
					new GZIPOutputStream(new FileOutputStream(fileOut)));) {
				TarArchiveEntry entry = new TarArchiveEntry(entryName);
				entry.setSize(fileIn.length());
				taos.putArchiveEntry(entry);
				copyFromInputstreamToOutputStream(fis, taos);
				taos.closeArchiveEntry();
			}
		} catch (IOException e) {
			throw new ArcException(e, ArcExceptionMessage.TGZ_CONVERSION_FAILED, fileIn);
		}
	}

	/**
	 * Verifie que l'archive zip existe, lit les fichiers de la listIdSource et les
	 * copie dans un TarArchiveOutputStream
	 *
	 * @param receptionDirectoryRoot
	 * @param phase
	 * @param etat
	 * @param currentContainer
	 * @param listIdSourceContainer
	 */
	public static void generateEntryFromFile(String receptionDirectoryRoot, String idSource,
			TarArchiveOutputStream taos) {
		File fileIn = Paths.get(receptionDirectoryRoot, idSource).toFile();
		if (fileIn.exists()) {
			try {
				TarArchiveEntry entry = new TarArchiveEntry(fileIn.getName());
				entry.setSize(fileIn.length());
				taos.putArchiveEntry(entry);
				// Ecriture dans le fichier
				copyFromInputstreamToOutputStream(
						new BufferedInputStream(new FileInputStream(fileIn), READ_BUFFER_SIZE), taos);
				taos.closeArchiveEntry();
			} catch (IOException ex) {
				LoggerHelper.errorGenTextAsComment(UtilitaireDao.class, "generateEntryFromFile()", LOGGER, ex);
			}
		}
	}

	/**
	 * Verifie que l'archive zip existe, lit les fichiers de la listIdSource et les
	 * copie dans un TarArchiveOutputStream
	 *
	 * @param receptionDirectoryRoot
	 * @param phase
	 * @param etat
	 * @param currentContainer
	 * @param listIdSourceContainer
	 */
	public static void generateEntryFromZip(String receptionDirectoryRoot, String currentContainer,
			List<String> listIdSourceContainer, TarArchiveOutputStream taos) {
		File fileIn = Paths.get(receptionDirectoryRoot, currentContainer).toFile();
		if (fileIn.exists()) {
			try {
				try (ZipInputStream tarInput = new ZipInputStream(
						new BufferedInputStream(new FileInputStream(fileIn), READ_BUFFER_SIZE));) {
					ZipEntry currentEntry = tarInput.getNextEntry();
					// si le fichier est trouvé, on ajoute
					while (currentEntry != null) {
						if (listIdSourceContainer.contains(currentEntry.getName())) {
							TarArchiveEntry entry = new TarArchiveEntry(currentEntry.getName());
							entry.setSize(currentEntry.getSize());
							taos.putArchiveEntry(entry);
							for (int c = tarInput.read(); c != -1; c = tarInput.read()) {
								taos.write(c);
							}
							taos.closeArchiveEntry();
						}
						currentEntry = tarInput.getNextEntry();
					}
				}
			} catch (IOException ex) {
				LoggerHelper.errorGenTextAsComment(UtilitaireDao.class, "generateEntryFromZip()", LOGGER, ex);
			}
		}
	}

	/**
	 * Verifie que l'archive .tar.gz existe, lit les fichiers de la listIdSource et
	 * les copie dans un TarArchiveOutputStream
	 *
	 * @param receptionDirectoryRoot
	 * @param entryPrefix
	 * @param currentContainer
	 * @param listIdSourceContainer
	 * @param taos
	 */
	public static void generateEntryFromTarGz(String receptionDirectoryRoot, String currentContainer,
			List<String> listIdSourceContainer, TarArchiveOutputStream taos) {
		File fileIn = new File(receptionDirectoryRoot + File.separator + currentContainer);
		LoggerHelper.traceAsComment(LOGGER, "#generateEntryFromTarGz()", receptionDirectoryRoot, "/", currentContainer);

		if (fileIn.exists()) {
			// on crée le stream pour lire à l'interieur de
			// l'archive
			try {
				try (TarInputStream tarInput = new TarInputStream(
						new GZIPInputStream(new BufferedInputStream(new FileInputStream(fileIn), READ_BUFFER_SIZE)));) {
					TarEntry currentEntry = tarInput.getNextEntry();
					// si le fichier est trouvé, on ajoute
					while (currentEntry != null) {
						if (listIdSourceContainer.contains(currentEntry.getName())) {
							TarArchiveEntry entry = new TarArchiveEntry(currentEntry.getName());
							entry.setSize(currentEntry.getSize());
							taos.putArchiveEntry(entry);
							tarInput.copyEntryContents(taos);
							taos.closeArchiveEntry();
						}
						currentEntry = tarInput.getNextEntry();
					}
				}
			} catch (IOException ex) {
				LoggerHelper.errorGenTextAsComment(UtilitaireDao.class, "generateEntryFromTarGz()", LOGGER, ex);
			}
		}
	}

	/**
	 * Verifie que l'archive .gz existe, lit les fichiers de la listIdSource et les
	 * copie dans un TarArchiveOutputStream
	 *
	 * @param receptionDirectoryRoot
	 * @param entryPrefix
	 * @param currentContainer
	 * @param listIdSourceContainer
	 * @param taos
	 */
	public static void generateEntryFromGz(String receptionDirectoryRoot, String currentContainer,
			List<String> listIdSourceContainer, TarArchiveOutputStream taos) {
		File fileIn = new File(receptionDirectoryRoot + "/" + currentContainer);
		if (fileIn.exists()) {
			try {
				// on crée le stream pour lire à l'interieur de
				// l'archive
				long size = 0;

				try (GZIPInputStream tarInput = new GZIPInputStream(
						new BufferedInputStream(new FileInputStream(fileIn), READ_BUFFER_SIZE));) {
					// on recupere d'abord la taille du stream; gzip ne permet pas
					// de le faire directement
					for (int c = tarInput.read(); c != -1; c = tarInput.read()) {
						size++;
					}
				}

				TarArchiveEntry entry = new TarArchiveEntry(listIdSourceContainer.get(0));
				entry.setSize(size);
				taos.putArchiveEntry(entry);
				try (GZIPInputStream tarInput = new GZIPInputStream(
						new BufferedInputStream(new FileInputStream(fileIn), READ_BUFFER_SIZE));) {
					for (int c = tarInput.read(); c != -1; c = tarInput.read()) {
						taos.write(c);
					}
					taos.closeArchiveEntry();
				}
			} catch (IOException ex) {
				LoggerHelper.errorGenTextAsComment(UtilitaireDao.class, "generateEntryFromGz()", LOGGER, ex);
			}
		}
	}


	/**
	 *
	 * copy input to output stream - available in several StreamUtils or Streams
	 * classes
	 *
	 * @param input
	 * @param output
	 * @throws IOException
	 */
	public static void copyFromInputstreamToOutputStream(InputStream input, OutputStream output) throws IOException {
		try {
			IOUtils.copy(input, output);
		} finally {
			try {
				input.close();
			} catch (IOException ioe) {
				LoggerHelper.errorAsComment(LOGGER, ioe, "Lors de la clôture de InputStream");
			}
		}
	}
	
	

	public static boolean isNotArchive(String fname) {
		return !fname.endsWith(CompressionExtension.TAR_GZ.getFileExtension()) && !fname.endsWith(CompressionExtension.TGZ.getFileExtension()) && !fname.endsWith(CompressionExtension.ZIP.getFileExtension())
				&& !fname.endsWith(CompressionExtension.GZ.getFileExtension());
	}
	
}
