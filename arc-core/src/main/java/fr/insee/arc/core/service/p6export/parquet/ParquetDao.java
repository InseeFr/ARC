package fr.insee.arc.core.service.p6export.parquet;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.dao.DuckdbDao;
import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;

public class ParquetDao {

	private static final Logger LOGGER = LogManager.getLogger(ParquetDao.class);
	
	// parquet file format as "file.parquet"
	private static final String PARQUET_FILE_EXTENSION = ".parquet";

	private ParquetEncryptionKey encryptionKey;
	
	private boolean exportIfEmpty;
	
	protected DuckdbDao duckdbDao;
	
	public ParquetDao() {
		super();
		this.duckdbDao = new DuckdbDao();
	}


	/**
	 * Export to parquet. Empty tables won't be exported.
	 * @param tables
	 * @param outputDirectory
	 * @param encryptionKey
	 * @throws ArcException
	 */
	public void exportToParquet(List<TableToRetrieve> tables, String outputDirectory,
			ParquetEncryptionKey encryptionKey) throws ArcException {
		exportToParquet(tables, outputDirectory, encryptionKey, false);
	}
	
	
	/**
	 * Export to parquet
	 * @param tables
	 * @param outputDirectory
	 * @param encryptionKey
	 * @param exportOnlyIfNotEmpty
	 * @throws ArcException
	 */
	public void exportToParquet(List<TableToRetrieve> tables, String outputDirectory,
			ParquetEncryptionKey encryptionKey, boolean exportIfEmpty) throws ArcException {

		this.encryptionKey=encryptionKey;
		this.exportIfEmpty=exportIfEmpty;
		
		ThrowingConsumer<Connection> exportToParquetOperation =  connection -> 
		{
			addParquetEncryptionKeyInDuckDb(connection);

			// create output directory
			FileUtilsArc.createDirIfNotexist(outputDirectory);
			
			// export tables one by one to parquet
			for (TableToRetrieve table : tables) {
				// export table to parquet
				LoggerHelper.custom(LOGGER, "Parquet export start : " + table.getTableName());
				exportTableToParquet(connection, table, outputDirectory);
				LoggerHelper.custom(LOGGER, "Parquet export end");
			}
		};
		
		duckdbDao.executeOnDuckdb(exportToParquetOperation);
		
	}

	private void addParquetEncryptionKeyInDuckDb(Connection connection) throws ArcException {

		if (encryptionKey == null) {
			return;
		}
		
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append("PRAGMA add_parquet_key(" + query.quoteText(encryptionKey.getType().getAlias()) + ","
					+ query.quoteText(encryptionKey.getValue()) + ");");

		duckdbDao.executeQuery(connection, query);		

	}


	/**
	 * 
	 * @param connection
	 * @param table
	 * @param outputDirectory
	 * @throws SQLException
	 */
	private void exportTableToParquet(Connection connection, TableToRetrieve table, String outputDirectory)
			throws ArcException {

		String outputFileName = exportTablePath(table, outputDirectory);
				
		exportCoordinatorTableToParquet(connection, table, outputFileName);

		exportExecutorTableToParquet(connection, table, outputFileName);

	}

	/**
	 * export table to parquet if table is located on executor nods
	 * @param connection
	 * @param table
	 * @param outputFileName
	 * @throws SQLException
	 */
	private void exportExecutorTableToParquet(Connection connection, TableToRetrieve table, String outputFileName) throws ArcException {
		
		if (!table.getNod().equals(ArcDatabase.EXECUTOR)) {
			return;
		}
		
		GenericPreparedStatementBuilder query = duckdbDao.selectTableFromAllExecutorNods(table.getTableName());

		if (checkExportCondition(connection, query)) {
			executeCopy(connection, query, outputFileName);
		}
		
	}

	/**
	 * export table to parquet if table is located on executor nods
	 * @param connection
	 * @param table
	 * @param outputFileName
	 * @throws SQLException
	 */
	private void exportCoordinatorTableToParquet(Connection connection, TableToRetrieve table, String outputFileName)
			throws ArcException {

		if (!table.getNod().equals(ArcDatabase.COORDINATOR)) {
			return;
		}

		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append("SELECT * FROM " + duckdbDao.attachedTableName(ArcDatabase.COORDINATOR.getIndex(), table.getTableName()));
		
		if (checkExportCondition(connection, query)) {
			executeCopy(connection, query, outputFileName);
		}
	}

	/**
	 * check if export must be executed
	 * @param connection
	 * @param query
	 * @return
	 * @throws SQLException
	 * @throws ArcException 
	 */
	private boolean checkExportCondition(Connection connection, GenericPreparedStatementBuilder query) throws ArcException
	{
		return this.exportIfEmpty || checkNotEmpty(connection, query);
	}
	
	
	/**
	 * check if the table selected by the query is not empty
	 * @param connection
	 * @param selectQuery
	 * @return true if the table contains at least 1 line, false if not
	 * @throws SQLException
	 */
	protected boolean checkNotEmpty(Connection connection, GenericPreparedStatementBuilder selectQuery)
			throws ArcException {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append("SELECT count(*) FROM (").append(selectQuery).append(" LIMIT 1) a;\n");
		
		int countLine;
		try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
			stmt.execute();
			
			try (ResultSet rs = stmt.getResultSet())
			{
				rs.next();
				countLine = rs.getInt(1);
			}
		}
		catch (SQLException e)
		{
			throw new ArcException(ArcExceptionMessage.SQL_EXECUTE_FAILED, e.getMessage());
		}
		
		return (countLine == 1);
	}

	/**
	 * execute COPY command on duckdb driver
	 * @param connection
	 * @param selectQuery
	 * @param output
	 * @throws SQLException
	 */
	private void executeCopy(Connection connection, GenericPreparedStatementBuilder selectQuery, String output)
			throws ArcException {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append("COPY (").append(selectQuery).append(") TO " + query.quoteText(output));
		query.append(" (FORMAT PARQUET ");
		
		if (encryptionKey != null) {
			query.append(", ENCRYPTION_CONFIG {footer_key: "+query.quoteText(encryptionKey.getType().getAlias())+"}");
		}
		
		query.append(");");
		duckdbDao.executeQuery(connection, query);
	}

	
	/**
	 * return the generated file path used when exporting a table
	 * @param table
	 * @param outputDirectory
	 * @return
	 */
	public static String exportTablePath(TableToRetrieve table, String outputDirectory)
	{
		return exportTablePath(table.getTableName(), outputDirectory);
	}
	
	public static String exportTablePath(String table, String outputDirectory)
	{
		return outputDirectory + File.separator + FormatSQL.extractTableNameToken(table)
		+ PARQUET_FILE_EXTENSION;
	}

}
