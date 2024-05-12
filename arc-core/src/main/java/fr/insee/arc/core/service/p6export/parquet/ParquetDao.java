package fr.insee.arc.core.service.p6export.parquet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.database.Delimiters;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.CompressedUtils;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.parquet.ParquetExtension;
import fr.insee.arc.utils.ressourceUtils.ConnectionAttribute;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.utils.FormatSQL;

public class ParquetDao {

	private static final String ATTACHMENT_NAME_PREFIX = "pg";

	// classpath file containing extension files
	private static final String DUCKDB_EXTENSION_PROVIDED_FILE = "duckdb/extensions.zip";

	// directory where extension will be unzip and used by duckdb
	private static final String DUCKDB_EXTENSION_INSTALLATION_DIRECTORY = Paths
			.get(System.getProperty("java.io.tmpdir"), "duckdb").toString();

	// parquet file format as "file.parquet"
	private static final String PARQUET_FILE_EXTENSION = ".parquet";

	ParquetEncryptionKey encryptionKey;
	
	
	/**
	 * Export to parquet
	 * 
	 * @param tables
	 * @param outputDirectory
	 * @param encryptionKey
	 * @throws ArcException
	 */
	public void exportToParquet(List<TableToRetrieve> tables, String outputDirectory,
			ParquetEncryptionKey encryptionKey) throws ArcException {

		this.encryptionKey=encryptionKey;
		
		// load duckdb extension
		loadDuckdb();

		try (Connection connection = DriverManager.getConnection("jdbc:duckdb:")) {

			// unzip extensions
			unzipExtensions(connection);

			// attach postgres database
			attachPostgresDatabasesToDuckdb(connection);

			// create output directory
			FileUtilsArc.createDirIfNotexist(outputDirectory);
			
			// export tables one by one to parquet
			for (TableToRetrieve table : tables) {
				// export table to parquet
				exportTableToParquet(connection, table, outputDirectory);
			}

		} catch (SQLException | IOException e) {
			throw new ArcException(ArcExceptionMessage.SQL_EXECUTE_FAILED, e.getMessage());
		}

	}

