package fr.insee.arc.core.service.utility;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ServiceHashFileName {

	public static final String CHILD_TABLE_TOKEN = "child";

	/**
	 * Generate the filename
	 * 
	 * @param tableName
	 * @param idSource
	 * @return
	 */
	public static String tableOfIdSource(String tableName, String idSource) {
		return tableName + "_" + CHILD_TABLE_TOKEN + "_" + hashOfIdSource(idSource);
	}

	/**
	 * get the hash value of a file
	 * 
	 * @param idSource
	 * @return
	 */
	public static String hashOfIdSource(String idSource) {
		String hashText = "";
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("SHA1");
			m.update(idSource.getBytes(), 0, idSource.length());
			hashText = String.format("%1$032x", new BigInteger(1, m.digest()));
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		return hashText;
	}

}
