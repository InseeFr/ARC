package fr.insee.arc.utils.dao;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.ressourceUtils.SpringApplicationContext;
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
@Component
public class UtilitaireDao implements IConstanteNumerique, IConstanteCaractere {

	private static final Logger LOGGER = LogManager.getLogger(UtilitaireDao.class);

	public static final int READ_BUFFER_SIZE = 131072;
    
	
	
	/**
	 * defaut number of tries to initialize an connexion to arc database
	 */
	private static final int DEFAULT_NUMBER_OF_CONNECTION_TRIES = 5;
	
	/**
	 * delay between connection tries to arc database
	 */
	private static final int MILLISECOND_BETWEEN_CONNECTION_TRIES = 1000;

	/**
	 * configurable parameter to set the number of try to get a connexion to arc database
	 */
	private int nbTryMax = DEFAULT_NUMBER_OF_CONNECTION_TRIES;
	/**
	 * Format des données utilisées dans la commande copy
	 */
	public static final String FORMAT_BINARY = "BINARY";
	/**
	 * Format des données utilisées dans la commande copy
	 */
	public static final String FORMAT_TEXT = "TEXT";
	/**
	 * Format des données utilisées dans la commande copy
	 */
	public static final String FORMAT_CSV = "CSV";
	/**
	 * execute request returns a table with headers, type and data
	 * provide the indexes of these elements
	 */
	public static final int EXECUTE_REQUEST_HEADERS_START_INDEX = 0;
	public static final int EXECUTE_REQUEST_TYPES_START_INDEX = 1;
	public static final int EXECUTE_REQUEST_DATA_START_INDEX = 2;


	private String pool;
	private static Map<String, UtilitaireDao> map;
	private boolean silent = false;

	@Autowired
	PropertiesHandler properties;

	private UtilitaireDao(String aPool) {
		this.pool = aPool;
		if (map == null) {
			map = new HashMap<>();
		}
		if (!map.containsKey(aPool)) {
			map.put(aPool, this);
		}
	}

	public static final UtilitaireDao get(String aPool) {
		if (map == null) {
			map = new HashMap<>();
		}
		if (!map.containsKey(aPool)) {
			map.put(aPool, (UtilitaireDao) SpringApplicationContext.getBean("utilitaireDao",aPool));
		}
		return map.get(aPool);
	}

