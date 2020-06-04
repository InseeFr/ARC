package fr.insee.arc.utils.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLClientInfoException;
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
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.stereotype.Component;

import fr.insee.arc.utils.files.FileUtils;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.textUtils.IConstanteNumerique;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.utils.utils.Pair;
import fr.insee.arc.utils.utils.SQLExecutor;

/**
 *
 * Split this -> ddl, dml, single vs multiple results
 *
 */
@Component
public class UtilitaireDao implements IConstanteNumerique, IConstanteCaractere {

	private static final Logger LOGGER = Logger.getLogger(UtilitaireDao.class);

    
    
	private int nbTryMax = 120;
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

	private String pool;
	private static Map<String, UtilitaireDao> map;
	private boolean silent = false;

	private static String FILE_THAT_CONTAINS_CONFIG_PARAMETER = "file:///";

	private UtilitaireDao(String aPool) {
		this.pool = aPool;
	}

	public static final UtilitaireDao get(String aPool) {
		if (map == null) {
			map = new HashMap<String, UtilitaireDao>();
		}
		if (!map.containsKey(aPool)) {
			map.put(aPool, new UtilitaireDao(aPool));
		}
		return map.get(aPool);
	}

	public static final UtilitaireDao get(String aPool, int nbTry) {
		get(aPool).nbTryMax = nbTry;
		return get(aPool);
	}

	public static final String getMethode(String methode) {
		return UtilitaireDao.class.getCanonicalName() + " " + methode;
	}

	private final static String untokenize(ModeRequete... modes) {
		StringBuilder returned = new StringBuilder();
		for (int i = 0; i < modes.length; i++) {
			returned.append(modes[i].expr()).append("\n");
		}
		return returned.toString();
	}

