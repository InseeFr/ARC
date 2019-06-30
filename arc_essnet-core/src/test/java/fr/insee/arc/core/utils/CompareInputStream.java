package fr.insee.arc.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author Randall Hauch
 * 
 * http://www.java2s.com/Tutorial/Java/0180__File/ComparetwoInputStream.htm
 * 
 */
public class CompareInputStream {

    /**
     * Compare two input stream
     * 
     * @param input1 the first stream
     * @param input2 the second stream
     * @return true if the streams contain the same content, or false otherwise
     * @throws IOException
     * @throws IllegalArgumentException if the stream is null
     */
    public static boolean isSame(InputStream input1, InputStream input2) throws IOException {
	boolean error = false;
	try {
	    byte[] buffer1 = new byte[1024];
	    byte[] buffer2 = new byte[1024];
	    try {
		int numRead1 = 0;
		int numRead2 = 0;
		while (true) {
		    numRead1 = input1.read(buffer1);
		    numRead2 = input2.read(buffer2);
		    if (numRead1 > -1) {
			if (numRead2 != numRead1)
			    return false;
			// Otherwise same number of bytes read
			if (!Arrays.equals(buffer1, buffer2))
			    return false;
			// Otherwise same bytes read, so continue ...
		    } else {
			// Nothing more in stream 1 ...
			return numRead2 < 0;
		    }
		}
	    } finally {
		input1.close();
	    }
	} catch (IOException e) {
	    error = true; // this error should be thrown, even if there is an error closing stream 2
	    throw e;
	} catch (RuntimeException e) {
	    error = true; // this error should be thrown, even if there is an error closing stream 2
	    throw e;
	} finally {
	    try {
		input2.close();
	    } catch (IOException e) {
		if (!error)
		    throw e;
	    }
	}
    }

}