	public static final UtilitaireDao get(String aPool, int nbTry) {
		get(aPool).nbTryMax = nbTry;
		return get(aPool);
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
			Class.forName(properties.getDatabaseDriverClassName());
			boolean connectionOk = false;
			int nbTry = 0;
			Connection c = null;
			
			while (!connectionOk && nbTry < nbTryMax) {
			// renvoie la connexion relative au driver
				try {
				c = DriverManager.getConnection(properties.getDatabaseUrl(), properties.getDatabaseUsername(),
						properties.getDatabasePassword());
					connectionOk = true;
				} catch (Exception e) {
					LoggerHelper.error(LOGGER,
							"Connection failure. Tentative de reconnexion dans " + MILLISECOND_BETWEEN_CONNECTION_TRIES +" milisecondes", nbTry);
					try {
						Thread.sleep(MILLISECOND_BETWEEN_CONNECTION_TRIES);
					} catch (InterruptedException e1) {
						Thread.currentThread().interrupt();
					}
				}
				nbTry++;
			}
		if (!connectionOk) {
			throw new ArcException("La connexion n'a pu aboutir");
		}
		return c;
		
		} catch (ClassNotFoundException e1) {
			throw new ArcException("L'initialisation de la connexion a échouée");
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
	public static boolean isConnectionOk(String pool) {
		try {
			get(pool, 1).executeRequest(null, new GenericPreparedStatementBuilder("select true"));
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
			b = getBoolean(connexion, FormatSQL.isTableExists(table));
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
	public void dropTable(Connection connexion, List<String> someTables) {
		try {
			if (someTables != null && !someTables.isEmpty()) {
				executeBlock(connexion, //
						new StringBuilder("DROP TABLE IF EXISTS ")//
								.append(Format.untokenize(someTables, ";\n DROP TABLE IF EXISTS "))//
								.append(";")//
				);
			}
		} catch (ArcException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "dropTable()", LOGGER, ex);
		}
	}

	/**
	 * @param connexion  la connexion à la base
	 * @param someTables le nom des tables
	 * @return
	 */
	public void dropTable(Connection connexion, String... someTables) {
		dropTable(connexion, Arrays.asList(someTables));
	}

	/**
	 * Exécute une requête {@code sql} avec des arguments {@code args}, renvoie le
	 * booléen (unique) que cette requête est censée rendre<br/>
	 *
	 *
	 * @param sql
	 * @param args
	 * @return
	 * @throws ArcException 
	 */
	public Boolean getBoolean(Connection connexion, GenericPreparedStatementBuilder sql, String... args) throws ArcException {
		
		String returned;
		returned = getString(connexion,sql,args);
		
		if (returned==null)
		{
			return null;
		}
		
		if (returned.equals("f"))
		{
			return false;
		}
		
		if (returned.equals("t"))
		{
			return true;
		}
		
		return null;
	}


	/**
	 * Compter les lignes d'une requête
	 *
	 * @param connexion
	 * @param requete   le résultat d'une requête non aliasée comme
	 *                  <code>SELECT * FROM nom_table</code>
	 * @return
	 */
	public Long getCountFromRequest(Connection connexion, String requete) {
		return getCount(connexion, "(" + requete + ") foo");
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
	public String getString(Connection connexion, GenericPreparedStatementBuilder requete, String... args) throws ArcException {
			requete.setQuery(new StringBuilder(Format.parseStringAvecArguments(requete.getQuery().toString(), args)));
			ArrayList<ArrayList<String>> returned=executeRequest(connexion, requete , ModeRequete.EXTRA_FLOAT_DIGIT);
			return (returned.size() <= EXECUTE_REQUEST_DATA_START_INDEX ? null : returned.get(EXECUTE_REQUEST_DATA_START_INDEX).get(0));

	}

	

	public Date getDate(Connection aConnexion, GenericPreparedStatementBuilder aRequete, SimpleDateFormat aSimpleDateFomrat)
			throws ArcException {
		String resultat = getString(aConnexion, aRequete);
		try {
			return resultat == null ? null : aSimpleDateFomrat.parse(resultat);
		} catch (ParseException e) {
			throw new ArcException(e);
		}
	}
	


	/**
	 * Exécute une requête qui renvoie exactement un argument de type {@link Long}.
	 *
	 * @param connexion la connexion à la base
	 * @param sql       la requête
	 * @param args      les arguments de la requête (optionnels)
	 * @return
	 */
	public Long getLong(Connection connexion, GenericPreparedStatementBuilder sql, String... args) {
		String returned;
		try {
			returned = getString(connexion,sql,args);
			return (returned == null ? Long.MIN_VALUE : Long.parseLong(returned));
		} catch (ArcException e) {
			LoggerHelper.errorGenTextAsComment(getClass(), "getInt()", LOGGER, e);
		}
		return Long.MIN_VALUE;
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
			ArrayList<ArrayList<String>> returned = executeRequest(connexion, sql, modes);
			return (returned.size() <= EXECUTE_REQUEST_DATA_START_INDEX ? ZERO : Integer.parseInt(returned.get(EXECUTE_REQUEST_DATA_START_INDEX).get(0)));
		} catch (Exception ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "getInt()", LOGGER, ex);
		}
		return ZERO;
	}

	/**
	 *
	 * @param connexion
	 * @param table
	 * @param column
	 * @return la valeur maximale obtenue sur la colonne {@code column} de la table
	 *         {@code table}
	 */
	public int getMax(Connection connexion, String table, String column) {
		return getInt(connexion, new GenericPreparedStatementBuilder("select max(" + column + ") max_value from " + table));
	}

	public boolean isColonneExiste(Connection aConnexion, String aNomTable, String aNomVariable) throws ArcException {
		return getColumns(aConnexion, new HashSet<String>(), aNomTable).contains(aNomVariable);
	}

	/**
	 *
	 * @param connexion
	 * @param aTable
	 * @return La valeur prise par {@code SELECT COUNT(*) FROM <aTable>}
	 */
	public long getCount(Connection connexion, String aTable) {
		return getCount(connexion, aTable, null);
	}

	/**
	 *
	 * @param connexion
	 * @param aTable
	 * @param clauseWhere
	 * @return La valeur prise par
	 *         {@code SELECT COUNT(*) FROM <aTable> WHERE <clauseWhere>}
	 */
	public long getCount(Connection connexion, String aTable, GenericPreparedStatementBuilder clauseWhere) {
		
		GenericPreparedStatementBuilder requete=new GenericPreparedStatementBuilder();
		
		requete.append("SELECT count(1) FROM " + aTable);
		
		if (clauseWhere.length()==0)
				{
				requete.append(" WHERE ");
				requete.append(clauseWhere);
				}
		return getLong(connexion, requete );
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
	public ArrayList<ArrayList<String>> executeRequestWithoutMetadata(Connection connexion, GenericPreparedStatementBuilder requete,
			ModeRequete... modes) throws ArcException {
		ArrayList<ArrayList<String>> returned = executeRequest(connexion, requete, modes);
		returned.remove(0);
		returned.remove(0);
		return returned;
	}

	public void executeImmediate(Connection connexion, StringBuilder requete, ModeRequete... modes)
			throws ArcException {
		executeImmediate(connexion, requete.toString(), modes);
	}


	public void executeImmediate(Connection connexion, String requete, ModeRequete... modes) throws ArcException {

		long start = new Date().getTime();
		
		LoggerHelper.trace(LOGGER, "/* executeImmediate on */");
		LoggerHelper.trace(LOGGER, "\n"+requete.trim());

		ConnectionWrapper connexionWrapper = initConnection(connexion);
		try {		
			connexionWrapper.getConnexion().setAutoCommit(true);
			try(Statement st = connexionWrapper.getConnexion().createStatement();)
			{
				try {
					st.execute(ModeRequete.configureQuery(requete,modes));
					LoggerHelper.traceAsComment(LOGGER, "DUREE : ", (new Date().getTime() - start) + "ms");
				} catch (SQLException e) {
					st.cancel();
					LoggerHelper.error(LOGGER, e);
					LoggerHelper.error(LOGGER, requete);
					throw e;
				}
			}
		} catch (SQLException e) {
			LoggerHelper.error(LOGGER, e);
			throw new ArcException(e);
		} finally {
			if (connexionWrapper.isLocal()) {
				connexionWrapper.close();
			}
		}
	}

	
	/**
	 * CHeck if a query will give result or not
	 * @param connexion
	 * @param requete
	 * @return
	 * @throws ArcException
	 */
	public Boolean testResultRequest(Connection connexion, GenericPreparedStatementBuilder requete) {
		GenericPreparedStatementBuilder requeteLimit = new GenericPreparedStatementBuilder();
		requeteLimit.append("SELECT * from (").append(requete).append(") dummyTable0000000 LIMIT 1");		
		try {
			return hasResults(null, requeteLimit);
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
	public ArrayList<ArrayList<String>> executeRequest(Connection connexion, GenericPreparedStatementBuilder requete,  ModeRequete... modes)
			throws ArcException {
		return executeRequest(connexion, requete, EntityProvider.getArrayOfArrayProvider(), modes);

	}
	
	/**
	 * Exécution de requêtes ramenant des enregistrements en mode Statement (donc sans cache)
	 * @param connexion
	 * @param requete
	 * @return
	 * @throws ArcException
	 */
	public ArrayList<ArrayList<String>> executeStatement(Connection connexion, GenericPreparedStatementBuilder requete)
			throws ArcException {
		return executeStatement(connexion, requete, EntityProvider.getArrayOfArrayProvider());

	}
	
	/**
	 * 
	 * @param <T>
	 * @param connexion
	 * @param requete
	 * @param entityProvider
	 * @return
	 * @throws ArcException
	 */
	public <T> T executeStatement(Connection connexion, GenericPreparedStatementBuilder requete, EntityProvider<T> entityProvider) throws ArcException {

		LoggerHelper.trace(LOGGER, "/* executeRequest on */");
		LoggerHelper.trace(LOGGER, "\n"+ModeRequete.configureQuery(requete.getQueryWithParameters()));
		LoggerHelper.trace(LOGGER, requete.getParameters());
		
		this.silent=false;
		
		try {
			ConnectionWrapper connexionWrapper = initConnection(connexion);
			try {
				connexionWrapper.getConnexion().setAutoCommit(false);
				try(Statement stmt = connexionWrapper.getConnexion().createStatement();)
				{
					try {
						ResultSet res = stmt.executeQuery(requete.getQuery().toString());
						return entityProvider.apply(res);
					} catch (SQLException e) {
						if (!this.silent) {
							LoggerHelper.error(LOGGER, stmt.toString());
						}
						throw e;
					} finally {
						connexionWrapper.getConnexion().commit();
					}
				}
			} catch (SQLException e) {
				if (!this.silent) {
					LoggerHelper.error(LOGGER, "executeStatement()", e);
				}
				connexionWrapper.getConnexion().rollback();
				throw e;
			} finally {
				connexionWrapper.close();
			}
		} catch (SQLException ex) {
			if (!this.silent) {
				LoggerHelper.error(LOGGER, "Lors de l'exécution de", requete.getQuery());
			}
			throw new ArcException(ex);
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
	public <T> T executeRequest(Connection connexion, GenericPreparedStatementBuilder requete, EntityProvider<T> entityProvider,
			ModeRequete... modes) throws ArcException {

		long start = new Date().getTime();
		LoggerHelper.trace(LOGGER, "/* executeRequest on */");
		LoggerHelper.trace(LOGGER, "\n"+ModeRequete.configureQuery(requete.getQueryWithParameters()));
		LoggerHelper.trace(LOGGER, requete.getParameters());
		
		this.silent=false;
		
		try {
			ConnectionWrapper connexionWrapper = initConnection(connexion);
			try {
				connexionWrapper.getConnexion().setAutoCommit(false);
				try(PreparedStatement stmt = connexionWrapper.getConnexion().prepareStatement(ModeRequete.configureQuery(requete.getQuery().toString(),modes));)
				{
					for (int i=0;i<requete.getParameters().size();i++)
					{
						stmt.setString(i+1, requete.getParameters().get(i));
					}
	
					try {
						// the first result found will be output
						boolean isresult = stmt.execute();
						LoggerHelper.traceAsComment(LOGGER, "DUREE : ", (new Date().getTime() - start) + "ms");

						if (!isresult)
						{
							do {
								isresult=stmt.getMoreResults();
								if (isresult) {
									break;
								}
								if (stmt.getUpdateCount() == -1)
								{
									break;
								}
							} while (true);
						}
						
						if (isresult) {
							ResultSet res = stmt.getResultSet();
							return entityProvider.apply(res);
						}
						return null;
					} catch (SQLException e) {
						if (!this.silent) {
							LoggerHelper.error(LOGGER, stmt.toString());
						}
						throw e;
					} finally {
						connexionWrapper.getConnexion().commit();
					}
				}
			} catch (SQLException e) {
				if (!this.silent) {
					LoggerHelper.error(LOGGER, "executeRequest()", e);
				}
				connexionWrapper.getConnexion().rollback();
				throw e;
			} finally {
				connexionWrapper.close();
			}
		} catch (SQLException ex) {
			if (!this.silent) {
				LoggerHelper.error(LOGGER, "Lors de l'exécution de", requete.getQuery());
			}
			throw new ArcException(ex);
		}
	}

	/**
	 * 
	 * Classe bridge qui permet d'utiliser l'interface de {@link UtilitaireDao} dans
	 * d'autres classes du projet.<br/>
	 * Pourquoi ?<br/>
	 * Parce que les autres classes étaient initialement prévues pour faire partie
	 * d'un package d'ORM complet, mais inachevé. La gestion des COMMIT, ROLLBACK ne
	 * s'y fait pas de façon unifiée, donc l'interface de ce package ORM fait appel
	 * à {@link UtilitaireDAO}.
	 *
	 * @param <T>
	 */
	public static abstract class EntityProvider<T> implements Function<ResultSet, T> {
		private static final class ArrayOfArrayProvider extends EntityProvider<ArrayList<ArrayList<String>>> {
			@Override
			public ArrayList<ArrayList<String>> apply(ResultSet res) {
				try {
					return fromResultSetToArray(res);
				} catch (ArcException ex) {
					throw new ArcException(ex);
				}
			}
		}

		public static final EntityProvider<ArrayList<ArrayList<String>>> getArrayOfArrayProvider() {
			return new ArrayOfArrayProvider();
		}

		private static final class GenericBeanProvider extends EntityProvider<GenericBean> {
			@Override
			public GenericBean apply(ResultSet res) {
				return new GenericBean(getArrayOfArrayProvider().apply(res));
			}
		}

		public static final EntityProvider<GenericBean> getGenericBeanProvider() {
			return new GenericBeanProvider();
		}

		private static final class TypedListProvider<T> extends EntityProvider<List<T>> {
			private Function<ResultSet, T> orm;

			/**
			 * @param orm
			 */
			TypedListProvider(Function<ResultSet, T> orm) {
				this.orm = orm;
			}

			@Override
			public List<T> apply(ResultSet res) {
				try {
					return fromResultSetToListOfT(() -> new ArrayList<>(), this.orm, res);
				} catch (ArcException ex) {
					throw new ArcException(ex);
				}
			}
		}

		public static final <T> EntityProvider<List<T>> getTypedListProvider(Function<ResultSet, T> orm) {
			return new TypedListProvider<>(orm);
		}

		private static final class DefaultEntityProvider<T> extends EntityProvider<T> {
			private Function<ResultSet, T> orm;

			/**
			 * @param orm
			 */
			DefaultEntityProvider(Function<ResultSet, T> orm) {
				this.orm = orm;
			}

			@Override
			public T apply(ResultSet res) {
				return this.orm.apply(res);
			}
		}

		public static final <T> EntityProvider<T> getDefaultEntityProvider(Function<ResultSet, T> orm) {
			return new DefaultEntityProvider<>(orm);
		}
	}

	public static final GenericBean fromResultSetToGenericBean(ResultSet res) {
		try {
			return new GenericBean(fromResultSetToArray(res));
		} catch (ArcException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public static ArrayList<ArrayList<String>> fromResultSetToArray(ResultSet res) throws ArcException {
		return fromResultSetToList(() -> new ArrayList<>(), new ArrayList<>(), res);
	}

	public static <T extends List<String>, U extends List<T>> U fromResultSetToList(Supplier<T> newList, U result,
			ResultSet res) throws ArcException {
		try {
		ResultSetMetaData rsmd = res.getMetaData();
		T record = newList.get();
		// Noms des colonnes
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			record.add(rsmd.getColumnLabel(i));
		}
		result.add(record);
		// Types des colonnes
		record = newList.get();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			/*
			 * le ResultSetMetaData fait un peu n'importe quoi avec les types. Si on a un
			 * int/bigint + sequence, il renvoit une serial/bigserial. sauf que l'on n'en
			 * veut pas, alors on doit corriger ça à la main
			 */
			HashMap<String, String> correctionType = new HashMap<String, String>();
			correctionType.put("serial", "int4");
			correctionType.put("bigserial", "int8");
			if (correctionType.containsKey(rsmd.getColumnTypeName(i))) {
				record.add(correctionType.get(rsmd.getColumnTypeName(i)));
			} else {
				record.add(rsmd.getColumnTypeName(i));
			}
		}
		result.add(record);
		while (res.next()) {
			record = newList.get();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				record.add(res.getString(i));
			}
			result.add(record);
		}
		return result;
		}
		catch (SQLException e)
		{
			throw new ArcException(e);
		}
	}

	public static <T, U extends List<T>> U fromResultSetToListOfT(Supplier<U> newList, Function<ResultSet, T> orm,
			ResultSet res) throws ArcException {
		try {
		U result = newList.get();
			while (res.next()) {
				result.add(orm.apply(res));
			}
		return result;
		} catch (SQLException e) {
			throw new ArcException(e);
		}
	}

	/**
	 * Renvoie true si une liste issue de requete a au moins un enregistrement <br/>
	 *
	 *
	 * @param l
	 * @return
	 */
	public static Boolean hasResults(ArrayList<ArrayList<String>> l) {
		return (l.size() > 2);
	}

	public Boolean hasResults(Connection connexion, GenericPreparedStatementBuilder requete) throws ArcException {
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
	public void outStreamRequeteSelect(Connection connexion, GenericPreparedStatementBuilder requete, OutputStream out) throws ArcException {
		StringBuilder str = new StringBuilder();
		String lineSeparator = "\n";
		int k = 0;
		int fetchSize = 5000;
		boolean endLoop = false;
		try (ConnectionWrapper connexionWrapper = initConnection(connexion))
			{
				while (!endLoop) {
					try {
						GenericPreparedStatementBuilder requeteLimit=new GenericPreparedStatementBuilder();
						requeteLimit.append(requete);
						requeteLimit.append(" offset " + (k * fetchSize) + " limit " + fetchSize + " ");
						
						try(PreparedStatement stmt = connexionWrapper.getConnexion().prepareStatement(requeteLimit.getQuery().toString()))
						{
						
							// bind parameters
							for (int i=0;i<requete.getParameters().size();i++)
							{
								stmt.setString(i+1, requete.getParameters().get(i));
							}
							
							// build file output
							try (ResultSet res = stmt.executeQuery())
								{
								ResultSetMetaData rsmd = res.getMetaData();
								if (k == 0) {
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
								}
								while (res.next()) {
									for (int i = 1; i <= rsmd.getColumnCount(); i++) {
										if (res.getString(i) != null) {
											str.append(res.getString(i).replace("\n", " ").replace("\r", ""));
										} else {
										}
										if (i < rsmd.getColumnCount()) {
											str.append(";");
										}
									}
									str.append(lineSeparator);
								}
								out.write(str.toString().getBytes());
								endLoop = (str.length() == 0);
								k++;
								str.setLength(0);
							}
						}
					} catch (Exception e) {
						LoggerHelper.trace(LOGGER, e.getMessage());
						throw e;
					}
				}
		} catch (Exception ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "outStreamRequeteSelect()", LOGGER, ex);
		}
	}

	public static void createDirIfNotexist(File f) {
		if (!f.exists()) {
			f.mkdirs();
		}
	}

	public static void createDirIfNotexist(String fPath) {
		File f = new File(fPath);
		createDirIfNotexist(f);
	}

	/**
	 *
	 * @param fileIn
	 * @param fileOut
	 * @param entryName
	 * @throws IOException
	 */
	public static void generateTarGzFromFile(File fileIn, File fileOut, String entryName) throws ArcException {

		try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(fileIn),READ_BUFFER_SIZE);)
		{
			try(TarArchiveOutputStream taos = new TarArchiveOutputStream(new GZIPOutputStream(new FileOutputStream(fileOut)));)
			{
				TarArchiveEntry entry = new TarArchiveEntry(entryName);
				entry.setSize(fileIn.length());
				taos.putArchiveEntry(entry);
				copyFromInputstreamToOutputStream(fis, taos);
				taos.closeArchiveEntry();
			}
		} catch (IOException e) {
			throw new ArcException(e);
		}
	}

	/**
	 * Verifie que l'archive zip existe, lit les fichiers de la listIdSource et les
	 * copie dans un TarArchiveOutputStream
	 *
	 * @param receptionDirectoryRoot
	 * @param phase
	 * @param etat
	 * @param currentContainer
	 * @param listIdSourceContainer
	 */
	public static void generateEntryFromFile(String receptionDirectoryRoot, String idSource,
			TarArchiveOutputStream taos) {
		File fileIn = Paths.get(receptionDirectoryRoot,idSource).toFile();
		if (fileIn.exists()) {
			try {
				TarArchiveEntry entry = new TarArchiveEntry(fileIn.getName());
				entry.setSize(fileIn.length());
				taos.putArchiveEntry(entry);
				// Ecriture dans le fichier
				copyFromInputstreamToOutputStream(new BufferedInputStream(new FileInputStream(fileIn),READ_BUFFER_SIZE), taos);
				taos.closeArchiveEntry();
			} catch (IOException ex) {
				LoggerHelper.errorGenTextAsComment(UtilitaireDao.class, "generateEntryFromFile()", LOGGER, ex);
			}
		}
	}

	/**
	 * Verifie que l'archive zip existe, lit les fichiers de la listIdSource et les
	 * copie dans un TarArchiveOutputStream
	 *
	 * @param receptionDirectoryRoot
	 * @param phase
	 * @param etat
	 * @param currentContainer
	 * @param listIdSourceContainer
	 */
	public static void generateEntryFromZip(String receptionDirectoryRoot, String currentContainer,
			ArrayList<String> listIdSourceContainer, TarArchiveOutputStream taos) {
		File fileIn = Paths.get(receptionDirectoryRoot, currentContainer).toFile();
		if (fileIn.exists()) {
			try {
				try(ZipInputStream tarInput = new ZipInputStream(new BufferedInputStream(new FileInputStream(fileIn),READ_BUFFER_SIZE));)
				{
					ZipEntry currentEntry = tarInput.getNextEntry();
					// si le fichier est trouvé, on ajoute
					while (currentEntry != null) {
						if (listIdSourceContainer.contains(currentEntry.getName())) {
							TarArchiveEntry entry = new TarArchiveEntry(currentEntry.getName());
							entry.setSize(currentEntry.getSize());
							taos.putArchiveEntry(entry);
							for (int c = tarInput.read(); c != -1; c = tarInput.read()) {
								taos.write(c);
							}
							taos.closeArchiveEntry();
						}
						currentEntry = tarInput.getNextEntry();
					}
				}
			} catch (IOException ex) {
				LoggerHelper.errorGenTextAsComment(UtilitaireDao.class, "generateEntryFromZip()", LOGGER, ex);
			}
		}
	}

	/**
	 * Verifie que l'archive .tar.gz existe, lit les fichiers de la listIdSource et
	 * les copie dans un TarArchiveOutputStream
	 *
	 * @param receptionDirectoryRoot
	 * @param entryPrefix
	 * @param currentContainer
	 * @param listIdSourceContainer
	 * @param taos
	 */
	public static void generateEntryFromTarGz(String receptionDirectoryRoot, String currentContainer,
			ArrayList<String> listIdSourceContainer, TarArchiveOutputStream taos) {
		File fileIn = new File(receptionDirectoryRoot + File.separator + currentContainer);
		LoggerHelper.traceAsComment(LOGGER, "#generateEntryFromTarGz()", receptionDirectoryRoot, "/", currentContainer);
				
		if (fileIn.exists()) {
			// on crée le stream pour lire à l'interieur de
			// l'archive
			try {
				try(TarInputStream tarInput = new TarInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(fileIn),READ_BUFFER_SIZE)));)
				{
					TarEntry currentEntry = tarInput.getNextEntry();
					// si le fichier est trouvé, on ajoute
					while (currentEntry != null) {
						if (listIdSourceContainer.contains(currentEntry.getName())) {
							TarArchiveEntry entry = new TarArchiveEntry(currentEntry.getName());
							entry.setSize(currentEntry.getSize());
							taos.putArchiveEntry(entry);
							tarInput.copyEntryContents(taos);
							taos.closeArchiveEntry();
						}
						currentEntry = tarInput.getNextEntry();
					}
				}
			} catch (IOException ex) {
				LoggerHelper.errorGenTextAsComment(UtilitaireDao.class, "generateEntryFromTarGz()", LOGGER, ex);
			}
		}
	}

