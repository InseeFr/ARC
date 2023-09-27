package fr.insee.arc.core.service.p1reception.registerarchive.bo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import fr.insee.arc.utils.files.CompressedUtils;

public class GzReader implements IArchiveStream {

	public GzReader() {
		super();
	}

	private GZIPInputStream inputStream;
	boolean readTest=false;
	
	private String fileName;

	@Override
	public void startInputStream(File f) throws IOException {
		this.inputStream = new GZIPInputStream(new BufferedInputStream(new FileInputStream(f), CompressedUtils.READ_BUFFER_SIZE));
		fileName=f.getName();
	}

	@Override
	public Entry getEntry() throws IOException {
		
		if (!readTest)
		{
			this.inputStream.read();
			readTest=true;
		}
		else
		{
			return null;
		}

		return new Entry(false, fileName);
	}

	@Override
	public void close() {
		try {
			inputStream.close();
		} catch (IOException e) {
			
		}
	}

}