	public boolean dropSomeSchema(Connection connexion, Collection<String> listeSchemaConserver) {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"\n SELECT string_agg('DROP SCHEMA IF EXISTS '||schema_name||' CASCADE;', '' ORDER BY schema_name)::text");
		sb.append("\n FROM information_schema.schemata \n");
		sb.append("\n WHERE schema_owner IN ");
		sb.append("\n (SELECT role_name FROM information_schema.enabled_roles)");
		if (listeSchemaConserver != null && listeSchemaConserver.size() > 0) {
			sb.append("\n AND schema_name NOT IN " + Format.sqlListe(listeSchemaConserver));
		}
		try {
			this.executeImmediate(connexion, sb);
		} catch (SQLException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "dropSomeSchema()", LOGGER, ex);
			return false;
		}
		return true;
	}

	public boolean prepareTablePartition(Connection aConnexion, String aTable, String aTableTempName, String aPartition,
			List<String> listColonnes) {
		try {
			List<String> listePartition = ManipString.splitAndCleanList(aPartition, ",");
			String expressionPartition = "abs(hashtext("//
					+ listePartition.stream()//
							.map(t -> "coalesce(" + t + ",'')")//
							.collect(Collectors.joining("||"))//
					+ "))";//
			String idHashName = "id_hash";
			int idHashModulo = 1000000000;
			String idRowName = "id_row";
			StringBuilder sb = new StringBuilder();
			sb.append("\n CREATE TABLE " + aTableTempName + " ");
			sb.append("\n AS  ");
			sb.append("\n WITH tmp_count AS ( ");
			sb.append("\n 		SELECT (count(*)*10000) + 1 AS n  ");
			sb.append("\n 		FROM " + aTable + " tablesample system(0.01) ");
			sb.append("\n 	), nb_partition AS ( ");
			sb.append("\n 	SELECT CASE 	WHEN log(n) <= 5 THEN 2 ");
			sb.append("\n 					WHEN log(n) <= 6 THEN 8 ");
			sb.append("\n 					WHEN log(n) <= 7 THEN 64 ");
			sb.append("\n 					ELSE 1024 ");
			sb.append("\n 			END AS n ");
			sb.append("\n 	FROM tmp_count ");
			sb.append("\n 	)  ");
			sb.append("\n SELECT " + listColonnes.stream()
					.filter(t -> !t.equalsIgnoreCase(idRowName) && !t.equalsIgnoreCase(idHashName)).map(t -> "foo." + t)
					.collect(Collectors.joining(",")));
			sb.append(", foo." + idHashName);
			if (!listColonnes.contains(idRowName)) {
				sb.append("\n , " + idHashName + "::bigint" + "*" + idHashModulo + "::bigint"
						+ " + ROW_NUMBER() OVER() AS " + idRowName);
			} else {
				sb.append("\n , CASE WHEN " + idRowName + "::bigint<" + idHashModulo + "::bigint");
				sb.append("\n 			THEN " + idRowName + "::bigint+" + idHashModulo + "::bigint" + "*" + idHashName
						+ "::bigint");
				sb.append("\n 			ELSE " + idRowName + "::bigint");
				sb.append("\n 			END AS " + idRowName);
			}
			sb.append("\n FROM ( ");

			sb.append("\n 		SELECT " + listColonnes.stream().filter(t -> !t.equalsIgnoreCase(idHashName))
					.map(t -> "tab." + t).collect(Collectors.joining(",")));
			sb.append("\n 		, " + expressionPartition + "% nb_partition.n + 1 AS " + idHashName);
			sb.append("\n 		FROM " + aTable + " tab, nb_partition ");

			sb.append("\n ) foo ");
			sb.append("\n ; ");
			sb.append(FormatSQL.dropUniqueTable(aTable));
			sb.append(FormatSQL.renameTableTo(aTableTempName, aTable));
			this.executeImmediate(aConnexion, sb);
		} catch (Exception e) {
			LoggerHelper.error(LOGGER, e, "Erreur lors de l'ajout du id_hash");
			return false;
		}
		return true;
	}

	public boolean createSchema(Connection aConnexion, String schema, String anAuthorization, String... someGrantAlls) {
		StringBuilder sb = new StringBuilder("BEGIN;");
		sb.append("CREATE SCHEMA " + schema + " AUTHORIZATION " + anAuthorization + ";");
		for (int i = 0; i < someGrantAlls.length; i++) {
			sb.append("GRANT ALL ON SCHEMA " + schema + " TO " + someGrantAlls[i] + ";");
		}
		sb.append("END;");
		try {
			this.executeImmediate(aConnexion, sb);
		} catch (SQLException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "createSchema()", LOGGER, ex);
			return false;
		}
		return true;
	}

	/**
	 * Retourne une connexion hors contexte (sans pooling)
	 *
	 * @return la connexion
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public final Connection getDriverConnexion() throws Exception {
		// invocation du driver
		PropertiesHandler properties = PropertiesHandler.getInstance();
		Class.forName(
		properties.getDatabaseDriverClassName());
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
				int sleep = 60000;
				LoggerHelper.error(LOGGER,
						"Connection failure. Tentative de reconnexion dans " + (sleep / 1000) + " secondes", nbTry);
				Thread.sleep(sleep);
			}
			nbTry++;
		}
		if (!connectionOk) {
			throw new Exception("la connexion a échouée");
		}
		return c;
	}

	/**
	 *
	 * @param connexion
	 * @return une nouvelle connexion non poolée si connexion isnull, ou la
	 *         connexion en entrée
	 */
	public final ConnectionWrapper initConnection(Connection connexion) {
		try {
			Boolean isNull = (connexion == null);

			return new ConnectionWrapper(isNull, isNull ? getDriverConnexion() : connexion);
		} catch (Exception e) {
			LoggerHelper.errorGenTextAsComment(getClass(), "initConnection()", LOGGER, e);
		}
		return null;
	}

	/**
	 * Vérifier qu'une table existe <br/>
	 *
	 */
	public Boolean isTableExiste(Connection connexion, String table) {
		Boolean b = null;
		try {
			b = getBoolean(connexion, FormatSQL.isTableExists(table).toString(), table);
		} catch (Exception e) {
		}
		return b;
	}

	/**
	 * <br/>
	 *
	 *
	 * @param connexion la connexion à la base
	 * @param table     le nom de la table
	 * @return
	 */
	public void dropUniqueTable(Connection connexion, String table) {
		try {
			executeRequest(connexion, "DROP TABLE IF EXISTS " + table + ";");
		} catch (SQLException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "dropUniqueTable()", LOGGER, ex);
		}
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
		} catch (SQLException ex) {
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
	 * <br/>
	 *
	 *
	 * @param connexion
	 * @param table
	 */
	public void truncateTable(Connection connexion, String table) {
		try {
			executeRequest(connexion, "TRUNCATE TABLE " + table);
		} catch (SQLException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "truncateTable()", LOGGER, ex);
		}
	}

	public void grantToRole(String droit, String role) {
		try {
			GenericBean gb = new GenericBean(executeRequest(null,
					"select 'do $$ begin GRANT " + droit + " ON TABLE '||schemaname||'.'||tablename||' TO " + role
							+ "; exception when others then end; $$;' as query from pg_tables "));
			ArrayList<String> allReq = gb.mapContent().get("query");
			StringBuilder requete = new StringBuilder();
			for (String req : allReq) {
				requete.append(req + "\n");
				if (requete.length() > FormatSQL.TAILLE_MAXIMAL_BLOC_SQL) {
					executeRequest(null, requete);
					requete.setLength(0);
				}
			}
			executeRequest(null, requete);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <br/>
	 *
	 *
	 * @param connexion la connexion à la base
	 * @param seq       le nom de la séquence
	 */
	public void createSequence(Connection connexion, String seq) {
		try {
			executeRequest(connexion, "CREATE SEQUENCE " + seq);
		} catch (SQLException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "createSequence()", LOGGER, ex);
		}
	}

	/**
	 * Exécute une requête {@code sql} avec des arguments {@code args}, renvoie le
	 * booléen (unique) que cette requête est censée rendre<br/>
	 *
	 *
	 * @param sql
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Boolean getBoolean(Connection aConnexion, StringBuilder sql, String... args) throws Exception {
		return getBoolean(aConnexion, sql.toString(), args);
	}

	/**
	 * Exécute une requête {@code sql} avec des arguments {@code args}, renvoie le
	 * booléen (unique) que cette requête est censée rendre<br/>
	 *
	 *
	 * @param sql
	 * @param args
	 * @return
	 */
	public Boolean getBoolean(Connection connexion, String sql, String... args) throws Exception {
		List<String> constructorMethodsAndClasses = findSQLQueryContructor();
		String updatedQuery = FormatSQL.addCommentaryHeaderToQuery(sql, constructorMethodsAndClasses);

		try {
			ConnectionWrapper connexionWrapper = initConnection(connexion);
			try {
				Statement stmt = connexionWrapper.getConnexion().createStatement();
				try {
					stmt.execute(ModeRequete.EXTRA_FLOAT_DIGIT.expr());
					String requete = Format.parseStringAvecArguments(updatedQuery, args);
					LoggerHelper.trace(LOGGER, requete);
					ResultSet rset = stmt.executeQuery(requete);
					if (rset.next()) {
						return rset.getBoolean(FIRST_COLUMN_INDEX);
					}
				} finally {
					stmt.close();
				}
			} finally {
				connexionWrapper.close();
			}
		} catch (Exception e) {
			LoggerHelper.errorAsComment(LOGGER, e, sql, Arrays.asList(args));
			throw e;
		}
		return null;
	}

	/**
	 * Exécute une requête qui renvoie exactement un argument de type {@link Long}.
	 *
	 * @param connexion la connexion à la base
	 * @param sql       la requête
	 * @param args      les arguments de la requête (optionnels)
	 * @return
	 */
	public Long getLong(Connection connexion, String sql, String... args) {
		String requete = Format.parseStringAvecArguments(sql, args);
		LoggerHelper.trace(LOGGER, requete);
		List<String> constructorMethodsAndClasses = findSQLQueryContructor();
		String updatedQuery = FormatSQL.addCommentaryHeaderToQuery(requete, constructorMethodsAndClasses);
		try {
			ConnectionWrapper connexionWrapper = initConnection(connexion);
			Statement stmt = connexionWrapper.getConnexion().createStatement();
			try {
				stmt.execute(ModeRequete.EXTRA_FLOAT_DIGIT.expr());
				ResultSet rset = stmt.executeQuery(updatedQuery);
				if (rset.next()) {
					return rset.getLong(FIRST_COLUMN_INDEX);
				}
			} finally {
				stmt.close();
				connexionWrapper.close();
			}
		} catch (Exception ex) {
			LoggerHelper.error(LOGGER, "Erreur non-bloquante gérée par la méthode", ex.getMessage(), ":", requete);
		}
		return Long.MIN_VALUE;
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
	 * @throws SQLException
	 */
	public String getString(Connection connexion, String requete, String... args) throws SQLException {
		try {
			ConnectionWrapper connexionWrapper = initConnection(connexion);
			Statement stmt = connexionWrapper.getConnexion().createStatement();

			List<String> constructorMethodsAndClasses = findSQLQueryContructor();
			String updatedQuery = FormatSQL.addCommentaryHeaderToQuery(requete, constructorMethodsAndClasses);

			try {
				stmt.execute(ModeRequete.EXTRA_FLOAT_DIGIT.expr());
				ResultSet rset = stmt.executeQuery(Format.parseStringAvecArguments(updatedQuery, args));
				if (rset.next()) {
					return rset.getString(FIRST_COLUMN_INDEX);
				}
			} finally {
				stmt.close();
				connexionWrapper.close();
			}
		} catch (SQLException e) {
			throw e;
		}
		return null;
	}

	/**
	 * Exécute une requête qui renvoie exactement un argument de type
	 * {@link String}.
	 *
	 * @param connexion la connexion à la base
	 * @param sql       la requête
	 * @param args      les arguments de la requête (optionnels)
	 * @return
	 * @throws SQLException
	 */
	public String getString(Connection connexion, StringBuilder sql, String... args) throws SQLException {
		return getString(connexion, sql.toString(), args);
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
	public int getInt(Connection connexion, StringBuilder sql, ModeRequete... modes) {
		return getInt(connexion, sql.toString(), modes);
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
	public int getInt(Connection connexion, String sql, ModeRequete... modes) {
		try {
			String returned = executeRequest(connexion, sql, modes).get(2).get(0);
			return (returned == null ? ZERO : Integer.parseInt(returned));
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
		return getInt(connexion, new StringBuilder("select max(" + column + ") max_value from " + table));
	}

	public boolean isColonneExiste(Connection aConnexion, String aNomTable, String aNomVariable) {
		return getColumns(aConnexion, new HashSet<String>(), aNomTable).contains(aNomVariable);
	}

	/**
	 * Récupère les colonnes de la table {@code tableName}
	 *
	 * @param connexion la connexion à la base
	 * @param liste     la liste des colonnes retournée
	 * @param tableName le nom de la table
	 * @return
	 */
	public Collection<String> getColumns(Connection connexion, Collection<String> liste, String tableName) {
		String token = (!tableName.contains(DOT)) ? "" : "pg_namespace.nspname||'.'||";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT attname");
		sql.append("\n  FROM pg_namespace");
		sql.append("\n  INNER JOIN pg_class");
		sql.append("\n    ON pg_class.relnamespace = pg_namespace.oid");
		sql.append("\n  INNER JOIN pg_attribute ");
		sql.append("\n    ON pg_class.oid          = pg_attribute.attrelid ");
		sql.append("\n  WHERE lower(" + token + "pg_class.relname)=lower('" + tableName + "')");
		sql.append("\n    AND attnum>0");// .append(" ORDER BY attname"));
		sql.append("\n    AND attisdropped=false");
		sql.append(";");
		List<String> constructorMethodsAndClasses = findSQLQueryContructor();

		String updatedQuery = FormatSQL.addCommentaryHeaderToQuery(sql.toString(), constructorMethodsAndClasses);
		LoggerHelper.trace(LOGGER, updatedQuery);
		try {
			ConnectionWrapper connexionWrapper = initConnection(connexion);
			try {
				Statement stmt = connexionWrapper.getConnexion().createStatement();
				try {
					stmt.execute(ModeRequete.EXTRA_FLOAT_DIGIT.expr());
					ResultSet result = stmt.executeQuery(updatedQuery.toString());
					while (result.next()) {
						liste.add(result.getString("attname"));
					}
				} finally {
					stmt.close();
				}
			} finally {
				connexionWrapper.close();
			}
		} catch (Exception ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "getColumns()", LOGGER, ex);
		}
		return liste;
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
	public long getCount(Connection connexion, String aTable, String clauseWhere) {
		return getLong(connexion, "SELECT count(1) FROM " + aTable
				+ (StringUtils.isBlank(clauseWhere) ? empty : (" WHERE " + clauseWhere)));
	}

	public Boolean testResultRequest(Connection connexion, StringBuilder requete) throws SQLException {
		return testResultRequest(connexion, requete.toString());
	}

	/**
	 * Exécution de requêtes ramenant des enregistrements <br/>
	 *
	 *
	 * @param connexion
	 * @param requete
	 * @return
	 * @throws SQLException
	 */
	public ArrayList<ArrayList<String>> executeRequest(Connection connexion, StringBuilder requete,
			ModeRequete... modes) throws SQLException {
		return executeRequest(connexion, requete.toString(), modes);
	}

	public ArrayList<ArrayList<String>> executeRequestWithoutMetadata(Connection connexion, StringBuilder requete,
			ModeRequete... modes) throws SQLException {
		return executeRequestWithoutMetadata(connexion, requete.toString(), modes);
	}

	/**
	 * <br/>
	 *
	 *
	 * @param connexion
	 * @param requete
	 * @param modes
	 * @return
	 * @throws SQLException
	 */
	public ArrayList<ArrayList<String>> executeRequestWithoutMetadata(Connection connexion, String requete,
			ModeRequete... modes) throws SQLException {
		ArrayList<ArrayList<String>> returned = executeRequest(connexion, requete, modes);
		returned.remove(0);
		returned.remove(0);
		return returned;
	}

	public void executeImmediate(Connection connexion, StringBuilder requete, ModeRequete... modes)
			throws SQLException {
		executeImmediate(connexion, requete.toString(), modes);
	}

	public String insertAndGetLastInserted(Connection connexion, String requete, String column, ModeRequete... modes)
			throws SQLException {
		List<String> constructorMethodsAndClasses = findSQLQueryContructor();

		String updatedQuery = FormatSQL.addCommentaryHeaderToQuery(requete, constructorMethodsAndClasses);
		LoggerHelper.trace(LOGGER, updatedQuery);
		ConnectionWrapper connexionWrapper = initConnection(connexion);
		try {
			connexionWrapper.getConnexion().setAutoCommit(true);
			Statement st = connexionWrapper.getConnexion().createStatement();
			try {
				st.execute(ModeRequete.EXTRA_FLOAT_DIGIT.expr());
				st.execute(untokenize(modes) + updatedQuery, Statement.RETURN_GENERATED_KEYS);
				ResultSet rs = st.getGeneratedKeys();
				if (rs.next()) {
					return String.valueOf(rs.getLong(FIRST_COLUMN_INDEX));
				}
			} finally {
				st.close();
			}
		} finally {
			if (connexionWrapper.isLocal()) {
				connexionWrapper.close();
			}
		}
		throw new SQLException("La requête n'était pas une insertion ou la récupération de la ligne insérée a échoué");
	}

	public void executeImmediate(Connection connexion, String requete, ModeRequete... modes) throws SQLException {
		List<String> constructorMethodsAndClasses = findSQLQueryContructor();

		String updatedQuery = FormatSQL.addCommentaryHeaderToQuery(requete, constructorMethodsAndClasses);

		long start = new Date().getTime();
		LoggerHelper.trace(LOGGER, updatedQuery.trim());

		ConnectionWrapper connexionWrapper = initConnection(connexion);
		try {
			connexionWrapper.getConnexion().setAutoCommit(true);
			Statement st = connexionWrapper.getConnexion().createStatement();
			try {
				st.execute(untokenize(modes) + ModeRequete.EXTRA_FLOAT_DIGIT.expr() + updatedQuery);
				LoggerHelper.traceAsComment(LOGGER, "DUREE : ", (new Date().getTime() - start) + "ms");
			} catch (Exception e) {
				st.cancel();
				LoggerHelper.error(LOGGER, "executeRequest()", e);
				throw e;
			} finally {
				st.close();
			}
		} catch (Exception e) {
			LoggerHelper.error(LOGGER, "executeRequest()", e);
			throw e;
		} finally {
			if (connexionWrapper.isLocal()) {
				connexionWrapper.close();
			}
		}
	}

	/**
	 * Execute les stat sur la table et renvoi dans l'ordre les colonnes les plus
	 * discriminantes
	 * 
	 * @param connexion
	 * @param tableName
	 * @return
	 */
	public ArrayList<String> statsAndMostDiscriminantColumn(Connection connexion, String tableName) {
		try {
			executeImmediate(connexion, "ANALYZE " + tableName + ";");
			GenericBean gb = new GenericBean(executeRequest(connexion,
					"SELECT attname as col FROM PG_STATS " + " WHERE tablename='"
							+ ManipString.substringAfterFirst(tableName, ".") + "' " + " AND schemaname='"
							+ ManipString.substringBeforeFirst(tableName, ".") + "' "
							+ " order by n_distinct>=0, -abs(n_distinct) "));
			return gb.mapContent().get("col");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * <br/>
	 *
	 *
	 * @param aConnexion
	 * @param string
	 * @param regex
	 * @return
	 */
	public Boolean checkAgainstRegex(Connection aConnexion, String string, String regex) {
		Boolean b = null;
		try {
			b = getBoolean(aConnexion, "select '"//
					+ string.replace(quote, quotequote) //
					// + "' similar to '" //
					+ "' ~ '" //
					+ regex.replace(quote, quotequote) //
					+ "'");
		} catch (Exception e) {
		}
		return b;
	}

	/**
	 * Permet de savoir si une requete renvoie des résultats ou non
	 *
	 * @param connexion
	 * @param requete
	 * @return
	 * @throws SQLException
	 */
	public Boolean testResultRequest(Connection connexion, String requete) {
		Boolean isresult = false;
		LoggerHelper.trace(LOGGER, requete);
		List<String> constructorMethodsAndClasses = findSQLQueryContructor();

		String updatedQuery = FormatSQL.addCommentaryHeaderToQuery(requete, constructorMethodsAndClasses);
		try {
			ConnectionWrapper connexionWrapper = initConnection(connexion);
			try {
				connexionWrapper.getConnexion().setAutoCommit(false);
				PreparedStatement stmt;
				stmt = connexionWrapper.getConnexion().prepareStatement("set extra_float_digits=3;");
				stmt.execute();
				stmt = connexionWrapper.getConnexion()
						.prepareStatement("SELECT * from (" + updatedQuery + ") dummyTable0000000 LIMIT 1 ");
				try {
					isresult = stmt.execute();
					return isresult;
				} finally {
					stmt.close();
					connexionWrapper.getConnexion().commit();
				}
			} catch (Exception e) {
				connexionWrapper.getConnexion().rollback();
				return isresult;
			} finally {
				connexionWrapper.close();
			}
		} catch (SQLException e) {
			LoggerHelper.errorGenTextAsComment(getClass(), "testResultRequest()", LOGGER, e);
			return isresult;
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
	 * @throws SQLException
	 * @throws PoolException
	 *
	 *
	 */
	public ArrayList<ArrayList<String>> executeRequest(Connection connexion, String requete, ModeRequete... modes)
			throws SQLException {

		return executeRequest(connexion, requete, EntityProvider.getArrayOfArrayProvider(), modes);

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
	 * @throws SQLException
	 * @throws PoolException
	 *
	 *
	 */
	public <T> List<T> executeRequest(Connection connexion, String requete, Function<ResultSet, T> orm,
			ModeRequete... modes) throws SQLException {
		return executeRequest(connexion, requete, EntityProvider.getTypedListProvider(orm), modes);
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
	 * @throws SQLException
	 * @throws PoolException
	 *
	 *
	 */
	public <T> T executeRequest(Connection connexion, String requete, EntityProvider<T> entityProvider,
			ModeRequete... modes) throws SQLException {
		if (modes != null && modes.length > 0) {
			LoggerHelper.trace(LOGGER, "\n" + untokenize(modes));
		}

		long start = new Date().getTime();
		List<String> constructorMethodsAndClasses = findSQLQueryContructor();
		String updatedQuery = FormatSQL.addCommentaryHeaderToQuery(requete, constructorMethodsAndClasses);
		LoggerHelper.trace(LOGGER, updatedQuery);

		try {
			ConnectionWrapper connexionWrapper = initConnection(connexion);
			try {
				connexionWrapper.getConnexion().setAutoCommit(false);
				PreparedStatement stmt;
				if (modes != null && modes.length > 0) {
					stmt = connexionWrapper.getConnexion()
							.prepareStatement(untokenize(modes) + ModeRequete.EXTRA_FLOAT_DIGIT.expr());
					stmt.execute();
				} else {
					stmt = connexionWrapper.getConnexion().prepareStatement(ModeRequete.EXTRA_FLOAT_DIGIT.expr());
					stmt.execute();
				}
				stmt = connexionWrapper.getConnexion().prepareStatement(updatedQuery);
				LoggerHelper.traceAsComment(LOGGER, "DUREE : ", (new Date().getTime() - start) + "ms");

				try {
					Boolean isresult = stmt.execute();
					if (isresult) {
						ResultSet res = stmt.getResultSet();
						return entityProvider.apply(res);
					}
					return null;
				} catch (Exception e) {
					if (!this.silent) {
						LoggerHelper.error(LOGGER, stmt.toString());
					}
					throw e;
				} finally {
					stmt.close();
					connexionWrapper.getConnexion().commit();
				}
			} catch (Exception e) {
				if (!this.silent) {
					LoggerHelper.error(LOGGER, "executeRequest()", e);
				}
				connexionWrapper.getConnexion().rollback();
				throw e;
			} finally {
				connexionWrapper.close();
			}
		} catch (Exception ex) {
			if (!this.silent) {
				LoggerHelper.error(LOGGER, "Lors de l'exécution de", updatedQuery);
			}
			throw ex;
		}
	}

	/**
	 * Search in the stackTrace methode with the {@link SQLExecutor} annotation
	 * 
	 * @return a list of string with the {@link Class} and {@link Method} of all
	 *         method with the {@link SQLExecutor} in the statcktrace
	 */
	private List<String> findSQLQueryContructor() {
		Set<Pair<Class<?>, Method>> methodsWithTheAnnotation = new HashSet<>();
		StackTraceElement[] stacktraces = Thread.currentThread().getStackTrace();
		for (StackTraceElement stackTraceElement : stacktraces) {
			Class<?> aClass;
			try {
				aClass = Class.forName(stackTraceElement.getClassName());
				Method[] methods = aClass.getMethods();
				String methodName = stackTraceElement.getMethodName();
				for (Method method : methods) {
					// Can't directly get the methiod, only it's name so have to check the name with
					// the methode in the stacktrace :(
					if (method.getName().equals(methodName)) {
						if (method.getAnnotation(SQLExecutor.class) != null) {
							methodsWithTheAnnotation.add(new Pair<Class<?>, Method>(aClass, method));
						}
					}
				}
			} catch (ClassNotFoundException e) {
				LoggerHelper.error(LOGGER, "Error when searching for annotation to better log");
			}

		}

		return methodsWithTheAnnotation.stream()
				.map(pair -> pair.getFirst().getSimpleName() + " " + pair.getSecond().getName())
				.collect(Collectors.toList());
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
			public ArrayOfArrayProvider() {
			}

			@Override
			public ArrayList<ArrayList<String>> apply(ResultSet res) {
				try {
					return fromResultSetToArray(res);
				} catch (SQLException ex) {
					throw new RuntimeException(ex);
				}
			}
		}

		public static final EntityProvider<ArrayList<ArrayList<String>>> getArrayOfArrayProvider() {
			return new ArrayOfArrayProvider();
		}

		private static final class GenericBeanProvider extends EntityProvider<GenericBean> {

			public GenericBeanProvider() {
			}

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
				} catch (SQLException ex) {
					throw new RuntimeException(ex);
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
		} catch (SQLException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public static ArrayList<ArrayList<String>> fromResultSetToArray(ResultSet res) throws SQLException {
		return fromResultSetToList(() -> new ArrayList<>(), new ArrayList<>(), res);
	}

	public static <T extends List<String>, U extends List<T>> U fromResultSetToList(Supplier<T> newList, U result,
			ResultSet res) throws SQLException {
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

	public static <T, U extends List<T>> U fromResultSetToListOfT(Supplier<U> newList, Function<ResultSet, T> orm,
			ResultSet res) throws SQLException {
		U result = newList.get();
		while (res.next()) {
			result.add(orm.apply(res));
		}
		return result;
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

	public Boolean hasResults(Connection connexion, String requete) throws Exception {
		try {
			return hasResults(executeRequest(connexion, requete));
		} catch (Exception ex) {
			throw ex;
		}
	}

	public Boolean hasResults(Connection connexion, StringBuilder requete) throws Exception {
		return hasResults(connexion, requete.toString());
	}

	/**
	 * Ecrit le résultat de la requête {@code requete} dans le fichier compressé
	 * {@code out}
	 *
	 * @param connexion
	 * @param requete
	 * @param out
	 * @throws SQLException
	 */
	public void outStreamRequeteSelect(Connection connexion, String requete, OutputStream out) throws SQLException {
		StringBuilder str = new StringBuilder();
		// String lineSeparator = System.getProperty("line.separator");
		String lineSeparator = "\n";
		StringBuilder requeteLimit = new StringBuilder();
		int k = 0;
		int fetchSize = 5000;
		boolean endLoop = false;
		try {
			ConnectionWrapper connexionWrapper = initConnection(connexion);
			try {
				while (!endLoop) {
					try {
						requeteLimit.setLength(0);
						requeteLimit.append(requete);
						requeteLimit.append(" offset " + (k * fetchSize) + " limit " + fetchSize + " ");
						PreparedStatement stmt = connexionWrapper.getConnexion()
								.prepareStatement(requeteLimit.toString());
						try {
							ResultSet res = stmt.executeQuery();
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
//                                        str.append("null");
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
						} finally {
							stmt.close();
						}
					} catch (Exception e) {
						throw e;
					}
				}
			} finally {
				connexionWrapper.close();
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
	public static void generateTarGzFromFile(File fileIn, File fileOut, String entryName) throws IOException {

		FileInputStream fis = new FileInputStream(fileIn);
		TarArchiveOutputStream taos = new TarArchiveOutputStream(new GZIPOutputStream(new FileOutputStream(fileOut)));
		TarArchiveEntry entry = new TarArchiveEntry(entryName);
		entry.setSize(fileIn.length());
		taos.putArchiveEntry(entry);
		copy(fis, taos);
		taos.closeArchiveEntry();
		taos.close();
		fis.close();
	}

	/**
	 * Verifie que l'archive zip existe, lit les fichiers de la listIdSource et les
	 * copie dans un TarArchiveOutputStream
	 *
	 * @param repertoire
	 * @param phase
	 * @param etat
	 * @param currentContainer
	 * @param listIdSourceContainer
	 */
	public static void generateEntryFromFile(String receptionDirectoryRoot, String idSource,
			TarArchiveOutputStream taos) {
		File fileIn = new File(receptionDirectoryRoot + "/" + idSource);
		if (fileIn.exists()) {
			try {
				TarArchiveEntry entry = new TarArchiveEntry(fileIn.getName());
				entry.setSize(fileIn.length());
				taos.putArchiveEntry(entry);
				// Ecriture dans le fichier
				copy(new FileInputStream(fileIn), taos);
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
	 * @param repertoire
	 * @param phase
	 * @param etat
	 * @param currentContainer
	 * @param listIdSourceContainer
	 */
	public static void generateEntryFromZip(String receptionDirectoryRoot, String currentContainer,
			ArrayList<String> listIdSourceContainer, TarArchiveOutputStream taos) {
		File fileIn = new File(receptionDirectoryRoot + "/" + currentContainer);
		if (fileIn.exists()) {
			try {
				ZipInputStream tarInput = new ZipInputStream(new FileInputStream(fileIn));
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
				tarInput.close();
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
		File fileIn = new File(receptionDirectoryRoot + "/" + currentContainer);
		LoggerHelper.traceAsComment(LOGGER, "#generateEntryFromTarGz()", receptionDirectoryRoot, "/", currentContainer);
		if (fileIn.exists()) {
			// on crée le stream pour lire à l'interieur de
			// l'archive
			try {
				TarInputStream tarInput = new TarInputStream(new GZIPInputStream(new FileInputStream(fileIn)));
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
				tarInput.close();
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
				GZIPInputStream tarInput = new GZIPInputStream(new FileInputStream(fileIn));
				long size = 0;
				// on recupere d'abord la taille du stream; gzip ne permet pas
				// de le faire directement
				for (int c = tarInput.read(); c != -1; c = tarInput.read()) {
					size++;
				}
				tarInput.close();
				TarArchiveEntry entry = new TarArchiveEntry(listIdSourceContainer.get(0));
				entry.setSize(size);
				taos.putArchiveEntry(entry);
				tarInput = new GZIPInputStream(new FileInputStream(fileIn));
				for (int c = tarInput.read(); c != -1; c = tarInput.read()) {
					taos.write(c);
				}
				taos.closeArchiveEntry();
				tarInput.close();
			} catch (IOException ex) {
				LoggerHelper.errorGenTextAsComment(UtilitaireDao.class, "generateEntryFromGz()", LOGGER, ex);
			}
		}
	}

	private static final int BUFFER_SIZE = 1024;

	/**
	 *
	 * FIXME : input n'est pas fermé ici car passé en paramètre.<br/>
	 * copy input to output stream - available in several StreamUtils or Streams
	 * classes
	 *
	 * @param input
	 * @param output
	 * @throws IOException
	 */
	public static void copy(InputStream input, OutputStream output) throws IOException {
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int n = 0;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
			}
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ioe) {
					LoggerHelper.errorAsComment(LOGGER, ioe, "Lors de la clôture de InputStream");
				}
			}
		}
	}

	/**
	 * Les fichiers à copier sont potentiellement dans des dossiers différents
	 * (****_OK ou *****_KO) <br/>
	 *
	 *
	 * @param connexion
	 * @param requete          , contient la liste des fichiers
	 * @param taos             , réceptacle des fichiers
	 * @param path             , chemin jusqu'à l'avant dernier dossier
	 * @param listRepertoireIn , noms du dernier dossier qui diffère d'un cas à
	 *                         l'autre
	 */
	public void copieFichiers(Connection connexion, String requete, TarArchiveOutputStream taos, String path,
			List<String> listRepertoireIn) {
		LoggerHelper.debugDebutMethodeAsComment(getClass(), "copieFichiers()", LOGGER);
		GenericBean g;
		ArrayList<String> listFichier = new ArrayList<>();
		File fileIn = null;
		boolean find;
		String receptionDirectoryRoot = "";
		try {
			g = new GenericBean(this.executeRequest(null, requete.toString()));
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
					receptionDirectoryRoot = path + File.separator + listRepertoireIn.get(j);
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
					copy(new FileInputStream(fileIn), taos);
					taos.closeArchiveEntry();
				}
			}
		} catch (SQLException | IOException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "copieFichiers()", LOGGER, ex);
		}
		LoggerHelper.debugFinMethodeAsComment(getClass(), "copieFichiers()", LOGGER);
	}

	/**
	 * exécute un bloque transactionnel
	 */
	public void executeBlock(Connection connexion, String... listeRequete) throws SQLException {
		StringBuilder bloc = new StringBuilder("BEGIN;\n");
		for (int i = 0; i < listeRequete.length; i++) {
			bloc.append(listeRequete[i]).append(semicolon);
		}
		bloc.append("END;\n");
		executeRequest(connexion, bloc.toString());
	}

	/**
	 *
	 * @param connexion
	 * @param requete
	 * @throws SQLException
	 */
	public void executeBlockNoError(Connection connexion, StringBuilder requete) throws SQLException {
		executeRequest(connexion, "do $$ BEGIN " + requete.toString() + " exception when others then END; $$;\n");
	}

	/**
	 *
	 * @param connexion
	 * @param requete
	 * @throws SQLException
	 */
	public void executeBlock(Connection connexion, StringBuilder requete) throws SQLException {
		executeRequest(connexion, "BEGIN;" + requete.toString() + " END;");
	}

	/**
	 *
	 * @param connexion
	 * @param requete
	 * @throws SQLException
	 */
	public void executeBlock(Connection connexion, String requete) throws SQLException {
		executeRequest(connexion, "BEGIN;" + requete + " END;");
	}

	/**
	 * Retourne la liste des valeurs prises par la colonne {@code champ} lors de
	 * l'exécution de la requête {@code sql}
	 *
	 * @param connexion
	 * @param returnedList
	 * @param sql
	 * @param champ
	 * @return
	 */
	public Collection<String> getStringArray(Connection connexion, Collection<String> returnedList, String sql,
			String champ) {
		try {
			ConnectionWrapper connexionWrapper = initConnection(connexion);
			Statement stmt = connexionWrapper.getConnexion().createStatement();
			try {
				ResultSet rs = stmt.executeQuery(sql);
				while (rs.next()) {
					returnedList.add(rs.getString(champ));
				}
			} finally {
				connexionWrapper.close();
				stmt.close();
			}
		} catch (Exception ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "getStringArray()", LOGGER, ex);
		}
		return returnedList;
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
					ResultSet rs = stmt.executeQuery(requete.toString());
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


	/**
	 * Sert à générer une requete a partir d'une requete exécutée en base (les
	 * resultat de la requetes sont concaténés)
	 *
	 * @param query
	 * @return
	 */
	public StringBuilder createQueryByQuery(Connection connexion, String query) {
		StringBuilder requete = new StringBuilder();
		ArrayList<ArrayList<String>> l;
		try {
			l = executeRequest(connexion, query);
			for (int i = 2; i < l.size(); i++) {
				requete.append(l.get(i).get(0) + "\n");
			}
		} catch (SQLException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "createQueryByQuery()", LOGGER, ex);
		}
		return requete;
	}

	public static boolean isNotArchive(String fname) {
		return !fname.endsWith(".tar.gz") && !fname.endsWith(".tgz") && !fname.endsWith(".zip")
				&& !fname.endsWith(".gz");
	}

	/**
	 * Ecrit le résultat de la requête {@code requete} dans le fichier compressé
	 * {@code zos} !Important! la requete doit être ordonnée sur le container <br/>
	 *
	 *
	 * @param connexion
	 * @param requete
	 * @param taos
	 * @param nomPhase
	 *
	 * @param dirSuffix
	 */
	public void zipOutStreamRequeteSelect(Connection connexion, String requete, TarArchiveOutputStream taos,
			String repertoireIn, String anEnvExcecution, String nomPhase, String dirSuffix) {
		StringBuilder requeteLimit = new StringBuilder();
		int k = 0;
		int fetchSize = 5000;
		GenericBean g;
		ArrayList<String> listIdSource = new ArrayList<>();
		ArrayList<String> listIdSourceEtat = new ArrayList<>();
		ArrayList<String> listIdSourceContainer = new ArrayList<>();
		ArrayList<String> listIdSourceEtatContainer = new ArrayList<>();
		ArrayList<String> listContainer = new ArrayList<>();
		String repertoire = repertoireIn + anEnvExcecution.toUpperCase().replace(".", "_") + File.separator;
		// String receptionDirectoryRootOK = repertoire + nomPhase + "_" +
		// etatOk;
		// String receptionDirectoryRootKO = repertoire + nomPhase + "_" +
		// etatKo;
		String currentContainer;
		while (true) {
			// Réécriture de la requete pour avoir le i ème paquet
			requeteLimit.setLength(0);
			requeteLimit.append(requete);
			requeteLimit.append(" offset " + (k * fetchSize) + " limit " + fetchSize + " ");
			// Récupération de la liste d'id_source par paquet de fetchSize
			try {
				g = new GenericBean(this.executeRequest(null, requeteLimit.toString()));
				HashMap<String, ArrayList<String>> m = g.mapContent();
				listIdSource = m.get("id_source");
				listContainer = m.get("container");
				listIdSourceEtat = m.get("etat_traitement");
			} catch (SQLException ex) {
				LoggerHelper.errorGenTextAsComment(getClass(), "zipOutStreamRequeteSelect()", LOGGER, ex);
			}
			if (listIdSource == null) {
				LoggerHelper.traceAsComment(LOGGER, "listIdSource est null, sortie");
				break;
			}
			LoggerHelper.traceAsComment(LOGGER, " listIdSource.size() =", listIdSource.size());
			// Ajout des fichiers à l'archive
			int i = 0;
			while (i < listIdSource.size()) {
				String receptionDirectoryRoot = repertoire + nomPhase + "_"
						+ ManipString.substringBeforeFirst(listIdSource.get(i), "_") + "_" + dirSuffix;
				// fichier non archivé
				if (isNotArchive(listContainer.get(i))) {
					generateEntryFromFile(receptionDirectoryRoot,
							ManipString.substringAfterFirst(listIdSource.get(i), "_"), taos);
					i++;
				} else {
					// on sauvegarde la valeur du container courant
					// on va extraire de la listIdSource tous les fichiers du
					// même container
					currentContainer = ManipString.substringAfterFirst(listContainer.get(i), "_");
					listIdSourceContainer.clear();
					listIdSourceEtatContainer.clear();
					int j = i;
					while (j < listContainer.size()
							&& ManipString.substringAfterFirst(listContainer.get(j), "_").equals(currentContainer)) {
						listIdSourceContainer.add(ManipString.substringAfterFirst(listIdSource.get(j), "_"));
						listIdSourceEtatContainer.add(listIdSourceEtat.get(j));
						j++;
					}
					// archive .tar.gz
					if (currentContainer.endsWith(".tar.gz") || currentContainer.endsWith(".tgz")) {
						generateEntryFromTarGz(receptionDirectoryRoot, currentContainer, listIdSourceContainer, taos);
						i = i + listIdSourceContainer.size();
					} else if (currentContainer.endsWith(".zip")) {
						generateEntryFromZip(receptionDirectoryRoot, currentContainer, listIdSourceContainer, taos);
						i = i + listIdSourceContainer.size();
					}
					// archive .gz
					else if (listContainer.get(i).endsWith(".gz")) {
						generateEntryFromGz(receptionDirectoryRoot, currentContainer, listIdSourceContainer, taos);
						i = i + listIdSourceContainer.size();
					}
				}
			}
			k++;
		}
	}

	public void createAsSelectFrom(Connection aConnexion, String aNomTableCible, String aNomTableSource,
			boolean dropFirst) throws SQLException {
		this.executeRequest(aConnexion, FormatSQL.createAsSelectFrom(aNomTableCible, aNomTableSource, dropFirst));
	}

	public void createAsSelectFrom(Connection aConnexion, String aNomTableCible, String aNomTableSource)
			throws SQLException {
		this.executeRequest(aConnexion, FormatSQL.createAsSelectFrom(aNomTableCible, aNomTableSource));
	}

	public void dupliquerVers(Connection connexion, List<String> sources, List<String> targets) throws SQLException {
		this.executeRequest(connexion, FormatSQL.dupliquerVers(sources, targets));
	}

	public void dupliquerVers(Connection connexion, String source, String target) throws SQLException {
		this.executeRequest(connexion, FormatSQL.dupliquerVers(source, target));
	}

	public void dupliquerVers(Connection connexion, List<String> sources, List<String> targets, String clauseWhere)
			throws SQLException {
		this.executeRequest(connexion, FormatSQL.dupliquerVers(sources, targets, clauseWhere));
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
					executeRequest(connexion, "select tablename from pg_tables where schemaname='pg_catalog'"));
			StringBuilder requete = new StringBuilder();
			for (String t : gb.mapContent().get("tablename")) {
				requete.append(FormatSQL.vacuumSecured(t, type));
			}
			executeImmediate(connexion, requete.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				executeImmediate(connexion, FormatSQL.resetTimeOutMaintenance());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Renvoie la liste des colonnes d'une table (EN MAJUSCULE), avec comme
	 * séparateur une virgule
	 *
	 * @param connexion
	 * @param tableIn
	 * @return
	 * @throws SQLException
	 */
	public static ArrayList<String> listeCol(String poolName, Connection connexion, String tableIn)
			throws SQLException {
		ArrayList<String> colList = new ArrayList<String>();
		colList = new GenericBean(get(poolName).executeRequest(connexion, FormatSQL.listeColonneByHeaders(tableIn)))
				.getHeadersUpperCase();
		return colList;
	}

	/**
	 * Renvoie la liste des colonnes d'une table (EN MAJUSCULE), avec comme
	 * séparateur une virgule
	 *
	 */
	public ArrayList<String> listeCol(Connection connexion, String tableIn) throws SQLException {
		return listeCol(this.pool, connexion, tableIn);
	}

	/*
	 * Prend en entrée une table et une liste de variables, renvoi les variables qui
	 * ne sont pas dans la table Une liste vide veut dire que la table contient
	 * toute les variables
	 */
	public List<String> isColumnsInTable(Connection connexion, String tableIn, List<String> aListVar)
			throws SQLException {
		List<String> colTableIn = getColumns(connexion, new ArrayList<String>(), tableIn).stream()
				.map(t -> t.toLowerCase().trim()).collect(Collectors.toList());
		return aListVar.stream().filter(t -> !colTableIn.contains(t.toLowerCase().trim())).collect(Collectors.toList());
	}

	/**
	 *
	 *
	 * @param aConnexion
	 * @param tableName
	 * @param keys       : clé de jointure. ex: "id_source,id"
	 * @param where      : la clause where sur laquelle se fait la mise à jour. ex :
	 *                   "id='12' and id_source like 'a%'"
	 * @param set        ... : la nouvelle valeur et le nom de la colonne a mettre à
	 *                   jour "12 as a"
	 * @throws SQLException
	 */
	public static void fastUpdate(String poolName, Connection aConnexion, String tableName, String keys, String where,
			String... set) throws SQLException {
		// récupérer la liste des colonnes
		// liste de toutes les colonnes
		ArrayList<String> colList = listeCol(poolName, aConnexion, tableName);
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
		// requete.append("\n create index idx1_" +
		// ManipString.substringAfterFirst(tableName, ".") + " on " +
		// tableFastUpdate +
		// "("+keys+");");
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
		//
		// requete.append("\n WHERE (");
		// for (int i = 0; i < colKeyList.size(); i++)
		// {
		// if (i > 0)
		// {
		// requete.append(" , ");
		// }
		// requete.append(colKeyList.get(i));
		// }
		// requete.append("\n) NOT IN (select distinct ");
		// for (int i = 0; i < colKeyList.size(); i++)
		// {
		// if (i > 0)
		// {
		// requete.append(", ");
		// }
		// requete.append(colKeyList.get(i));
		// }
		// requete.append("\n from " + tableFastUpdate + " b)");
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
	 * Fast update qui accepte les fonctions d'aggregation
	 *
	 * @param poolName
	 * @param aConnexion
	 * @param tableName
	 * @param keys
	 * @param where
	 * @param set
	 * @throws SQLException
	 */
	public void fastUpdateAggr(String poolName, Connection aConnexion, String tableName, String keys, String where,
			String... set) throws SQLException {
		// récupérer la liste des colonnes
		// liste de toutes les colonnes
		ArrayList<String> colList = listeCol(poolName, aConnexion, tableName);
		// liste des colonnes à mettre à jour
		ArrayList<String> colSetList = new ArrayList<String>();
		ArrayList<String> setList = new ArrayList<String>();
		for (int i = 0; i < set.length; i++) {
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
		String tableFastUpdate = FormatSQL.temporaryTableName(tableName, "FA");
		String tableImage = FormatSQL.temporaryTableName(tableName, "IA");
		// reecriture pour prendre en compte les fonction d'aggregation
		// set titi=max() over () se fait avant la clause where
		requete.append("analyze " + tableName + "(");
		for (int i = 0; i < colKeyList.size(); i++) {
			if (i > 0) {
				requete.append(" , ");
			}
			requete.append(colKeyList.get(i));
		}
		requete.append(");");
		requete.append(" drop table if exists " + tableFastUpdate + " ;");
		requete.append("\n create table " + tableFastUpdate + " " + FormatSQL.WITH_NO_VACUUM + " as ");
		requete.append("\n select * from (select " + keys + " ");
		for (int i = 0; i < setList.size(); i++) {
			requete.append("," + setList.get(i));
		}
		requete.append("\n from " + tableName + " ");
		requete.append("\n ) a ");
		requete.append(" WHERE EXISTS (select 1 from " + tableName + " b ");
		requete.append("\n WHERE ");
		for (int i = 0; i < colKeyList.size(); i++) {
			if (i > 0) {
				requete.append("AND ");
			}
			requete.append("a." + colKeyList.get(i) + "=b." + colKeyList.get(i) + " ");
		}
		requete.append("\n AND " + where + " ");
		requete.append("\n ); ");
		// requete.append("\n ) u WHERE (");
		// for (int i = 0; i < colKeyList.size(); i++) {
		// if (i > 0) {
		// requete.append(" , ");
		// }
		// requete.append(colKeyList.get(i));
		// }
		// requete.append(") IN (SELECT distinct ");
		// for (int i = 0; i < colKeyList.size(); i++) {
		// if (i > 0) {
		// requete.append(" , ");
		// }
		// requete.append(colKeyList.get(i));
		// }
		// requete.append(" FROM " + tableName + " WHERE " + where + ") ;");
		// requete.append("\n create index idx1_" +
		// ManipString.substringAfterFirst(tableFastUpdate, ".") + " on " +
		// tableFastUpdate +
		// "("+keys+");");
		requete.append("\n drop table if exists " + tableImage + ";");
		requete.append("\n create table " + tableImage + " " + FormatSQL.WITH_NO_VACUUM + " as ");
		requete.append("\n SELECT ");
		for (int i = 0; i < colList.size(); i++) {
			if (i > 0) {
				requete.append(",");
			}
			requete.append("a." + colList.get(i));
		}
		requete.append("\n FROM " + tableName + " a");
		// requete.append("\n WHERE NOT EXISTS (select 1 from " +
		// tableFastUpdate + " b ");
		// requete.append("\n WHERE ");
		// for (int i = 0; i < colKeyList.size(); i++) {
		// if (i > 0) {
		// requete.append("AND ");
		// }
		// requete.append("a."+colKeyList.get(i)+"=b."+colKeyList.get(i)+" ");
		// }
		//
		// requete.append("\n) ");
		requete.append("\n WHERE (");
		for (int i = 0; i < colKeyList.size(); i++) {
			if (i > 0) {
				requete.append(" , ");
			}
			requete.append(colKeyList.get(i));
		}
		requete.append("\n) NOT IN (select distinct ");
		for (int i = 0; i < colKeyList.size(); i++) {
			if (i > 0) {
				requete.append(" , ");
			}
			requete.append(colKeyList.get(i));
		}
		requete.append("\n from  " + tableFastUpdate + " b)");
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
		requete.append("\n drop table if exists " + tableFastUpdate + ";");
		requete.append("\n drop table if exists " + tableName + ";");
		requete.append(
				"\n alter table " + tableImage + " rename to " + ManipString.substringAfterFirst(tableName, ".") + ";");
		requete.append("analyze " + tableName + " (" + keys + ");");
		this.executeBlockNoError(aConnexion, requete);
		requete.setLength(0);
	}

	/**
	 *
	 * @param connexion
	 * @param directoryOut
	 * @param file
	 * @throws Exception
	 */
	public void export(Connection connexion, String aRequete, String directoryOut, String file) throws Exception {
		FileUtils.mkDirs(Paths.get(directoryOut));
		String fName = "";
		/*
		 * On teste le nom de fichiers pour numéroter le lot
		 */
		for (int i = 0; i < 1000000; i++) {
			fName = directoryOut + File.separator + file + "-" + i + ".csv.gz";
			File f = new File(fName);
			if (!f.exists()) {
				break;
			}
		}
		/**
		 * Copy dans le fichier
		 */
		OutputStream os = new FileOutputStream(fName);
		GZIPOutputStream gzos = new GZIPOutputStream(os) {
			{
				this.def.setLevel(Deflater.BEST_SPEED);
			}
		};
		try {
			exporting(connexion, aRequete, gzos, true, false);
		} finally {
			gzos.close();
		}
	}

	/**
	 * Exporte les données d'une table dans un fichier zip. Ecrase le fichier
	 * précédent ayant le même nom.
	 * 
	 * @param connexion
	 * @param table
	 * @param directoryOut
	 * @param file
	 * @throws Exception
	 */
	public void exportZip(Connection connexion, String table, String directoryOut, String file) throws Exception {
		FileUtils.mkDirs(Paths.get(directoryOut));
		String fName = "";
		String fEntry = "";
		fEntry = file + FileUtils.EXTENSION_CSV;
		fName = directoryOut + File.separator + fEntry + FileUtils.EXTENSION_ZIP;
		/**
		 * Copy dans le fichier Le deuxième argument du FileOutputstream permet
		 * d'écraser le fichier s'il existe déjà
		 */
		OutputStream os = new FileOutputStream(fName, false);
		ZipOutputStream zout = new ZipOutputStream(os) {

			{
				this.def.setLevel(Deflater.BEST_SPEED);
			}
		};

		try {
			zout.putNextEntry(new ZipEntry(fEntry));
			exporting(connexion, table, zout, true);
			zout.closeEntry();
		} finally {
			zout.close();
		}
	}

	/**
	 * export de table postgres dans un stream
	 *
	 * @param connexion
	 * @param table
	 * @param os
	 * @param csv       : true / false (binary)
	 * @throws SQLException
	 * @throws IOException
	 */
	public void exporting(Connection connexion, String table, OutputStream os, boolean csv, boolean... forceQuote)
			throws SQLException, IOException {
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
		} finally {
			conn.close();
		}
	}

	/**
	 * TODO à mutualiser avec exporting export de table postgres dans un stream
	 *
	 * @param connexion
	 * @param table
	 * @param os
	 * @param csv       : true / false (binary)
	 * @throws SQLException
	 * @throws IOException
	 */
	public void exportingWithoutHeader(Connection connexion, String table, OutputStream os, boolean csv,
			boolean... forceQuote) throws SQLException, IOException {
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
	 * @param aDelim
	 * @throws Exception
	 */
	public void importing(Connection connexion, String table, InputStream is, boolean csv, String... aDelim)
			throws Exception {
		ConnectionWrapper conn = initConnection(connexion);
		try {
			conn.getConnexion().setAutoCommit(false);
			CopyManager copyManager = new CopyManager((BaseConnection) conn.getConnexion());
			String delimiter = "";
			String quote = Character.toString((char) 2);

			if (aDelim != null && aDelim.length > 0) {
				delimiter = ", DELIMITER '" + aDelim[0] + "', QUOTE '" + quote + "' ";
			}

			boolean header = true;
			if (aDelim != null && aDelim.length > 1) {
				header = false;
			}

			String h = (header ? ", HEADER true " : "");

			if (csv) {
				copyManager.copyIn("COPY " + table + " FROM STDIN WITH (FORMAT CSV " + h + delimiter + ") ", is);
			} else {
				copyManager.copyIn("COPY " + table + " FROM STDIN WITH (FORMAT BINARY)", is);
			}
			conn.getConnexion().commit();
		} catch (Exception e) {
			e.printStackTrace();
			conn.getConnexion().rollback();
		} finally {
			conn.close();
		}
	}

	public void importing(Connection connexion, String table, Reader aReader, boolean csv, boolean header,
			String... aDelim) throws Exception {
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
		} catch (Exception e) {
			e.printStackTrace();
			conn.getConnexion().rollback();
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
	 * @throws Exception
	 */
	public void importing(Connection connexion, String table, String aColumnName, InputStream is, boolean csv,
			boolean header, String aDelim, String aQuote) throws Exception {
		importing(connexion, table, aColumnName, is, csv ? FORMAT_CSV : FORMAT_BINARY, header, aDelim, aQuote);
	}

	public void importing(Connection connexion, String table, String aColumnName, InputStream is, boolean csv,
			boolean header, String aDelim, String aQuote, String encoding) throws Exception {
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
	 * @throws Exception
	 */
	public void importing(Connection connexion, String table, String aColumnName, InputStream is, String format,
			boolean header, String aDelim, String aQuote) throws Exception {
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
	 * @throws Exception
	 */
	public void importing(Connection connexion, String table, String aColumnName, InputStream is, String format,
			boolean header, String aDelim, String aQuote, String encoding) throws Exception {
		LoggerHelper.info(LOGGER, "importing()");
		ConnectionWrapper conn = initConnection(connexion);
		try {
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
		} catch (Exception e) {

			e.printStackTrace();

			if (e.getMessage().startsWith("ERROR: missing data for column")) {

				throw new Exception("Il manque une/des colonne dans le corps du fichier");

			} else if (e.getMessage().startsWith("ERROR: extra data after last expected column")) {

				throw new Exception("Il manque un/des headers");

			} else {
				throw e;
			}

		} finally {
			conn.close();
		}
	}

	/**
	 * On passe une table et une liste de colonne pour voir si un index avec ces
	 * colonnes existe déjà
	 * 
	 * @param aConnection
	 * @param aListeColonnes
	 * @param aTable
	 * @param aSchema
	 * @return
	 */
	public boolean isIndexAlreadyExists(Connection aConnection, List<String> aListeColonnes, String aTable,
			String aSchema) {
		boolean returned = false;
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT unnest(REGEXP_MATCHES(indexdef, '\\((.*)\\)')) ");
		requete.append("\n FROM pg_indexes ");
		requete.append("\n WHERE tablename = " + FormatSQL.textToSql(aTable) + " and schemaname = "
				+ FormatSQL.textToSql(aSchema) + ";");
		List<String> listeColonnesIndexs = getList(aConnection, requete.toString(), new ArrayList<>());
		LoggerHelper.debug(LOGGER, "listeColonnesIndexs : ", listeColonnesIndexs.toString());
		System.out.println("aListeColonnes : " + aListeColonnes.toString());
		System.out.println("listeColonnesIndexs : " + listeColonnesIndexs.toString());

		for (String index : listeColonnesIndexs) {
			List<String> colonnesIndex = ManipString.stringToList(index, ",");
			if (colonnesIndex.containsAll(aListeColonnes) && aListeColonnes.containsAll(colonnesIndex))
				returned = true;
		}
		return returned;
	}

	/**
	 * Recherche dans un schema donné l'existence d'au moins un index dons le nom
	 * correspond au pattern donné. Si plusieurs index sont trouvés un warning est
	 * ajouté dans les logs.
	 * 
	 * @param aConnection
	 * @param aSchema
	 * @param idxNamePattern
	 * @return
	 */
	public boolean isIndexAlreadyExists(Connection cnx, String aSchema, String idxNamePattern) {

		StringBuilder sql = new StringBuilder();
		sql.append("\n SELECT 1 ");
		sql.append("\n FROM pg_indexes ");
		sql.append("\n WHERE schemaname = " + FormatSQL.textToSql(aSchema));
		sql.append("\n AND indexName ilike " + FormatSQL.textToSql(idxNamePattern));
		long nb = getCountFromRequest(cnx, sql.toString());

		if (nb > 1) {
			LoggerHelper.warn(LOGGER,
					"Plusieurs index trouvée dans le schéma " + aSchema + " pour le pattern " + idxNamePattern);
		}

		return (nb >= 1);
	}

	public void importing(Connection connexion, String table, InputStream is, boolean csv, String aDelim)
			throws Exception {
		importing(connexion, table, null, is, csv, true, aDelim, null);
	}

	public void importing(Connection connexion, String table, InputStream is, boolean csv) throws Exception {
		importing(connexion, table, null, is, csv, true, null, null);
	}

	public void importing(Connection connexion, String table, String aColumnName, InputStream is, boolean csv)
			throws Exception {
		importing(connexion, table, aColumnName, is, csv, true, null, null);
	}

	public void importing(Connection connexion, String table, InputStream is, boolean csv, boolean aHeader,
			String aDelim) throws Exception {
		importing(connexion, table, null, is, csv, aHeader, aDelim, null);
	}

	public void importing(Connection connexion, String table, String aColumnName, InputStream is, boolean csv,
			boolean aHeader, String aDelim) throws Exception {
		importing(connexion, table, aColumnName, is, csv, aHeader, aDelim, null);

	}

	public Date getDate(Connection aConnexion, StringBuilder aRequete, SimpleDateFormat aSimpleDateFomrat)
			throws ParseException, SQLException {
		return getDate(aConnexion, aRequete.toString(), aSimpleDateFomrat);
	}

	public Date getDate(Connection aConnexion, String aRequete, SimpleDateFormat aSimpleDateFomrat)
			throws ParseException, SQLException {
		String resultat = getString(aConnexion, aRequete);
		return resultat == null ? null : aSimpleDateFomrat.parse(resultat);
	}

	/**
	 *
	 * @param aConnexion
	 * @param schema
	 * @param pattern
	 * @return la liste des tables telles que {@code pg_namespace.nspname = schema}
	 *         et {@code pg_class.relname LIKE pattern}
	 */
	public List<String> listeTablesFromPattern(Connection aConnexion, String schema, String pattern) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT nspname||'.'||relname AS nom_qualifie");
		requete.append("\n FROM pg_class INNER JOIN pg_namespace ON pg_class.relnamespace = pg_namespace.oid");
		requete.append("\n WHERE pg_namespace.nspname = '" + schema + "'");
		requete.append("\n   AND pg_class.relname LIKE '" + pattern + "'");
		requete.append("\n   AND pg_class.relkind = 'r'");
		requete.append(";");
		return getList(aConnexion, requete, new ArrayList<String>());
	}

	/**
	 *
	 * @param aConnexion
	 * @param schema
	 * @param pattern
	 * @return la liste des tables telles que {@code pg_namespace.nspname = schema}
	 *         et {@code pg_class.relname LIKE pattern}
	 */
	public List<String> listeTablesFromConditions(Connection aConnexion, String schema, String... conditions) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT nspname||'.'||relname AS nom_qualifie");
		requete.append("\n FROM pg_class INNER JOIN pg_namespace ON pg_class.relnamespace = pg_namespace.oid");
		requete.append("\n WHERE pg_namespace.nspname = '" + schema + "'");
		for (String condition : conditions) {
			requete.append("\n   AND pg_class.relname " + condition);
		}
		requete.append("\n   AND pg_class.relkind = 'r'");
		requete.append(";");
		return getList(aConnexion, requete, new ArrayList<String>());
	}

	public List<String> listeContraintes(Connection connexion, String aSchema, String aTable, String aTypeContrainte) {
		return getList(connexion, FormatSQL.listeContraintes(aSchema, aTable, aTypeContrainte),
				new ArrayList<String>());
	}

	public List<String> listeAllContraintes(Connection connexion, String aSchema, String aTable) {
		List<String> returned = new ArrayList<>();
		List<String> listeTypeConstraint = new ArrayList<>(Arrays.asList("c", "f", "p", "u", "t", "x"));
		for (String type : listeTypeConstraint) {
			returned.addAll(
					getList(connexion, FormatSQL.listeContraintes(aSchema, aTable, type), new ArrayList<String>()));
		}
		return returned;
	}

	/**
	 *
	 * @param aConnexion
	 * @param schema
	 * @param pattern
	 * @return la liste des tables telles que {@code pg_namespace.nspname = schema}
	 *         et {@code pg_class.relname LIKE pattern}
	 */
	public List<String> listeTablesFromPattern(Connection aConnexion, String schema, Collection<String> patterns) {
		StringBuilder requete = new StringBuilder();
		requete.append("\n SELECT schemaname||'.'||tablename AS nom_qualifie");
		requete.append("\n FROM pg_tables ");
		requete.append("\n WHERE schemaname='" + schema + "'");
		requete.append("\n AND ( ");
		boolean isFirst = true;
		for (String pattern : patterns) {
			if (isFirst) {
				isFirst = false;
			} else {
				requete.append("\n   OR ");
			}
			requete.append("tablename LIKE '" + pattern + "'");
		}
		requete.append(");");
		return getList(aConnexion, requete, new ArrayList<String>());
	}

	/**
	 * Quand postgresql 9.5 arrive, virer cette méthode et faire des create index if
	 * not exists
	 *
	 * @param aConnexion
	 * @param nomIndex
	 * @param nomSchema
	 * @param nomTable
	 * @return
	 */
	@Deprecated
	public boolean isIndexExist(Connection aConnexion, String nomIndex, String nomSchema, String nomTable) {
		return getCountFromRequest(aConnexion, "SELECT * FROM pg_indexes WHERE indexname = '" + nomIndex
				+ "' AND tablename = '" + nomTable + "' AND schemaname = '" + nomSchema + "'") > 0;
	}

	public boolean isSilent() {
		return this.silent;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public static Connection getConnectionFromProperties(String aPool, String aCheminAcces) {
		try {
		    // get some properties manually
		    PropertiesHandler properties = PropertiesHandler.getInstance();
		    return DriverManager.getConnection(properties.getDatabaseUrl()//
			    , properties.getDatabaseUsername()//
			    , properties.getDatabasePassword()//
		    );
		} catch (Exception e) {
			LoggerHelper.error(LOGGER, e, e.getMessage());
		}
		return null;
	}

	public static void setConnectionName(Connection connection, String connectioName) {
		Properties props = new Properties();
		props.setProperty("ApplicationName ", "connectioName");
		try {
			connection.setClientInfo(props);
		} catch (SQLClientInfoException e) {
			LoggerHelper.error(LOGGER, "error in setConnectionName()");
		}
	}

}