	/**
	 * Verifie que l'archive .gz existe, lit les fichiers de la listIdSource et les
	 * copie dans un TarArchiveOutputStream
	 *
	 * @param receptionDirectoryRoot
	 * @param entryPrefix
	 * @param currentContainer
	 * @param listIdSourceContainer
	 * @param taos
	 */
	public static void generateEntryFromGz(String receptionDirectoryRoot, String currentContainer,
			ArrayList<String> listIdSourceContainer, TarArchiveOutputStream taos) {
		File fileIn = new File(receptionDirectoryRoot + "/" + currentContainer);
		if (fileIn.exists()) {
			try {
				// on crée le stream pour lire à l'interieur de
				// l'archive
				long size = 0;
				
				try(GZIPInputStream tarInput = new GZIPInputStream(new BufferedInputStream(new FileInputStream(fileIn),READ_BUFFER_SIZE));)
				{
					// on recupere d'abord la taille du stream; gzip ne permet pas
					// de le faire directement
					for (int c = tarInput.read(); c != -1; c = tarInput.read()) {
						size++;
					}
				}
				
				TarArchiveEntry entry = new TarArchiveEntry(listIdSourceContainer.get(0));
				entry.setSize(size);
				taos.putArchiveEntry(entry);
				try(GZIPInputStream tarInput = new GZIPInputStream(new BufferedInputStream(new FileInputStream(fileIn),READ_BUFFER_SIZE));)
				{
					for (int c = tarInput.read(); c != -1; c = tarInput.read()) {
						taos.write(c);
					}
					taos.closeArchiveEntry();
				}
			} catch (IOException ex) {
				LoggerHelper.errorGenTextAsComment(UtilitaireDao.class, "generateEntryFromGz()", LOGGER, ex);
			}
		}
	}

