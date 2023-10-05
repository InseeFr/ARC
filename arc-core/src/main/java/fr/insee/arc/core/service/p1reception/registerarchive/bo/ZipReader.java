package fr.insee.arc.core.service.p1reception.registerarchive.bo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.tools.tar.TarInputStream;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.CompressedUtils;

public class ZipReader implements IArchiveStream {

	public ZipReader() {
		super();
	}

	private FileInputStream fileInputStream;
	private BufferedInputStream bufferedInputStream;
	private ZipArchiveInputStream zipInputStream;

	@Override
	public void startInputStream(File f) throws IOException {
		this.fileInputStream = new FileInputStream(f);
		this.bufferedInputStream = new BufferedInputStream(fileInputStream, CompressedUtils.READ_BUFFER_SIZE);
		this.zipInputStream = new ZipArchiveInputStream(bufferedInputStream);
	}

	@Override
	public Entry getEntry() throws IOException {
		ZipArchiveEntry currentEntry = zipInputStream.getNextZipEntry();

		if (currentEntry == null) {
			return null;
		}

		return new Entry(currentEntry.isDirectory(), currentEntry.getName());
	}
	
	@Override
	public void close() {
		try {
			if (zipInputStream != null) {
				zipInputStream.close();
			}
			if (bufferedInputStream != null) {
				bufferedInputStream.close();
			}
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		} catch (IOException e) {
			new ArcException(e, ArcExceptionMessage.FILE_CLOSE_FAILED).logFullException();
		}
	}

}
