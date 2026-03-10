package fr.insee.arc.core.service.p1reception.registerarchive.bo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.CompressedUtils;

public class GzReader implements IArchiveStream {

	public GzReader() {
		super();
	}

	private FileInputStream fileInputStream;
	private BufferedInputStream bufferedInputStream;
	private GZIPInputStream inputStream;

	boolean readTest = false;
	private String fileName;

	@Override
	public void startInputStream(File f) throws IOException {
		this.fileInputStream = new FileInputStream(f);
		this.bufferedInputStream = new BufferedInputStream(fileInputStream, CompressedUtils.READ_BUFFER_SIZE);
		this.inputStream = new GZIPInputStream(bufferedInputStream);
		fileName = f.getName();
	}

	@Override
	public Entry getEntry() throws IOException {

		if (!readTest) {
			this.inputStream.read();
			readTest = true;
		} else {
			return null;
		}

		return new Entry(false, fileName);
	}

	@Override
	public void close() {
		try {
			if (inputStream != null) {
				inputStream.close();
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
