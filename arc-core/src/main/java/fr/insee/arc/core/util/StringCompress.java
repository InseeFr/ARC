package fr.insee.arc.core.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StringCompress {
	public static String compress(String str) throws IOException{
	    try(
	    	ByteArrayOutputStream out = new ByteArrayOutputStream();
	    	GZIPOutputStream gzip = new GZIPOutputStream(out);
	    	)
	    {
	    	gzip.write(str.getBytes());
	    	return new BigInteger(out.toByteArray()).toString();
	    }
	 }

	public static String uncompress(String zz) throws IOException {
		BigInteger z=new BigInteger(zz);
		
		try(ByteArrayInputStream bis = new ByteArrayInputStream(z.toByteArray());
		GZIPInputStream gis = new GZIPInputStream(bis);
		BufferedReader br = new BufferedReader(new InputStreamReader(gis));
			)
		{
			StringBuilder sb = new StringBuilder();
			String line;
			while((line = br.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		}
	}
	
	
}
