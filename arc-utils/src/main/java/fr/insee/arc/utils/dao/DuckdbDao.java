package fr.insee.arc.utils.dao;

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
import java.util.Properties;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.database.Delimiters;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.CompressedUtils;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.parquet.ParquetExtension;
import fr.insee.arc.utils.ressourceUtils.ConnectionAttribute;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class DuckdbDao {

	
	
	public void executeOnDuckdb(ThrowingConsumer<Connection> operationOnDuckdb) throws ArcException
	{
		loadDuckdb();
		
		Properties connectionProperties = new Properties();
		connectionProperties.setProperty("preserve_insertion_order", "false");
		
		try (Connection connection = DriverManager.getConnection("jdbc:duckdb:",connectionProperties)) {

			// unzip extensions
			unzipExtensions(connection);

			// attach postgres database
			attachPostgresDatabasesToDuckdb(connection);
			
			operationOnDuckdb.accept(connection);
			
		}
		catch (SQLException e)
		{
			throw new ArcException(ArcExceptionMessage.DATABASE_CONNECTION_FAILED, e.getMessage());
		}
	}
	
	
		/**
		 * Attach the postgres databases (coordinator and executors) to duckdb engine
		 * @param connection
		 * @param encryptionKey
		 * @throws SQLException
		 * @throws IOException
		 */
		private void attachPostgresDatabasesToDuckdb(Connection connection)
				throws ArcException {

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
				String connexionChain = c.getConnectionChainInLibpqFormat();

				query.append("ATTACH " + query.quoteText(connexionChain) + " AS " + attachmentName(connectionIndex)
						+ " (TYPE postgres);\n");
			}
			executeQuery(connection, query);		
		}
	

	public void executeQuery(Connection connection, GenericPreparedStatementBuilder query) throws ArcException {
		try (PreparedStatement stmt = connection.prepareStatement(query.getQueryWithParameters())) {
			stmt.execute();
		}
		catch (SQLException e)
		{
			throw new ArcException(ArcExceptionMessage.SQL_EXECUTE_FAILED, e.getMessage());
		}
	}
	
	private static final String ATTACHMENT_NAME_PREFIX = "pg";

	// classpath file containing extension files
	private static final String DUCKDB_EXTENSION_PROVIDED_FILE = "duckdb/extensions.zip";

	// directory where extension will be unzip and used by duckdb
	private static final String DUCKDB_EXTENSION_INSTALLATION_DIRECTORY = Paths
			.get(System.getProperty("java.io.tmpdir"), "duckdb").toString();
	
	

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
	 * unzip the duckdb postgres extension
	 * @param connection 
	 * @throws ArcException 
	 * @throws IOException
	 * @throws SQLException 
	 */
	private void unzipExtensions(Connection connection) throws ArcException {

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
		catch (IOException e)
		{
			throw new ArcException(ArcExceptionMessage.FILE_READ_FAILED, e.getMessage());
		}
	}
	
	
	private String readDuckDbVersion(Connection connection) throws ArcException {
		String version;
		try (PreparedStatement stmt = connection.prepareStatement("SELECT version()")) {
			stmt.execute();
			
			try (ResultSet rs= stmt.getResultSet())
			{
				rs.next();
				version = rs.getString(1);
			}
		}
		catch (SQLException e)
		{
			throw new ArcException(ArcExceptionMessage.SQL_EXECUTE_FAILED, e.getMessage());
		}
		
		return version;
	}
	
	
	/**
	 * return the name of an table attached to a given postgre database duckdb use
	 * database.schema.tablename format
	 * 
	 * @param tablename
	 * @param connectionIndex
	 * @return
	 */
	public String attachedTableName(int connectionIndex, String tablename) {
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
	public String attachmentName(int connectionIndex) {
		return ATTACHMENT_NAME_PREFIX + Delimiters.SQL_TOKEN_DELIMITER + connectionIndex;
	}

	
	/**
	 * Query that select from a table the data located on all executors nod
	 * @param tablename
	 * @return
	 */
	public GenericPreparedStatementBuilder selectTableFromAllExecutorNods(String tablename)
	{
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		boolean first = true;
		for (int connectionIndex = ArcDatabase.EXECUTOR
				.getIndex(); connectionIndex < PropertiesHandler.getInstance().numberOfNods(); connectionIndex++) {
			if (first) {
				first = false;
			} else {
				query.append(SQL.UNION_ALL);
			}
			query.append("SELECT * FROM " + attachedTableName(connectionIndex, tablename));
		}
		return query;
	}

}
