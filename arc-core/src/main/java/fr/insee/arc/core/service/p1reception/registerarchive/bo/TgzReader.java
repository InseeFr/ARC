package fr.insee.arc.core.service.p1reception.registerarchive.bo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import fr.insee.arc.utils.files.CompressedUtils;

public class TgzReader implements IArchiveStream {

	public TgzReader() {
		super();
	}

	private TarInputStream tarInputStream;

	@Override
	public void startInputStream(File f) throws IOException {
		tarInputStream = new TarInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(f), CompressedUtils.READ_BUFFER_SIZE)));
	}

	@Override
	public Entry getEntry() throws IOException {
		TarEntry currentEntry = tarInputStream.getNextEntry();
		
		if (currentEntry == null) {
			return null;
		}

		return new Entry(currentEntry.isDirectory(), currentEntry.getName());
	}

	@Override
	public void close() {
		try {
			tarInputStream.close();
		} catch (IOException e) {
			
		}
	}

}
