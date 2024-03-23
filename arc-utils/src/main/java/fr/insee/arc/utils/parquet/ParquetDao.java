package fr.insee.arc.utils.parquet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.database.ArcDatabase;
import fr.insee.arc.utils.database.Delimiters;
import fr.insee.arc.utils.database.TableToRetrieve;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.CompressedUtils;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.ressourceUtils.ConnectionAttribute;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.utils.FormatSQL;

public class ParquetDao {

	private static final String ATTACHMENT_NAME_PREFIX = "pg";

	// classpath file containing extension files
	private static final String DUCKDB_EXTENSION_PROVIDED_FILE = "duckdb/extensions.zip";

	// directory where extension will be unzip and used by duckdb
	private static final String DUCKDB_EXTENSION_INSTALLATION_DIRECTORY = Paths.get(System.getProperty("java.io.tmpdir"), "duckdb").toString();
	
	// parquet file format as "file.parquet"
	private static final String PARQUET_FILE_EXTENSION = ".parquet";

	public void exportToParquet(List<TableToRetrieve> tables, String outputDirectory,
			ParquetEncryptionKey encryptionKey) throws ArcException {

		loadDuckdb();

		try (Connection connection = DriverManager.getConnection("jdbc:duckdb:")) {
			
			unzipExtensions();

			attachPostgresDatabasesToDuckdb(connection, encryptionKey);

			// exporter la liste des tables en parquet
			for (TableToRetrieve table : tables) {
				exportTableToParquet(connection, table, outputDirectory);
			}

		} catch (SQLException | IOException e) {
			throw new ArcException(ArcExceptionMessage.PARQUET_EXPORT_FAILED);
		}

	}

	private void exportTableToParquet(Connection connection, TableToRetrieve table, String outputDirectory)
			throws SQLException {

		PropertiesHandler properties = PropertiesHandler.getInstance();
		int numberOfPods = properties.getConnectionProperties().size();

		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();

		String output = outputDirectory + File.separator + FormatSQL.extractTableNameToken(table.getTableName()) + PARQUET_FILE_EXTENSION;

		if (table.getNod().equals(ArcDatabase.COORDINATOR)) {
			query.append(
					"SELECT * FROM " + attachedTableName(ArcDatabase.COORDINATOR.getIndex(), table.getTableName()));
			executeCopy(connection, query, output);
			return;
		}

		if (table.getNod().equals(ArcDatabase.EXECUTOR)) {
			boolean first = true;
			for (int connectionIndex = ArcDatabase.EXECUTOR
					.getIndex(); connectionIndex < numberOfPods; connectionIndex++) {
				if (first) {
					first = false;
				} else {
					query.append(SQL.UNION_ALL);
				}
				query.append("SELECT * FROM " + attachedTableName(connectionIndex, table.getTableName()));
			}

			executeCopy(connection, query, output);
		}

	}

	private void executeCopy(Connection connection, GenericPreparedStatementBuilder selectQuery, String output)
			throws SQLException {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append("COPY (").append(selectQuery).append(") TO " + query.quoteText(output) + "; ");
		executeQuery(connection, query);
	}

	private void attachPostgresDatabasesToDuckdb(Connection connection, ParquetEncryptionKey encryptionKey)
			throws SQLException, IOException {

		PropertiesHandler properties = PropertiesHandler.getInstance();
		int numberOfPods = properties.getConnectionProperties().size();

		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append("SET custom_extension_repository = " + query.quoteText(DUCKDB_EXTENSION_INSTALLATION_DIRECTORY) + ";\n");
		query.append("SET extension_directory  = " + query.quoteText(DUCKDB_EXTENSION_INSTALLATION_DIRECTORY) + ";\n");
		query.append("INSTALL postgres;\n");

		for (int connectionIndex = 0; connectionIndex < numberOfPods; connectionIndex++) {
			ConnectionAttribute c = properties.getConnectionProperties().get(connectionIndex);

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

	private void unzipExtensions() throws IOException {
		try (InputStream is = getClass().getResourceAsStream(DUCKDB_EXTENSION_PROVIDED_FILE)) {
			try (ZipArchiveInputStream zis = new ZipArchiveInputStream(is)) {
				ZipArchiveEntry zae = zis.getNextEntry();
				while (zae != null) {
					
					// if already uncompressed, try next entry
					if (new File(DUCKDB_EXTENSION_INSTALLATION_DIRECTORY + File.separator + zae).exists()) {
						zae = zis.getNextEntry();
						continue;
					}

					if (zae.isDirectory()) {
						FileUtilsArc.createDirIfNotexist(DUCKDB_EXTENSION_INSTALLATION_DIRECTORY + File.separator + zae);
					} else {
						try (FileOutputStream fos = new FileOutputStream(DUCKDB_EXTENSION_INSTALLATION_DIRECTORY + File.separator + zae)) {
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
