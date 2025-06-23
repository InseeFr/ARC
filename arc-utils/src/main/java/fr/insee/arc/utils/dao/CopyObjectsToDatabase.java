package fr.insee.arc.utils.dao;

import java.sql.Connection;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;

public class CopyObjectsToDatabase {

	private static final int CHUNK_SIZE = 10000;
	
	
	private CopyObjectsToDatabase() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * execute copy by chunk. It is mandatory for large GenericBean objects
	 * @param connection
	 * @param targetTableName
	 * @param genericBeanContainingData
	 * @throws ArcException
	 */
	public static void execCopyFromGenericBean(Connection connection, String targetTableName, GenericBean genericBeanContainingData)
			throws ArcException {
		execCopyFromGenericBean(connection, targetTableName, genericBeanContainingData, CHUNK_SIZE, true);
	}

	public static void execCopyFromGenericBeanWithoutDroppingTargetTable(Connection targetConnection, String targetTableName, GenericBean genericBeanContainingData)
			throws ArcException {
		execCopyFromGenericBean(targetConnection, targetTableName, genericBeanContainingData, CHUNK_SIZE, false);
	}

	
	/**
	 * execute copy from GenericBean to database by chunk of size @param chunkSize
	 * 
	 * @param targetConnection
	 * @param targetTableName
	 * @param genericBeanContainingData
	 * @param chunkSize
	 * @throws ArcException
	 */
	private static void execCopyFromGenericBean(Connection targetConnection, String targetTableName, GenericBean genericBeanContainingData, int chunkSize, boolean replaceTargetTable)
			throws ArcException {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();

		query.append(query.createWithGenericBean(targetTableName, genericBeanContainingData, replaceTargetTable));

		int cursor = 0;
		boolean stillToDo = true;
		
		do {
			int startChunk = cursor;
			int endChunk = cursor + chunkSize;
			cursor = endChunk;
			stillToDo=(cursor < genericBeanContainingData.getContent().size());
			
			query.insertWithGenericBeanByChunk(targetTableName, genericBeanContainingData, startChunk, endChunk);
			
			// analyze on the table at the end
			if (!stillToDo)
			{
				query.append(SQL.COMMIT).append(SQL.END_QUERY);
				query.append(FormatSQL.analyzeSecured(targetTableName));
			}
			
			UtilitaireDao.get(0).executeRequest(targetConnection, query);
			query = new GenericPreparedStatementBuilder();
			
		} while (stillToDo);
		
	}

}