	private static final int BUFFER_SIZE = 1024;

	/**
	 *
	 * copy input to output stream - available in several StreamUtils or Streams
	 * classes
	 *
	 * @param input
	 * @param output
	 * @throws IOException
	 */
	public static void copyFromInputstreamToOutputStream(InputStream input, OutputStream output) throws IOException {
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int n = 0;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
			}
		} finally {
			try {
				input.close();
			} catch (IOException ioe) {
				LoggerHelper.errorAsComment(LOGGER, ioe, "Lors de la clôture de InputStream");
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
	public void getFilesDataStreamFromListOfInputDirectories(Connection connexion, GenericPreparedStatementBuilder requete, TarArchiveOutputStream taos, String path,
			List<String> listRepertoireIn) {
		LoggerHelper.debugDebutMethodeAsComment(getClass(), "copieFichiers()", LOGGER);
		GenericBean g;
		ArrayList<String> listFichier = new ArrayList<>();
		File fileIn = null;
		boolean find;
		String receptionDirectoryRoot = "";
		try {
			g = new GenericBean(this.executeRequest(null, requete));
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
					copyFromInputstreamToOutputStream(new BufferedInputStream(new FileInputStream(fileIn),READ_BUFFER_SIZE), taos);
					taos.closeArchiveEntry();
				}
			}
		} catch (ArcException | IOException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "copieFichiers()", LOGGER, ex);
		}
		LoggerHelper.debugFinMethodeAsComment(getClass(), "copieFichiers()", LOGGER);
	}

	/**
	 * exécute un bloque transactionnel
	 */
	public void executeBlock(Connection connexion, String... listeRequete) throws ArcException {
		StringBuilder bloc = new StringBuilder("BEGIN;\n");
		for (int i = 0; i < listeRequete.length; i++) {
			bloc.append(listeRequete[i]).append(semicolon);
		}
		bloc.append("END;\n");
		executeImmediate(connexion, bloc.toString());
	}

	/**
	 *
	 * @param connexion
	 * @param requete
	 * @throws ArcException
	 */
	public void executeBlockNoError(Connection connexion, StringBuilder requete) throws ArcException {
		executeImmediate(connexion, "do $$ BEGIN " + requete.toString() + " exception when others then END; $$;\n");
	}

	/**
	 *
	 * @param connexion
	 * @param requete
	 * @throws ArcException
	 */
	public void executeBlock(Connection connexion, StringBuilder requete) throws ArcException {
		executeBlock(connexion, requete.toString());
	}

	/**
	 *
	 * @param connexion
	 * @param requete
	 * @throws ArcException
	 */
	public void executeBlock(Connection connexion, String requete) throws ArcException {
		if (!requete.trim().isEmpty()) {
			executeImmediate(connexion, "BEGIN;" + requete + "COMMIT;");
		}
	}


	public List<String> getList(Connection connexion, StringBuilder requete, List<String> returned) {
		return getList(connexion, requete.toString(), returned);
	}

	public List<String> getList(Connection connexion, String requete, List<String> returned) {
		try {
			LoggerHelper.trace(LOGGER, requete);
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

	public List<String> getList(Connection connexion, StringBuilder requete, String nomColonne, List<String> returned) {
		try {
			ConnectionWrapper connexionWrapper = initConnection(connexion);
			try {
				Statement stmt = connexionWrapper.getConnexion().createStatement();
				try {
					ResultSet rs = stmt.executeQuery(requete.toString());
					while (rs.next()) {
						returned.add(rs.getString(nomColonne));
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

	public static boolean isNotArchive(String fname) {
		return !fname.endsWith(".tar.gz") && !fname.endsWith(".tgz") && !fname.endsWith(".zip")
				&& !fname.endsWith(".gz");
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

			executeImmediate(connexion, FormatSQL.setTimeOutMaintenance());

			GenericBean gb = new GenericBean(
					executeRequest(connexion, new GenericPreparedStatementBuilder("select tablename from pg_tables where schemaname='pg_catalog'")));
			StringBuilder requete = new StringBuilder();
			for (String t : gb.mapContent().get("tablename")) {
				requete.append(FormatSQL.analyzeSecured(t));
			}
			for (String t : gb.mapContent().get("tablename")) {
				requete.append(FormatSQL.vacuumSecured(t, type));
			}
			executeImmediate(connexion, requete.toString());
		} catch (Exception ex) {
			LoggerHelper.error(LOGGER, ex);
		} finally {
			try {
				executeImmediate(connexion, FormatSQL.resetTimeOutMaintenance());
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
	public Collection<String> getColumns(Connection connexion, Collection<String> liste, String tableIn) throws ArcException {
			liste.addAll(new GenericBean(get(this.pool).executeStatement(connexion, FormatSQL.listeColonneByHeaders(tableIn)))
					.getHeaders());
		return liste;
	}

	
	public List<String> getColumns(Connection connexion, String tableIn) throws ArcException
	{
		return new ArrayList<>(getColumns(connexion, new ArrayList<>(), tableIn));
	}

	/**
	 * Met à jour un table en mode bulk
	 * c'est à dire en recréant l'image modifiée de la table à mettre à jour et en droppant l'ancienne
	 * Equivalent à UPDATE table SET [@param set] WHERE [@param where]
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
	public static void fastUpdate(String poolName, Connection aConnexion, String tableName, String keys, List<String> colList, String where,
			String... set) throws ArcException {
		// récupérer la liste des colonnes
		// liste de toutes les colonnes
		// liste des colonnes à mettre à jour
		ArrayList<String> colSetList = new ArrayList<String>();
		ArrayList<String> setList = new ArrayList<String>();
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
		ArrayList<String> colKeyList = new ArrayList<String>();
		for (int i = 0; i < keys.split(",").length; i++) {
			colKeyList.add(keys.split(",")[i].trim().toUpperCase());
		}
		// construction de la requete
		StringBuilder requete = new StringBuilder();
		String tableFastUpdate = FormatSQL.temporaryTableName(tableName, "F");
		String tableImage = FormatSQL.temporaryTableName(tableName, "I");
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
		get(poolName).executeImmediate(aConnexion, requete);
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
	public void exporting(Connection connexion, String table, OutputStream os, boolean csv, boolean... forceQuote)
			throws ArcException {
		ConnectionWrapper conn = initConnection(connexion);

		boolean forceQuoteBis;
		if (forceQuote != null && forceQuote.length > 0) {
			forceQuoteBis = forceQuote[0];
		} else {
			forceQuoteBis = true;
		}

		try {
			CopyManager copyManager = new CopyManager((BaseConnection) conn.getConnexion());
			if (csv) {
				if (forceQuoteBis) {
					copyManager.copyOut("COPY " + table
							+ " TO STDOUT WITH (FORMAT csv, HEADER true , DELIMITER ';' , FORCE_QUOTE *, ENCODING 'UTF8') ",
							os);
				} else {
					copyManager.copyOut(
							"COPY " + table
									+ " TO STDOUT WITH (FORMAT csv, HEADER true , DELIMITER ';' , ENCODING 'UTF8') ",
							os);
				}
			} else {
				copyManager.copyOut("COPY " + table + " TO STDOUT WITH (FORMAT BINARY)", os);
			}
		} catch (SQLException e) {
			throw new ArcException(e);
		} catch (IOException e) {
			throw new ArcException(e);
		} finally {
			conn.close();
		}
	}

	/**
	 *
	 * @param connexion
	 * @param table
	 * @param os
	 * @param csv       : true / false (binary)
	 * @throws ArcException
	 * @throws IOException
	 */
	public void exportingWithoutHeader(Connection connexion, String table, OutputStream os, boolean csv,
			boolean... forceQuote) throws ArcException, IOException {
		ConnectionWrapper conn = initConnection(connexion);

		boolean forceQuoteBis;
		if (forceQuote != null && forceQuote.length > 0) {
			forceQuoteBis = forceQuote[0];
		} else {
			forceQuoteBis = true;
		}

		try {
			CopyManager copyManager = new CopyManager((BaseConnection) conn.getConnexion());
			if (csv) {
				if (forceQuoteBis) {
					copyManager.copyOut("COPY " + table
							+ " TO STDOUT WITH (FORMAT csv, HEADER false , DELIMITER ';' , FORCE_QUOTE *, ENCODING 'UTF8') ",
							os);
				} else {
					copyManager.copyOut(
							"COPY " + table
									+ " TO STDOUT WITH (FORMAT csv, HEADER false , DELIMITER ';' , ENCODING 'UTF8') ",
							os);
				}
			} else {
				copyManager.copyOut("COPY " + table + " TO STDOUT WITH (FORMAT BINARY)", os);
			}
		} catch (SQLException e) {
			throw new ArcException(e);
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
	public void importing(Connection connexion, String table, Reader aReader, boolean csv, boolean header,
			String... aDelim) throws ArcException {
		ConnectionWrapper conn = initConnection(connexion);
		try {
			conn.getConnexion().setAutoCommit(false);
			CopyManager copyManager = new CopyManager((BaseConnection) conn.getConnexion());
			String delimiter = "";
			String quote = Character.toString((char) 2);

			if (aDelim != null && aDelim.length > 0) {
				delimiter = ", DELIMITER '" + aDelim[0] + "', QUOTE '" + quote + "' ";
			}

			if (aDelim != null && aDelim.length > 1) {
				header = false;
			}

			String h = (header ? ", HEADER true " : "");

			if (csv) {
				copyManager.copyIn("COPY " + table + " FROM STDIN WITH (FORMAT CSV " + h + delimiter + ") ", aReader);
			} else {
				copyManager.copyIn("COPY " + table + " FROM STDIN WITH (FORMAT BINARY)", aReader);
			}
			conn.getConnexion().commit();
		} catch (SQLException | IOException e) {

			LoggerHelper.error(LOGGER, e);

			try {
				conn.getConnexion().rollback();
			} catch (SQLException e1) {
				throw new ArcException("Error in connection rollback",e1);
			}
			
			throw new ArcException(e);

		} finally {
			conn.close();
		}
	}

	/**
	 * Copie brutal de fichier plat dans une table SQL.
	 * 
	 * @param connexion
	 * @param table
	 * @param aColumnName
	 * @param is
	 * @param csv
	 * @param header
	 * @param aDelim
	 * @param aQuote
	 * @throws ArcException
	 */
	public void importing(Connection connexion, String table, String aColumnName, InputStream is, boolean csv,
			boolean header, String aDelim, String aQuote) throws ArcException {
		importing(connexion, table, aColumnName, is, csv ? FORMAT_CSV : FORMAT_BINARY, header, aDelim, aQuote);
	}

	public void importing(Connection connexion, String table, String aColumnName, InputStream is, boolean csv,
			boolean header, String aDelim, String aQuote, String encoding) throws ArcException {
		importing(connexion, table, aColumnName, is, csv ? FORMAT_CSV : FORMAT_BINARY, header, aDelim, aQuote,
				encoding);
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
	 * @throws ArcException
	 */
	public void importing(Connection connexion, String table, String aColumnName, InputStream is, String format,
			boolean header, String aDelim, String aQuote) throws ArcException {
		importing(connexion, table, aColumnName, is, format, header, aDelim, aQuote, null);
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
	public void importing(Connection connexion, String table, String aColumnName, InputStream is, String format,
			boolean header, String aDelim, String aQuote, String encoding) throws ArcException {
		LoggerHelper.info(LOGGER, "importing()");
		try (ConnectionWrapper conn = initConnection(connexion);)
		{
			conn.getConnexion().setAutoCommit(false);
			CopyManager copyManager = new CopyManager((BaseConnection) conn.getConnexion());
			String delimiter = "";
			String quote = "";
			String columnName = "";
			String encode = "UTF8";

			if (aDelim != null && aDelim.length() == 1) {
				delimiter = ", DELIMITER '" + aDelim + "'";
			}

			if (aQuote != null && aQuote.length() == 1) {
				quote = ", QUOTE '" + aQuote + "'";
			}

			if (aColumnName != null && aColumnName != "") {
				columnName = aColumnName;
			}

			if (encoding != null) {
				encode = encoding;
			}

			String h = (header ? ", HEADER true " : "");
			if (format.equals(FORMAT_CSV) || format.equals(FORMAT_TEXT)) {
				copyManager.copyIn("COPY " + table + columnName + " FROM STDIN WITH (FORMAT " + format + ", ENCODING '"
						+ encode + "' " + h + delimiter + quote + ") ", is);
			}

			if (format.equals(FORMAT_BINARY)) {
				copyManager.copyIn("COPY " + table + " FROM STDIN WITH (FORMAT BINARY)", is);
			}

			conn.getConnexion().commit();

			LoggerHelper.info(LOGGER, "importing done");
		} catch (IOException e) {

			LoggerHelper.error(LOGGER, e);

			if (e.getMessage().startsWith("ERROR: missing data for column")) {

				throw new ArcException("Il manque une/des colonne dans le corps du fichier",e);

			} else if (e.getMessage().startsWith("ERROR: extra data after last expected column")) {

				throw new ArcException("Il manque un/des headers",e);

			} else {
				throw new ArcException(e);
			}

		} catch (SQLException e) {
			throw new ArcException(e);
		}
	}

	public void importing(Connection connexion, String table, InputStream is, boolean csv, String aDelim)
			throws ArcException {
		importing(connexion, table, null, is, csv, true, aDelim, null);
	}

	public void importing(Connection connexion, String table, InputStream is, boolean csv) throws ArcException {
		importing(connexion, table, null, is, csv, true, null, null);
	}

	public void importing(Connection connexion, String table, String aColumnName, InputStream is, boolean csv)
			throws ArcException {
		importing(connexion, table, aColumnName, is, csv, true, null, null);
	}

	public void importing(Connection connexion, String table, InputStream is, boolean csv, boolean aHeader,
			String aDelim) throws ArcException {
		importing(connexion, table, null, is, csv, aHeader, aDelim, null);
	}

	public void importing(Connection connexion, String table, String aColumnName, InputStream is, boolean csv,
			boolean aHeader, String aDelim) throws ArcException {
		importing(connexion, table, aColumnName, is, csv, aHeader, aDelim, null);

	}

	public boolean isSilent() {
		return this.silent;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

}
