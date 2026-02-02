package fr.insee.arc.utils.dao;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import fr.insee.arc.utils.dataobjects.ColumnAttributes;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.CompressedUtils;
import fr.insee.arc.utils.ressourceUtils.ConnectionAttribute;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.textUtils.IConstanteNumerique;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

/**
 *
 * Split this -> ddl, dml, single vs multiple results
 *
 */
public class UtilitaireDao implements IConstanteNumerique, IConstanteCaractere {

	private static final Logger LOGGER = LogManager.getLogger(UtilitaireDao.class);

	/**
	 * execute request returns a table with headers, type and data provide the
	 * indexes of these elements
	 */
	public static final int EXECUTE_REQUEST_HEADERS_START_INDEX = 0;
	public static final int EXECUTE_REQUEST_TYPES_START_INDEX = 1;
	public static final int EXECUTE_REQUEST_DATA_START_INDEX = 2;

	/**
	 * default pool name used in properties
	 */
	private Integer pool;
	public static Map<Integer, UtilitaireDao> map = new ConcurrentHashMap<>();

	PropertiesHandler properties;

	private UtilitaireDao(Integer aPool) {
		this.pool = aPool;
		properties = PropertiesHandler.getInstance();
	}

	public static final UtilitaireDao get(Integer aPool) {
		map.putIfAbsent(aPool, new UtilitaireDao(aPool));
		return map.get(aPool);
	}

	/**
	 * Compute the number of executor nods according to he number of user declared
	 * in connexion Users are split by ||| . See regexp :
	 * {@value #CONNECTION_SEPARATOR}
	 * 
	 * @return
	 */
	public int numberOfNods() {
		return properties.numberOfNods();
	}

	/**
	 * return a valid connection index according to the given connection in
	 * properties
	 * 
	 * @param aPool
	 * @return
	 */
	private int validConnectionIndex(Integer aPool) {

		int numberOfNodsDeclaredInProperties = numberOfNods();

		return (aPool < numberOfNodsDeclaredInProperties) ? aPool : numberOfNodsDeclaredInProperties - 1;
	}

	/**
	 * Retourne une connexion vers la base de données
	 *
	 * @return la connexion
	 * @throws ArcException
	 * @throws ClassNotFoundException
	 */
	public final Connection getDriverConnexion() throws ArcException {
		// invocation du driver
		try {

			int validConnectionIndex = validConnectionIndex(this.pool);

			ConnectionAttribute currentConnectionAttributes = properties.connectionProperties()
					.get(validConnectionIndex);
			String driver = currentConnectionAttributes.getDatabaseDriverClassName();
			String uri = currentConnectionAttributes.getDatabaseUrl();
			String user = currentConnectionAttributes.getDatabaseUsername();
			String password = currentConnectionAttributes.getDatabasePassword();

			Class.forName(driver);
			Connection c = null;

			// renvoie la connexion relative au driver
			try {
				Properties props = new Properties();
				props.setProperty("user", user);
				props.setProperty("password", password);
				props.setProperty("tcpKeepAlive", "true");
				props.setProperty("prepareThreshold", "0");
				props.setProperty("preparedStatementCacheQueries", "0");
				props.setProperty("preparedStatementCacheSizeMiB", "0");

				c = DriverManager.getConnection(uri, props);

			} catch (Exception e) {
				throw new ArcException(e, ArcExceptionMessage.DATABASE_CONNECTION_FAILED);
			}
			return c;

		} catch (ClassNotFoundException e1) {
			throw new ArcException(e1, ArcExceptionMessage.DATABASE_CONNECTION_FAILED);
		}
	}

	/**
	 *
	 * @param connexion
	 * @return une nouvelle connexion non poolée si connexion isnull, ou la
	 *         connexion en entrée
	 * @throws ArcException
	 */
	public final ConnectionWrapper initConnection(Connection connexion) throws ArcException {
		Boolean isNull = (connexion == null);
		return new ConnectionWrapper(isNull, Boolean.TRUE.equals(isNull) ? getDriverConnexion() : connexion);
	}

