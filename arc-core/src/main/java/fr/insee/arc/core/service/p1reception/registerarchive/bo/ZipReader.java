package fr.insee.arc.core.service.p1reception.registerarchive.bo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import fr.insee.arc.utils.files.CompressedUtils;

public class ZipReader implements IArchiveStream {

	public ZipReader() {
		super();
	}

	private ZipArchiveInputStream zipInputStream;

	@Override
	public void startInputStream(File f) throws IOException {
		zipInputStream = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(f), CompressedUtils.READ_BUFFER_SIZE));
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
			zipInputStream.close();
		} catch (IOException e) {
			
		}
	}

}
