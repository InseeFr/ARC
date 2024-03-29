package fr.insee.arc.core.service.global.dao;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

public class HashFileNameConversion {

	private HashFileNameConversion() {
		throw new IllegalStateException("Utility class");
	}

	public static final String CHILD_TABLE_TOKEN = "child";
	
	private static final String HASH_ALGORITHM="SHA1";

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
	 * get the hash value of a file using SHA1
	 * 
	 * @param idSource
	 * @return
	 * @throws ArcException
	 */
	public static String hashOfIdSource(String idSource) throws ArcException {
		return hashOfIdSource(idSource,HASH_ALGORITHM);
	}
	
	/**
	 * get the hash value of a file
	 * 
	 * @param idSource
	 * @return
	 * @throws ArcException
	 */
	public static String hashOfIdSource(String idSource, String hashAlgorithm) throws ArcException {
		String hashText = "";
		MessageDigest m;
		try {
			m = MessageDigest.getInstance(hashAlgorithm);
			m.update(idSource.getBytes(), 0, idSource.length());
			hashText = String.format("%1$032x", new BigInteger(1, m.digest()));
		} catch (NoSuchAlgorithmException exception) {
			throw new ArcException(exception, ArcExceptionMessage.HASH_FAILED, idSource);
		}
		return hashText;
	}

}