	/** Returns true if the connection is valid. */
	public boolean isConnectionOk() {
		try {
			executeRequest(null, new GenericPreparedStatementBuilder("select true"));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Vérifier qu'une table existe <br/>
	 *
	 */
	public Boolean isTableExiste(Connection connexion, String table) {
		Boolean b = null;
		try {
			b = hasResults(connexion, FormatSQL.tableExists(table));
		} catch (Exception e) {
			LoggerHelper.errorGenTextAsComment(getClass(), "isTableExiste()", LOGGER, e);
		}
		return b;
	}

	/**
	 * @param connexion  la connexion à la base
	 * @param someTables le nom des tables
	 * @return
	 */
	public void dropTable(Connection connexion, String... someTables) {
		try {
			executeRequest(connexion, FormatSQL.dropTable(someTables));
		} catch (ArcException ex) {
			ex.logFullException();
		}
	}

	/**
	 * @param connexion  la connexion à la base
	 * @param someTables le nom des tables
	 * @return
	 */
	public void dropTable(Connection connexion, List<String> someTables) {
		try {
			executeRequest(connexion, FormatSQL.dropTable(someTables.toArray(new String[0])));
		} catch (ArcException ex) {
			ex.logFullException();
		}
	}

	/**
	 * Exécute une requête qui renvoie exactement UN (unique) résultat de type
	 * {@link String}.<br/>
	 * Si plusieurs enregistrements devaient être récupérés par la requete
	 * {@code requete}, seul le premier est récupéré à la place.
	 *
	 * @param connexion la connexion à la base
	 * @param requete   la requête
	 * @param args      les arguments de la requête (optionnels)
	 * @return
	 * @throws ArcException
	 */
	public String getString(Connection connexion, GenericPreparedStatementBuilder requete) throws ArcException {
		List<List<String>> returned = executeRequest(connexion, requete,
				new ModeRequete[] { ModeRequete.EXTRA_FLOAT_DIGIT });
		return (returned.size() <= EXECUTE_REQUEST_DATA_START_INDEX ? null
				: returned.get(EXECUTE_REQUEST_DATA_START_INDEX).get(0));

	}
	
	/**
	 * Exécute une requête qui renvoie exactement un argument de type
	 * {@link Boolean}.
	 *
	 * @param connexion la connexion à la base
	 * @param sql       la requête
	 * @param args      les arguments de la requête (optionnels)
	 * @return
	 * @throws ArcException 
	 */
	public boolean getBoolean(Connection connexion, GenericPreparedStatementBuilder sql) throws ArcException {
		String result = getString(connexion, sql);
		return result.equals("t");
	}

	/**
	 * Exécute une requête qui renvoie exactement un argument de type
	 * {@link Integer}.
	 *
	 * @param connexion la connexion à la base
	 * @param sql       la requête
	 * @param args      les arguments de la requête (optionnels)
	 * @return
	 */
	public int getInt(Connection connexion, GenericPreparedStatementBuilder sql, ModeRequete... modes) {
		try {
			List<List<String>> returned = executeRequest(connexion, sql, modes);
			return (returned.size() <= EXECUTE_REQUEST_DATA_START_INDEX ? ZERO
					: Integer.parseInt(returned.get(EXECUTE_REQUEST_DATA_START_INDEX).get(0)));
		} catch (Exception ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "getInt()", LOGGER, ex);
		}
		return ZERO;
	}
	
	public int getInt(Connection connexion, String sql, ModeRequete... modes) {
		return getInt(connexion, new GenericPreparedStatementBuilder(sql), modes);
	}
	
	/**
	 * Exécute une requête qui renvoie exactement un argument de type
	 * {@link Integer}.
	 *
	 * @param connexion la connexion à la base
	 * @param sql       la requête
	 * @param args      les arguments de la requête (optionnels)
	 * @return
	 * @throws ArcException 
	 */
	public long getLong(Connection connexion, GenericPreparedStatementBuilder sql, ModeRequete... modes) throws ArcException {
		List<List<String>> returned = executeRequest(connexion, sql, modes);
		return Long.parseLong(returned.get(EXECUTE_REQUEST_DATA_START_INDEX).get(0));
	}
	
	/**
	 * Check if a column exists in a table
	 * 
	 * @param aConnexion
	 * @param aNomTable
	 * @param aNomVariable
	 * @return
	 * @throws ArcException
	 */
	public boolean isColonneExiste(Connection aConnexion, String aNomTable, String aNomVariable) throws ArcException {
		return getColumns(aConnexion, new HashSet<>(), aNomTable).contains(aNomVariable);
	}

	/**
	 * <br/>
	 *
	 *
	 * @param connexion
	 * @param requete
	 * @param modes
	 * @return
	 * @throws ArcException
	 */
	public List<List<String>> executeRequestWithoutMetadata(Connection connexion,
			GenericPreparedStatementBuilder requete, ModeRequete... modes) throws ArcException {
		List<List<String>> returned = executeRequest(connexion, requete, modes);
		returned.remove(0);
		returned.remove(0);
		return returned;
	}

	public void executeImmediate(Connection connexion, GenericPreparedStatementBuilder requete, ModeRequete... modes) throws ArcException {

		if (LOGGER.isEnabled(Level.TRACE)) {
			LoggerHelper.traceAsComment(LOGGER, "START executeImmediate");
			LoggerHelper.trace(LOGGER, "\n" + ModeRequete.configureQuery(requete, modes).getQueryWithParameters());
		}

		ConnectionWrapper connexionWrapper = initConnection(connexion);
		try {
			connexionWrapper.getConnexion().setAutoCommit(true);
			try (Statement st = connexionWrapper.getConnexion().createStatement();) {
				try {
					st.execute(ModeRequete.configureQuery(requete, modes).getQueryWithParameters());
					LoggerHelper.traceAsComment(LOGGER, "END executeImmediate");
				} catch (SQLException e) {
					try {
						st.cancel();
					}
					catch (SQLException cancelException) {
						LoggerHelper.error(LOGGER, requete);
						throw e;
					}
					LoggerHelper.error(LOGGER, requete);
					throw e;
				}
			}
		} catch (SQLException e) {
			throw new ArcException(e, ArcExceptionMessage.SQL_EXECUTE_FAILED, e.getMessage()).logFullException();
		} finally {
			if (connexionWrapper.isLocal()) {
				connexionWrapper.close();
			}
		}
	}

	/**
	 * CHeck if a query will give result or not
	 * 
	 * @param connexion
	 * @param requete
	 * @return
	 * @throws ArcException
	 */
	public Boolean testResultRequest(Connection connexion, GenericPreparedStatementBuilder requete) {
		GenericPreparedStatementBuilder requeteLimit = new GenericPreparedStatementBuilder();
		requeteLimit.append("SELECT * from (").append(requete).append(") dummy LIMIT 1");
		try {
			executeRequest(connexion, requeteLimit);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Exécution de requêtes ramenant des enregistrements en mode PreparedStatement
	 *
	 * <br/>
	 *
	 *
	 * @param connexion
	 *
	 * @param requete
	 *
	 * @return
	 * @throws ConnexionException
	 * @throws ArcException
	 * @throws PoolException
	 *
	 *
	 */
	public List<List<String>> executeRequest(Connection connexion, GenericPreparedStatementBuilder requete,
			ModeRequete[] modes) throws ArcException {
		return executeRequest(connexion, requete, EntityProvider.getArrayOfArrayProvider(), modes, true);

	}

	public List<List<String>> executeRequest(Connection connexion, StringBuilder requete)
			throws ArcException {
		return executeRequest(connexion, new GenericPreparedStatementBuilder(requete));
	}
	
	public List<List<String>> executeRequest(Connection connexion, String requete)
			throws ArcException {
		return executeRequest(connexion, new GenericPreparedStatementBuilder(requete));
	}
	
	public List<List<String>> executeRequest(Connection connexion, GenericPreparedStatementBuilder requete)
			throws ArcException {
		return executeRequest(connexion, requete, EntityProvider.getArrayOfArrayProvider(), new ModeRequete[] {}, true);
	}

	public List<List<String>> executeRequestNoCommit(Connection connexion, GenericPreparedStatementBuilder requete)
			throws ArcException {
		return executeRequest(connexion, requete, EntityProvider.getArrayOfArrayProvider(), new ModeRequete[] {}, false);
	}
	
	
	public void executeRequestCommit(Connection connexion)
			throws ArcException {
		try {
			connexion.commit();
		} catch (SQLException sqlException) {
			try {
				connexion.rollback();
			} catch (SQLException e) {
				LoggerHelper.error(LOGGER, "Rollback failed");
			}
			LoggerHelper.error(LOGGER, "Commit failed");
			throw new ArcException(sqlException, ArcExceptionMessage.SQL_EXECUTE_FAILED, sqlException.getMessage()).logFullException();
		}

	}
	

	public void executeRequestRollback(Connection connexion)
			throws ArcException {
		try {
			connexion.rollback();
		} catch (SQLException sqlException) {
			LoggerHelper.error(LOGGER, "Lors du commit");
			throw new ArcException(sqlException, ArcExceptionMessage.SQL_EXECUTE_FAILED, sqlException.getMessage()).logFullException();
		}

	}
	
	/**
	 * Register in the targetPreparedStatement the bind variable of a request with
	 * correct type
	 * 
	 * @param requete
	 * @param bindVariableIndex
	 * @throws SQLException
	 */
	public static void registerBindVariable(PreparedStatement targetPreparedStatement,
			GenericPreparedStatementBuilder requete, int bindVariableIndex) throws SQLException {
		if (requete.getParameters().get(bindVariableIndex).getType().equals(ParameterType.STRING)) {
			targetPreparedStatement.setString(bindVariableIndex + 1,
					(String) requete.getParameters().get(bindVariableIndex).getValue());		
		} else if (requete.getParameters().get(bindVariableIndex).getType().equals(ParameterType.INT)) {
			targetPreparedStatement.setInt(bindVariableIndex + 1,
					(Integer) requete.getParameters().get(bindVariableIndex).getValue());
		} else if (requete.getParameters().get(bindVariableIndex).getType().equals(ParameterType.BYTES)) {
			targetPreparedStatement.setBytes(bindVariableIndex + 1,
					(byte[]) requete.getParameters().get(bindVariableIndex).getValue());
		}
	}

	/**
	 * Exécution de requêtes ramenant des enregistrements
	 *
	 * <br/>
	 *
	 *
	 * @param connexion
	 *
	 * @param requete
	 *
	 * @return
	 * @throws ConnexionException
	 * @throws ArcException
	 * @throws PoolException
	 *
	 *
	 */
	public <T> T executeRequest(Connection connexion, GenericPreparedStatementBuilder requete,
			EntityProvider<T> entityProvider, ModeRequete[] modes, boolean autocommit) throws ArcException {

		if (LOGGER.isEnabled(Level.TRACE)) {
			LoggerHelper.trace(LOGGER, "/* Start executeRequest */");
			LoggerHelper.trace(LOGGER, "\n" + ModeRequete.configureQuery(requete).getQueryWithParameters());
		}

		try {
			ConnectionWrapper connexionWrapper = initConnection(connexion);
			boolean rollback = false;
			try {
				connexionWrapper.getConnexion().setAutoCommit(false);
				try (PreparedStatement stmt = connexionWrapper.getConnexion()
						.prepareStatement(ModeRequete.configureQuery(requete, modes).getQuery().toString());) {
					for (int i = 0; i < requete.getParameters().size(); i++) {
						registerBindVariable(stmt, requete, i);
					}
					
					// the first result found will be output
					boolean isresult = stmt.execute();
					LoggerHelper.traceAsComment(LOGGER, "End executeRequest");

					if (!isresult) {
						do {
							isresult = stmt.getMoreResults();
							if (isresult) {
								break;
							}
							if (stmt.getUpdateCount() == -1) {
								break;
							}
						} while (true);
					}

					if (isresult) {
						ResultSet res = stmt.getResultSet();
						return entityProvider.apply(res);
					}
					return null;
				}
			} catch (SQLException e) {
				connexionWrapper.getConnexion().rollback();
				rollback = true;
				LoggerHelper.error(LOGGER, ModeRequete.configureQuery(requete).getQueryWithParameters());						
				LoggerHelper.error(LOGGER, "ROLLBACK !!", e);
				throw e;
			} finally {
				if (!rollback && autocommit)
				{
					connexionWrapper.getConnexion().commit();
				}
				connexionWrapper.close();
			}
		} catch (SQLException sqlException) {
			LoggerHelper.error(LOGGER, "Lors de l'exécution de", requete.getQuery());
			throw new ArcException(sqlException, ArcExceptionMessage.SQL_EXECUTE_FAILED, sqlException.getMessage()).logFullException();
		}
	}

	/**
	 * Renvoie true si une liste issue de requete a au moins un enregistrement <br/>
	 *
	 *
	 * @param l
	 * @return
	 */
	public static boolean hasResults(List<List<String>> l) {
		return (l.size() > 2);
	}

	public boolean hasResults(Connection connexion, GenericPreparedStatementBuilder requete) throws ArcException {
		return hasResults(executeRequest(connexion, requete));
	}

	/**
	 * Ecrit le résultat de la requête {@code requete} dans le fichier compressé
	 * {@code out}
	 *
	 * @param connexion
	 * @param requete
	 * @param out
	 * @throws ArcException
	 */
	public void outStreamRequeteSelect(Connection connexion, GenericPreparedStatementBuilder requete, OutputStream out)
			throws ArcException {
		
			writeSelectQueryMetadataToOutputStreamAsCSV(connexion, requete, out);
		
			exporting(connexion, "(" + requete + ")", out, true);

	}

	/**
	 * write query metadata to outputstream in csv format
	 * @param connection
	 * @param requete
	 * @param out
	 * @throws ArcException
	 */
	public void writeSelectQueryMetadataToOutputStreamAsCSV(Connection connexion, GenericPreparedStatementBuilder requete,
			OutputStream out) throws ArcException {

		try (ConnectionWrapper connexionWrapper = initConnection(connexion)) {

			// limit query output as only metadata is required
			GenericPreparedStatementBuilder limit = FormatSQL.limitQuery(requete , 0);
	
			try (PreparedStatement stmt = connexionWrapper.getConnexion()
					.prepareStatement(limit.getQuery().toString())) {
	
				// bind parameters
				for (int i = 0; i < limit.getParameters().size(); i++) {
					registerBindVariable(stmt, limit, i);
				}
	
				StringBuilder str = new StringBuilder();
				String lineSeparator = System.lineSeparator();
	
				// write metadata in output
				try (ResultSet res = stmt.executeQuery()) {
					ResultSetMetaData rsmd = res.getMetaData();
	
					// Noms des colonnes
					for (int i = 1; i <= rsmd.getColumnCount(); i++) {
						str.append(rsmd.getColumnLabel(i));
						if (i < rsmd.getColumnCount()) {
							str.append(";");
						}
					}
					str.append(lineSeparator);
					// Types des colonnes
					for (int i = 1; i <= rsmd.getColumnCount(); i++) {
						str.append(rsmd.getColumnTypeName(i));
						if (i < rsmd.getColumnCount()) {
							str.append(";");
						}
					}
					str.append(lineSeparator);
	
					out.write(str.toString().getBytes());
	
				} catch (SQLException | IOException e) {
					throw new ArcException(ArcExceptionMessage.GUI_EXPORT_TABLE_FAILED);
				}
			} catch (SQLException e1) {
				throw new ArcException(ArcExceptionMessage.GUI_EXPORT_TABLE_FAILED);
			}
			
		}
	}

	/**
	 * Les fichiers à copier sont potentiellement dans des dossiers différents
	 * (****_OK ou *****_KO) <br/>
	 *
	 *
	 * @param connexion
	 * @param requete          , contient la liste des fichiers à copier
	 * @param taos             , stream receptacle des fichiers
	 * @param path             , chemin jusqu'à l'avant dernier dossier
	 * @param listRepertoireIn , noms du dernier dossier qui diffère d'un cas à
	 *                         l'autre
	 */
	public void getFilesDataStreamFromListOfInputDirectories(Connection connexion,
			GenericPreparedStatementBuilder requete, TarArchiveOutputStream taos, String path,
			List<String> listRepertoireIn) {
		LoggerHelper.debugDebutMethodeAsComment(getClass(), "copieFichiers()", LOGGER);
		GenericBean g;
		List<String> listFichier = new ArrayList<>();
		File fileIn = null;
		boolean find;
		String receptionDirectoryRoot = "";
		try {
			g = new GenericBean(this.executeRequest(connexion, requete));
			listFichier = g.mapContent().get("nom_fichier");
			LoggerHelper.traceAsComment(LOGGER, "listeFichier =", listFichier);
			if (listFichier == null) {
				LoggerHelper.traceAsComment(LOGGER, "listeFichier est null, sortie de la méthode");
				return;
			}
			for (int i = 0; i < listFichier.size(); i++) {
				LoggerHelper.traceAsComment(LOGGER, "listFichier.get(", i, ") =", listFichier.get(i));
				// boucle sur l'ensemble des dossiers de recherche
				find = false;
				for (int j = 0; j < listRepertoireIn.size() && !find; j++) {
					receptionDirectoryRoot = Paths.get(path, listRepertoireIn.get(j)).toString();
					fileIn = new File(receptionDirectoryRoot + File.separator + listFichier.get(i));
					if (fileIn.exists()) {// le fichier existe dans le dossier OK
						find = true;
					}
				}
				// Ajout d'un nouveau fichier
				// Ajout de l'entrée ?
				LoggerHelper.traceAsComment(LOGGER, "Copie du fichier", fileIn);
				if (fileIn != null) {
					TarArchiveEntry entry = new TarArchiveEntry(fileIn.getName());
					entry.setSize(fileIn.length());
					taos.putArchiveEntry(entry);
					// Ecriture dans le fichier
					CompressedUtils.copyFromInputstreamToOutputStream(
							new BufferedInputStream(new FileInputStream(fileIn), CompressedUtils.READ_BUFFER_SIZE),
							taos);
					taos.closeArchiveEntry();
				}
			}
		} catch (ArcException | IOException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "copieFichiers()", LOGGER, ex);
		}
		LoggerHelper.debugFinMethodeAsComment(getClass(), "copieFichiers()", LOGGER);
	}

	public List<String> getList(Connection connexion, StringBuilder requete, List<String> returned) {
		return getList(connexion, requete.toString(), returned);
	}

	public List<String> getList(Connection connexion, String requete, List<String> returned) {
		try {
			if (LOGGER.isEnabled(Level.TRACE)) {
				LoggerHelper.trace(LOGGER, requete);
			}
			ConnectionWrapper connexionWrapper = initConnection(connexion);
			try {
				Statement stmt = connexionWrapper.getConnexion().createStatement();
				try {
					ResultSet rs = stmt.executeQuery(requete);
					while (rs.next()) {
						returned.add(rs.getString(FIRST_COLUMN_INDEX));
					}
				} finally {
					stmt.close();
				}
			} finally {
				connexionWrapper.close();
			}
		} catch (Exception ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "getList()", LOGGER, ex);
		}
		return returned;
	}

	/**
	 * Postgres libère mal l'espace sur les tables quand on fait trop d'opération
	 * sur les colonnes. Un vacuum full des tables du méta-modèle permet de résoudre
	 * ce problème.
	 *
	 * @param connexion
	 * @param type
	 */
	public void maintenancePgCatalog(Connection connexion, String type) {
		try {
			LoggerHelper.debugAsComment(LOGGER, "vacuum", type, "sur le catalogue.");

			executeRequest(connexion, FormatSQL.setTimeOutMaintenance());

			GenericBean gb = new GenericBean(executeRequest(connexion, new GenericPreparedStatementBuilder(
					"select relname from pg_stat_all_tables where schemaname='pg_catalog' and n_dead_tup>"+FormatSQL.NUMBER_OF_DEAD_TUPLES_FOR_VACUUM)));
			
			
			GenericPreparedStatementBuilder requete = new GenericPreparedStatementBuilder();
			for (String t : gb.getColumnValues("relname")) {
				requete.append(FormatSQL.vacuumSecured(t, type));
			}
			if (requete.length()>0)
			{
				// vacuumSecured requires executeImmediate autocommit set to true
				executeImmediate(connexion, requete);
			}
		} catch (Exception ex) {
			LoggerHelper.error(LOGGER, ex);
		} finally {
			try {
				executeRequest(connexion, FormatSQL.resetTimeOutMaintenance());
			} catch (Exception e) {
				LoggerHelper.error(LOGGER, e);
			}
		}
	}

	/**
	 * Renvoie la liste des colonnes d'une table
	 *
	 * @param connexion
	 * @param tableIn
	 * @return
	 * @throws ArcException
	 */
	public Collection<String> getColumns(Connection connexion, Collection<String> liste, String tableIn)
			throws ArcException {
		liste.addAll(new GenericBean(executeRequest(connexion, FormatSQL.listeColonneByHeaders(tableIn))).getHeaders());
		return liste;
	}

	public List<String> getColumns(Connection connexion, String tableIn) throws ArcException {
		return new ArrayList<>(getColumns(connexion, new ArrayList<>(), tableIn));
	}

	/**
	 * Met à jour un table en mode bulk c'est à dire en recréant l'image modifiée de
	 * la table à mettre à jour et en droppant l'ancienne Equivalent à UPDATE table
	 * SET [@param set] WHERE [@param where]
	 * 
	 * @param aConnexion
	 * @param tableName
	 * @param keys       : clé de jointure. ex: "id_source,id"
	 * @param colList    : la liste complète des colonnes de la table
	 * @param where      : la clause where sur laquelle se fait la mise à jour. ex :
	 *                   "id='12' and id_source like 'a%'"
	 * @param set        ... : la nouvelle valeur et le nom de la colonne a mettre à
	 *                   jour "12 as a"
	 * @throws ArcException
	 */
	public void fastUpdate(Connection aConnexion, String tableName, String keys, List<String> colList, String where,
			String... set) throws ArcException {
		// récupérer la liste des colonnes
		// liste de toutes les colonnes
		// liste des colonnes à mettre à jour
		List<String> colSetList = new ArrayList<>();
		List<String> setList = new ArrayList<>();
		for (int i = 0; i < set.length; i++) {
			// extraire la colonne à mettre à jour; la garder ssi elle existe
			// dans le modèle de la table à mettre à
			// jour.
			String col = ManipString.substringAfterLast(set[i].trim(), "as ").toUpperCase();
			if (colList.contains(col)) {
				colSetList.add(col);
				setList.add(set[i]);
			}
		}
		// liste des colonnes de la jointure (clé primaire de la table initiale)
		List<String> colKeyList = new ArrayList<>();
		for (int i = 0; i < keys.split(",").length; i++) {
			colKeyList.add(keys.split(",")[i].trim().toUpperCase());
		}
		// construction de la requete
		StringBuilder requete = new StringBuilder();
		String tableFastUpdate = UtilitaireDaoTemporaryTable.TABLE_FAST_UPDATE_CONTAINER;
		String tableImage = UtilitaireDaoTemporaryTable.TABLE_FAST_UPDATE_IMAGE;
		requete.append(" drop table if exists " + tableFastUpdate + ";");

		requete.append("\n create  ");
		// Si pas de schema défini, on fait une table temporaire
		if (!tableFastUpdate.contains(".")) {
			requete.append("temporary ");
		}

		requete.append("table " + tableFastUpdate + " " + FormatSQL.WITH_NO_VACUUM + " as ");
		requete.append("\n select " + keys + " ");
		for (int i = 0; i < setList.size(); i++) {
			requete.append("," + setList.get(i));
		}
		requete.append("\n FROM " + tableName + " ");
		requete.append("\n WHERE " + where + ";");

		requete.append("\n drop table if exists " + tableImage + ";");
		requete.append("\n set enable_nestloop=off; ");
		requete.append("\n create ");
		if (!tableFastUpdate.contains(".")) {
			requete.append("temporary ");
		}

		requete.append("table " + tableImage + " " + FormatSQL.WITH_NO_VACUUM + " as ");
		requete.append("\n SELECT ");
		for (int i = 0; i < colList.size(); i++) {
			if (i > 0) {
				requete.append(",");
			}
			requete.append("a." + colList.get(i));
		}
		requete.append("\n FROM " + tableName + " a");
		requete.append("\n WHERE NOT EXISTS (select 1 from " + tableFastUpdate + " b ");
		requete.append("\n WHERE ");
		for (int i = 0; i < colKeyList.size(); i++) {
			if (i > 0) {
				requete.append("AND ");
			}
			requete.append("a." + colKeyList.get(i) + "=b." + colKeyList.get(i) + " ");
		}
		requete.append("\n) ");
		requete.append("\n UNION ALL ");
		requete.append("\n SELECT ");
		for (int i = 0; i < colList.size(); i++) {
			if (i > 0) {
				requete.append(",");
			}
			if (colSetList.contains(colList.get(i))) {
				requete.append("b." + colList.get(i));
			} else {
				requete.append("a." + colList.get(i));
			}
		}
		requete.append("\n FROM " + tableName + " a, " + tableFastUpdate + " b WHERE ");
		for (int i = 0; i < colKeyList.size(); i++) {
			if (i > 0) {
				requete.append(" AND ");
			}
			requete.append("a." + colKeyList.get(i) + "=b." + colKeyList.get(i));
		}
		requete.append(";");
		requete.append("\n set enable_nestloop=on; ");
		requete.append("\n drop table if exists " + tableFastUpdate + " ;");
		requete.append("\n drop table if exists " + tableName + ";");
		requete.append(
				"\n alter table " + tableImage + " rename to " + ManipString.substringAfterFirst(tableName, ".") + ";");
		requete.append("analyze " + tableName + " (" + keys + ");");
		executeRequest(aConnexion, requete);
		requete.setLength(0);
	}

	/**
	 * export de table postgres dans un stream
	 *
	 * @param connexion
	 * @param table
	 * @param os
	 * @param csv       : true / false (binary)
	 * @throws ArcException
	 * @throws IOException
	 */
	public void exporting(Connection connexion, String table, OutputStream os, boolean csv) throws ArcException {
		ConnectionWrapper conn = initConnection(connexion);

		try {
			CopyManager copyManager = new CopyManager((BaseConnection) conn.getConnexion());
			if (csv) {
				copyManager.copyOut("COPY " + table
						+ " TO STDOUT WITH (FORMAT csv, HEADER false , DELIMITER ';' , ENCODING 'UTF8') ", os);
			} else {
				copyManager.copyOut("COPY " + table + " TO STDOUT WITH (FORMAT BINARY)", os);
			}
		} catch (SQLException sqlException) {
			throw new ArcException(sqlException, ArcExceptionMessage.SQL_EXECUTE_FAILED, sqlException.getMessage()).logFullException();
		} catch (IOException e) {
			throw new ArcException(e, ArcExceptionMessage.STREAM_WRITE_FAILED);
		} finally {
			conn.close();
		}
	}

	/**
	 * Copie brutal de fichier plat dans une table SQL.
	 *
	 * @param connexion
	 * @param table
	 * @param is
	 * @param csv
	 * @param header
	 * @param aDelim
	 * @throws ArcException
	 */
	public void importingWithReader(Connection connexion, String table, Reader aReader, boolean header,
			String... aDelim) throws ArcException {
		ConnectionWrapper conn = initConnection(connexion);
		try {
			conn.getConnexion().setAutoCommit(false);
			CopyManager copyManager = new CopyManager((BaseConnection) conn.getConnexion());
			String delimiter = "";
			String quote = "\"";

			if (aDelim != null && aDelim.length > 0) {
				delimiter = ", DELIMITER '" + aDelim[0] + "', QUOTE '" + quote + "' ";
			}

			if (aDelim != null && aDelim.length > 1) {
				header = false;
			}

			String h = (header ? ", HEADER true " : "");

			copyManager.copyIn("COPY " + table + " FROM STDIN WITH (FORMAT CSV " + h + delimiter + ") ", aReader);

			conn.getConnexion().commit();
		} catch (SQLException | IOException e) {

			LoggerHelper.error(LOGGER, e);

			try {
				conn.getConnexion().rollback();
			} catch (SQLException e1) {
				throw new ArcException(e1, ArcExceptionMessage.DATABASE_ROLLBACK_FAILED);
			}

			throw new ArcException(e, ArcExceptionMessage.IMPORTING_FAILED);

		} finally {
			conn.close();
		}
	}

	/**
	 * Copie brutal de fichier plat dans une table SQL.
	 * 
	 * @param connexion
	 * @param table       nom de la table à remplir
	 * @param aColumnName
	 * @param is
	 * @param format      le format des données (CSV, TEXT ou BINARY)
	 * @param header      le flux de données contient-il en première ligne la liste
	 *                    des colonnes ?
	 * @param aDelim      le délimiter (exemple le point virgule)
	 * @param aQuote
	 * @param encoding    : default = UTF8
	 * @throws ArcException
	 * @throws ArcException
	 */
	public void importing(Connection connexion, String table, String aColumnName, InputStream is, boolean header,
			String aDelim, String aQuote, String encoding) throws ArcException {
		LoggerHelper.info(LOGGER, "importing()");
		try (ConnectionWrapper conn = initConnection(connexion);) {
			conn.getConnexion().setAutoCommit(false);
			CopyManager copyManager = new CopyManager((BaseConnection) conn.getConnexion());
			String delimiter = "";
			String quote = "";
			String columnName = "";
			String encode = StandardCharsets.UTF_8.name();

			if (aDelim != null && aDelim.length() == 1) {
				delimiter = ", DELIMITER '" + aDelim + "'";
			}

			if (aQuote != null && aQuote.length() == 1) {
				quote = ", QUOTE '" + aQuote + "'";
			}

			if (aColumnName != null && !aColumnName.equals("")) {
				columnName = aColumnName;
			}

			if (encoding != null) {
				encode = encoding;
			}

			String h = (header ? ", HEADER true " : "");

			copyManager.copyIn("COPY " + table + columnName + " FROM STDIN WITH (FORMAT CSV, ENCODING '" + encode + "' "
					+ h + delimiter + quote + ") ", is);

			conn.getConnexion().commit();

			LoggerHelper.info(LOGGER, "importing done");

		} catch (IOException e) {
			if (e.getMessage().startsWith(ArcExceptionMessage.IMPORTING_JAVA_EXCEPTION_DATA_MISSING.getMessage())) {

				throw new ArcException(e, ArcExceptionMessage.IMPORTING_COLUMNS_MISSING).logFullException();

			} else if (e.getMessage()
					.startsWith(ArcExceptionMessage.IMPORTING_JAVA_EXCEPTION_HEADERS_MISSING.getMessage())) {

				throw new ArcException(e, ArcExceptionMessage.IMPORTING_HEADERS_MISSING).logFullException();

			} else {
				throw new ArcException(e, ArcExceptionMessage.STREAM_READ_FAILED).logFullException();
			}

		} catch (SQLException sqlException) {
			throw new ArcException(sqlException, ArcExceptionMessage.SQL_EXECUTE_FAILED, sqlException.getMessage()).logFullException();
		}
	}

	/**
	 * Send a vacuum query
	 * @param connexion
	 * @param table
	 * @param mode
	 * @throws ArcException
	 */
	public void vacuumSecured(Connection connexion, String tableToVacuum, String vacuumMode) throws ArcException
	{
		// vacuumSecured requires executeImmediate autocommit set to true
		executeImmediate(connexion,new GenericPreparedStatementBuilder(FormatSQL.vacuumSecured(tableToVacuum, vacuumMode)));	
	}
	
	/**
	 * Send a analyze query
	 * @param connexion
	 * @param table
	 * @param mode
	 * @throws ArcException
	 */
	public void analyzeSecured(Connection connexion, String table) throws ArcException
	{
		executeRequest(connexion,FormatSQL.analyzeSecured(table));
	}

	
	/**
	 * retrieve the columns definition of the input table
	 * 
	 * @param targetConnection
	 * @param inputTable
	 * @return
	 * @throws ArcException
	 */
	public ColumnAttributes retrieveColumnAttributes(Connection connection, String tablename) throws ArcException {
		GenericPreparedStatementBuilder query =new GenericPreparedStatementBuilder();
		query.build(SQL.SELECT, "*", SQL.FROM, tablename, SQL.WHERE, SQL.FALSE);
		GenericBean gb = new GenericBean(UtilitaireDao.get(0).executeRequest(connection, query));
		return new ColumnAttributes(gb);
	}
	
	
	public PropertiesHandler getProperties() {
		return properties;
	}

	public void setProperties(PropertiesHandler properties) {
		this.properties = properties;
	}

}