	/**
	 * 
	 * @param connection
	 * @param table
	 * @param outputDirectory
	 * @throws SQLException
	 */
	private void exportTableToParquet(Connection connection, TableToRetrieve table, String outputDirectory)
			throws SQLException {

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
	private void exportExecutorTableToParquet(Connection connection, TableToRetrieve table, String outputFileName) throws SQLException {
		
		if (!table.getNod().equals(ArcDatabase.EXECUTOR)) {
			return;
		}
		
		PropertiesHandler properties = PropertiesHandler.getInstance();
		
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		boolean first = true;
		for (int connectionIndex = ArcDatabase.EXECUTOR
				.getIndex(); connectionIndex < properties.numberOfNods(); connectionIndex++) {
			if (first) {
				first = false;
			} else {
				query.append(SQL.UNION_ALL);
			}
			query.append("SELECT * FROM " + attachedTableName(connectionIndex, table.getTableName()));
		}

		executeCopy(connection, query, outputFileName);
		
	}

	/**
	 * export table to parquet if table is located on executor nods
	 * @param connection
	 * @param table
	 * @param outputFileName
	 * @throws SQLException
	 */
	private void exportCoordinatorTableToParquet(Connection connection, TableToRetrieve table, String outputFileName)
			throws SQLException {

		if (!table.getNod().equals(ArcDatabase.COORDINATOR)) {
			return;
		}

		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append("SELECT * FROM " + attachedTableName(ArcDatabase.COORDINATOR.getIndex(), table.getTableName()));
		executeCopy(connection, query, outputFileName);
	}

	/**
	 * execute COPY command on duckdb driver
	 * @param connection
	 * @param selectQuery
	 * @param output
	 * @throws SQLException
	 */
	private void executeCopy(Connection connection, GenericPreparedStatementBuilder selectQuery, String output)
			throws SQLException {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append("COPY (").append(selectQuery).append(") TO " + query.quoteText(output));
		
		if (encryptionKey != null) {
			query.append("(ENCRYPTION_CONFIG {footer_key: "+query.quoteText(encryptionKey.getType().getAlias())+"})");
		}
		
		query.append(";");
		executeQuery(connection, query);
	}

	
	/**
	 * Attach the postgres databases (coordinator and executors) to duckdb engine
	 * @param connection
	 * @param encryptionKey
	 * @throws SQLException
	 * @throws IOException
	 */
	private void attachPostgresDatabasesToDuckdb(Connection connection)
			throws SQLException {

		PropertiesHandler properties = PropertiesHandler.getInstance();
		
		ConnectionAttribute[] postgresConnections = properties.connectionProperties().toArray(new ConnectionAttribute[0]);
		
		int numberOfPods = postgresConnections.length;

		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append("SET custom_extension_repository = " + query.quoteText(DUCKDB_EXTENSION_INSTALLATION_DIRECTORY)
				+ ";\n");
		query.append("SET extension_directory  = " + query.quoteText(DUCKDB_EXTENSION_INSTALLATION_DIRECTORY) + ";\n");
		query.append("INSTALL postgres;\n");

		
		
		for (int connectionIndex = 0; connectionIndex < numberOfPods; connectionIndex++) {
			ConnectionAttribute c = postgresConnections[connectionIndex];

			String connexionChain = "dbname=" + c.getDatabase() + " user=" + c.getDatabaseUsername() + " port="
					+ c.getPort() + " password=" + c.getDatabasePassword() + " host=" + c.getHost();

			query.append("ATTACH " + query.quoteText(connexionChain) + " AS " + attachmentName(connectionIndex)
					+ " (TYPE postgres, READ_ONLY);\n");

			if (encryptionKey != null) {
				query.append("PRAGMA add_parquet_key(" + query.quoteText(encryptionKey.getType().getAlias()) + ","
						+ query.quoteText(encryptionKey.getValue()) + ");");
			}
		}

		executeQuery(connection, query);

	}

	/**
	 * unzip the duckdb postgres extension
	 * @param connection 
	 * @throws IOException
	 * @throws SQLException 
	 */
	private void unzipExtensions(Connection connection) throws IOException, SQLException {

		// check if temporary folder /temp/duckdb/version/ already exists
		// if yes, do nothing : extensions have already been extracted
		if (Paths.get(DUCKDB_EXTENSION_INSTALLATION_DIRECTORY, readDuckDbVersion(connection)).toFile().exists())
		{
			return;
		}
		
		// unzip the extension file
		try (InputStream is = ParquetExtension.class.getResourceAsStream(DUCKDB_EXTENSION_PROVIDED_FILE)) {
			try (ZipArchiveInputStream zis = new ZipArchiveInputStream(is)) {
				ZipArchiveEntry zae = zis.getNextEntry();
				while (zae != null) {

					// if already uncompressed, try next entry
					if (new File(DUCKDB_EXTENSION_INSTALLATION_DIRECTORY + File.separator + zae).exists()) {
						zae = zis.getNextEntry();
						continue;
					}

					if (zae.isDirectory()) {
						FileUtilsArc
								.createDirIfNotexist(DUCKDB_EXTENSION_INSTALLATION_DIRECTORY + File.separator + zae);
					} else {
						try (FileOutputStream fos = new FileOutputStream(
								DUCKDB_EXTENSION_INSTALLATION_DIRECTORY + File.separator + zae)) {
							byte[] buffer = new byte[CompressedUtils.READ_BUFFER_SIZE];
							int len;
							while ((len = zis.read(buffer)) > 0) {
								fos.write(buffer, 0, len);
							}
						}
					}
					zae = zis.getNextEntry();
				}
			}
		}
	}

	private String readDuckDbVersion(Connection connection) throws SQLException {
		String version;
		try (PreparedStatement stmt = connection.prepareStatement("SELECT version()")) {
			stmt.execute();
			
			try (ResultSet rs= stmt.getResultSet())
			{
				rs.next();
				version = rs.getString(1);
			}
		}
		return version;
	}

	private void executeQuery(Connection connection, GenericPreparedStatementBuilder query) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement(query.getQueryWithParameters())) {
			stmt.execute();
		}
	}

	/**
	 * load duckdb driver
	 * 
	 * @throws ArcException
	 */
	private void loadDuckdb() throws ArcException {
		try {
			Class.forName("org.duckdb.DuckDBDriver");
		} catch (ClassNotFoundException e) {
			throw new ArcException(ArcExceptionMessage.DATABASE_CONNECTION_FAILED);
		}
	}

	
	/**
	 * return the generated file path used when exporting a table
	 * @param table
	 * @param outputDirectory
	 * @return
	 */
	protected String exportTablePath(TableToRetrieve table, String outputDirectory)
	{
		return outputDirectory + File.separator + FormatSQL.extractTableNameToken(table.getTableName())
		+ PARQUET_FILE_EXTENSION;
	}
	
	
	/**
	 * return the name of an table attached to a given postgre database duckdb use
	 * database.schema.tablename format
	 * 
	 * @param tablename
	 * @param connectionIndex
	 * @return
	 */
	protected String attachedTableName(int connectionIndex, String tablename) {
		return attachmentName(connectionIndex) + Delimiters.SQL_SCHEMA_DELIMITER + tablename;
	}

	/**
	 * return a postgres attachment name for a given connection index as a reminder
	 * connection index 0 is coordinator and 1+ are executors connection indexes
	 * format return is pg_connectionIndex
	 * 
	 * @param connectionIndex
	 * @return
	 */
	protected String attachmentName(int connectionIndex) {
		return ATTACHMENT_NAME_PREFIX + Delimiters.SQL_TOKEN_DELIMITER + connectionIndex;
	}

}
