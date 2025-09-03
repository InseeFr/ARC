package fr.insee.arc.utils.dao;

import java.sql.Connection;

import fr.insee.arc.utils.dataobjects.ColumnAttributes;
import fr.insee.arc.utils.dataobjects.PgViewEnum;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.ressourceUtils.ConnectionAttribute;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;

public class CopyObjectsToDatabase {

	private static final int CHUNK_SIZE = 10000;

	private CopyObjectsToDatabase() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * execute copy by chunk. It is mandatory for large GenericBean objects
	 * 
	 * @param connection
	 * @param targetTableName
	 * @param genericBeanContainingData
	 * @throws ArcException
	 */
	public static void execCopyFromGenericBean(Connection connection, String targetTableName,
			GenericBean genericBeanContainingData) throws ArcException {
		execCopyFromGenericBean(connection, targetTableName, genericBeanContainingData, CHUNK_SIZE, true);
	}

	public static void execCopyFromGenericBeanWithoutDroppingTargetTable(Connection targetConnection,
			String targetTableName, GenericBean genericBeanContainingData) throws ArcException {
		execCopyFromGenericBean(targetConnection, targetTableName, genericBeanContainingData, CHUNK_SIZE, false);
	}

	/**
	 * Drop and replace the target table Copy the data of the input table located on
	 * the input connexion to the target table located on the target Connection
	 * 
	 * @param inputConnection
	 * @param targetConnection
	 * @param inputTable
	 * @param targetTable
	 * @throws ArcException
	 */
	public static void execCopyFromTable(Connection inputConnection, Connection targetConnection, String inputTable,
			String targetTable) throws ArcException {

		createExtensionDblink(targetConnection);

		try {
			execCopyFromTableCore(inputConnection, targetConnection, inputTable, targetTable, true);
		} finally {
			dropExtensionDblink(targetConnection);
		}

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
	private static void execCopyFromGenericBean(Connection targetConnection, String targetTableName,
			GenericBean genericBeanContainingData, int chunkSize, boolean replaceTargetTable) throws ArcException {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();

		query.append(query.createWithGenericBean(targetTableName, genericBeanContainingData, replaceTargetTable));

		int cursor = 0;
		boolean stillToDo = true;

		do {
			int startChunk = cursor;
			int endChunk = cursor + chunkSize;
			cursor = endChunk;
			stillToDo = (cursor < genericBeanContainingData.getContent().size());

			query.insertWithGenericBeanByChunk(targetTableName, genericBeanContainingData, startChunk, endChunk);

			// analyze on the table at the end
			if (!stillToDo) {
				query.append(SQL.COMMIT).append(SQL.END_QUERY);
				query.append(FormatSQL.analyzeSecured(targetTableName));
			}

			UtilitaireDao.get(0).executeRequest(targetConnection, query);
			query = new GenericPreparedStatementBuilder();

		} while (stillToDo);

	}

	/**
	 * execute the copy from a table located on a given input connexion to a target
	 * table located on a target Connection
	 * 
	 * @param inputConnexion
	 * @param targetConnection
	 * @param inputTable
	 * @param targetTable
	 * @param create
	 * @throws ArcException
	 */
	private static void execCopyFromTableCore(Connection inputConnection, Connection targetConnection,
			String inputTable, String targetTable, boolean replaceTargetTable) throws ArcException {

		// normalize table name
		connectDblink(inputConnection, targetConnection);

		ColumnAttributes inputTableColumnAttributes = retrieveColumnAttributesOfDistantInputTable(inputConnection,
				targetConnection, inputTable);

		createOutputTableIfRequired(targetConnection, targetTable, inputTableColumnAttributes, replaceTargetTable);

		insertDataInputTableOutputTable(inputConnection, targetConnection, targetTable, inputTable,
				inputTableColumnAttributes);

		disconnectDblink(inputConnection, targetConnection);

	}

	/**
	 * Create the dblink extension. It will be dropped at the end for security
	 * concern
	 * 
	 * @param targetConnection
	 * @throws ArcException
	 */
	private static void createExtensionDblink(Connection targetConnection) {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.build(SQL.CREATE, SQL.EXTENSION, SQL.IF_NOT_EXISTS, SQL.DBLINK, SQL.WITH, SQL.SCHEMA, SQL.PUBLIC);
		try {
			UtilitaireDao.get(0).executeRequest(targetConnection, query);
		} catch (ArcException e) {
			// silent fail : since postgres 13 only trusted extension can be created by non superuser
			// so this query might fail
		}
	}

	/**
	 * Create a dblink connection from the target connection to the input connection
	 * The target connection where the target table is created read data from the
	 * input connection through the dblink
	 * 
	 * @param inputConnexion
	 * @param targetConnection
	 * @throws ArcException
	 */
	private static void connectDblink(Connection inputConnection, Connection targetConnection) throws ArcException {
		// get connection information to build the connection chain
		PropertiesHandler p = PropertiesHandler.getInstance();
		ConnectionAttribute connexionInAttributes = p.retrieveConnectionAttribute(inputConnection);

		// connect to connexionIn from connexionOut
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append("SELECT dblink_connect(").appendText(inputConnection.toString()).append(",")
				.appendText(connexionInAttributes.getConnectionChainInLibpqFormat()).append(")");
		UtilitaireDao.get(0).executeRequest(targetConnection, query);
	}

	/**
	 * Use the database link to retrieve the columns definition of the input table
	 * 
	 * @param targetConnection
	 * @param inputTable
	 * @return
	 * @throws ArcException
	 */
	private static ColumnAttributes retrieveColumnAttributesOfDistantInputTable(Connection inputConnection,
			Connection targetConnection, String inputTable) throws ArcException {

		// retrieve the meta data of table to copy
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append("SELECT cols, cols_with_type FROM dblink(").appendText(inputConnection.toString()).append(",")
				.appendText(FormatSQL.getTableMetadata(inputTable).getQueryWithParameters()).append(") as metadata (cols text, cols_with_type text)");

		GenericBean gb = new GenericBean(UtilitaireDao.get(0).executeRequest(targetConnection, query));
		return new ColumnAttributes(gb.getColumnValues("cols").get(0), gb.getColumnValues("cols_with_type").get(0));

	}

	/**
	 * Drop the output table container if required Create the output table container
	 * if not exists
	 * 
	 * @param targetConnection
	 * @param targetTable
	 * @param inputTableColumnAttributes
	 * @param replaceTargetTable
	 * @throws ArcException
	 */
	private static void createOutputTableIfRequired(Connection targetConnection, String targetTable,
			ColumnAttributes inputTableColumnAttributes, boolean replaceTargetTable) throws ArcException {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();

		// drop target table if it must be replaced
		if (replaceTargetTable) {
			query.append(SQL.DROP).append(SQL.TABLE).append(SQL.IF_EXISTS).append(targetTable).append(SQL.END_QUERY);
		}

		query.append(SQL.CREATE);
		if (FormatSQL.isTemporary(targetTable)) {
			query.append(SQL.TEMPORARY);
		}
		query.build(SQL.TABLE, SQL.IF_NOT_EXISTS);
		query.append(targetTable).append(" (").append(inputTableColumnAttributes.getColsWithType()).append(" )")
				.append(FormatSQL.WITH_NO_VACUUM);
		UtilitaireDao.get(0).executeRequest(targetConnection, query);

	}

	/**
	 * read the data from input table through dblink and insert data into the output
	 * table container
	 * 
	 * @param targetConnection
	 * @param targetTable
	 * @param inputTable
	 * @param inputTableColumnAttributes
	 * @throws ArcException
	 */
	private static void insertDataInputTableOutputTable(Connection inputConnection, Connection targetConnection,
			String targetTable, String inputTable, ColumnAttributes inputTableColumnAttributes) throws ArcException {

		// query data and insert into container
		String queryData = String.format("SELECT %s FROM %s", inputTableColumnAttributes.getCols(), inputTable);

		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.build(SQL.INSERT_INTO, targetTable, "(", inputTableColumnAttributes.getCols(), ")");
		query.build(SQL.SELECT, inputTableColumnAttributes.getCols(), SQL.FROM);
		query.build(SQL.DBLINK, "(", query.quoteText(inputConnection.toString()), ",", query.quoteText(queryData), ")");
		query.build(SQL.AS, PgViewEnum.ALIAS_A.getTableName(), "(", inputTableColumnAttributes.getColsWithType(), ")");
		UtilitaireDao.get(0).executeRequest(targetConnection, query);
	}

	/**
	 * Disconnect the dblink
	 * 
	 * @param targetConnection
	 * @throws ArcException
	 */
	private static void disconnectDblink(Connection inputConnection, Connection targetConnection) throws ArcException {
		// disconnect connexionIn from connexionOut
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append("SELECT dblink_disconnect(").appendText(inputConnection.toString()).append(")");
		UtilitaireDao.get(0).executeRequest(targetConnection, query);

	}

	/**
	 * Drop the dblink extension
	 * 
	 * @param targetConnection
	 * @throws ArcException
	 */
	private static void dropExtensionDblink(Connection targetConnection) {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.build(SQL.DROP, SQL.EXTENSION, SQL.IF_EXISTS, SQL.DBLINK);
		try {
			UtilitaireDao.get(0).executeRequest(targetConnection, query);
		} catch (ArcException e) {
			// silent fail : since postgres 13 only trusted extension can be created by non superuser
			// so this query might fail
		}
	}

}
