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
	 * @param tableName
	 * @param gb
	 * @throws ArcException
	 */
	public static void execCopyFromGenericBean(Connection connection, String tableName, GenericBean gb)
			throws ArcException {
		execCopyFromGenericBean(connection, tableName, gb, CHUNK_SIZE);
	}

	/**
	 * execute copy from GenericBean to database by chunk of size @param chunkSize
	 * 
	 * @param connection
	 * @param tableName
	 * @param gb
	 * @param chunkSize
	 * @throws ArcException
	 */
	private static void execCopyFromGenericBean(Connection connection, String tableName, GenericBean gb, int chunkSize)
			throws ArcException {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();

		query.append(query.createWithGenericBean(tableName, gb));

		int cursor = 0;
		boolean stillToDo = true;
		
		do {
			int startChunk = cursor;
			int endChunk = cursor + chunkSize;
			cursor = endChunk;
			stillToDo=(cursor < gb.getContent().size());
			
			query.insertWithGenericBeanByChunk(tableName, gb, startChunk, endChunk);
			
			// analyze on the table at the end
			if (!stillToDo)
			{
				query.append(SQL.COMMIT).append(SQL.END_QUERY);
				query.append(FormatSQL.analyzeSecured(tableName));
			}
			
			UtilitaireDao.get(0).executeImmediate(connection, query);
			query = new GenericPreparedStatementBuilder();
			
		} while (stillToDo);
		
	}

}