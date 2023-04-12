package fr.insee.arc.core.service.utility;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import fr.insee.arc.utils.exception.ArcException;

public class ServiceHashFileName {

	private ServiceHashFileName() {
		throw new IllegalStateException("Utility class");
	}

	public static final String CHILD_TABLE_TOKEN = "child";

	/**
	 * Generate the filename
	 * 
	 * @param tableName
	 * @param idSource
	 * @return
	 * @throws ArcException
	 */
	public static String tableOfIdSource(String tableName, String idSource) throws ArcException {
		return tableName + "_" + CHILD_TABLE_TOKEN + "_" + hashOfIdSource(idSource);
	}

	/**
	 * get the hash value of a file
	 * 
	 * @param idSource
	 * @return
	 * @throws ArcException
	 */
	public static String hashOfIdSource(String idSource) throws ArcException {
		String hashText = "";
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("SHA1");
			m.update(idSource.getBytes(), 0, idSource.length());
			hashText = String.format("%1$032x", new BigInteger(1, m.digest()));
		} catch (NoSuchAlgorithmException e) {
			throw new ArcException("Hashing idsource was not possible because hash algorithm is not implemented");
		}
		return hashText;
	}

}
