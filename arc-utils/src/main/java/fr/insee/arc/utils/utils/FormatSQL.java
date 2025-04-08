package fr.insee.arc.utils.utils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.core.Utils;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.database.Delimiters;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.security.SqlInjectionChecked;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.textUtils.IConstanteNumerique;

public class FormatSQL implements IConstanteCaractere, IConstanteNumerique {
	private FormatSQL() {
		throw new IllegalStateException("Utility class");
	}

	public static final String NULL = "null";
	public static final String NO_VACUUM = " (autovacuum_enabled = false, toast.autovacuum_enabled = false) ";
	public static final String WITH_NO_VACUUM = " WITH" + NO_VACUUM;

	// temporary table generation token name
	public static final String TMP = "$tmp$";
	public static final String REGEX_TMP = "\\$tmp\\$";

	public static final String IMG = "img";
	
	public static final boolean DROP_FIRST_FALSE = false;
	public static final boolean DROP_FIRST_TRUE = true;

	public static final int TAILLE_MAXIMAL_BLOC_SQL = 700000;
	public static final int MAXIMUM_NUMBER_OF_BIND_IN_PREPARED_STATEMENT = 10000;
	public static final int MAXIMUM_NUMBER_OF_LINE_IN_PREPARED_STATEMENT_BLOCK = 100;

	public static final int TIMEOUT_MAINTENANCE = 600000;

	public static final String VACUUM_OPTION_NONE = "";
	public static final String VACUUM_OPTION_FREEZE = "freeze";
	public static final String VACUUM_OPTION_FULL = "full";
	public static final int NUMBER_OF_DEAD_TUPLES_FOR_VACUUM = 100000;
	
	private static final Logger LOGGER = LogManager.getLogger(FormatSQL.class);
	

	/**
	 * query to drop a table in database
	 * 
	 * @param tableName
	 * @return
	 */
	public static GenericPreparedStatementBuilder dropTable(String... someTables) {
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		for (String tableName : someTables) {
			query.build(SQL.DROP, SQL.TABLE, SQL.IF_EXISTS, tableName, SQL.END_QUERY, SQL.BR);
		}
		return query;
	}

	/**
	 * query to retrieve
	 * 
	 * @param table
	 * @return
	 */
	@SqlInjectionChecked
	public static GenericPreparedStatementBuilder tableExists(String table) {
		String tableSchema = extractSchemaNameToken(table);
		String tableName = extractTableNameToken(table);

		GenericPreparedStatementBuilder requete = new GenericPreparedStatementBuilder();
		requete.append("SELECT schemaname||'.'||tablename AS table_name FROM pg_tables ");
		requete.append("\n WHERE tablename like " + requete.quoteText(tableName.toLowerCase()) + " ");
		if (tableSchema != null) {
			requete.append("\n AND schemaname = " + requete.quoteText(tableSchema.toLowerCase()) + " ");
		}
		return requete;
	}

	public static String extractSchemaNameToken(String fullTableName) {
		return fullTableName.contains(SQL.DOT.getSqlCode())
				? ManipString.substringBeforeFirst(fullTableName, SQL.DOT.getSqlCode())
				: null;
	}

	public static String extractTableNameToken(String fullTableName) {
		return ManipString.substringAfterFirst(fullTableName, SQL.DOT.getSqlCode());
	}

	/**
	 * Pour récupérer la liste des colonnes d'une table rapidement
	 *
	 * @param table
	 * @return
	 */
	public static GenericPreparedStatementBuilder listeColonneByHeaders(String table) {
		return new GenericPreparedStatementBuilder("select * from " + table + " where false; ");
	}

	/**
	 * reset role
	 * 
	 * @param roleName
	 * @return
	 * @throws ArcException
	 */
	public static GenericPreparedStatementBuilder resetRole() {
		return new GenericPreparedStatementBuilder("RESET role;").append(SQL.COMMIT).append(SQL.END_QUERY);
	}
	
	/**
	 * Switch the database user
	 * 
	 * @param roleName
	 * @return
	 * @throws ArcException
	 */
	public static GenericPreparedStatementBuilder changeRole(String roleName) {
		return setConfig("role", roleName).append(SQL.COMMIT).append(SQL.END_QUERY);
	}
	
	/**
	 * Query to set database parameters with prepared statement
	 * @param databaseParameter
	 * @param value
	 * @return
	 */
	@SqlInjectionChecked
	public static GenericPreparedStatementBuilder setConfig(String databaseParameter, String value)
	{
		GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
		query.append("SELECT set_config("+query.quoteText(databaseParameter)+", "+query.quoteText(value)+", false);");
		return query;
	}

	/**
	 * timeOut
	 */
	public static String setTimeOutMaintenance() {
		return "BEGIN;SET statement_timeout=" + TIMEOUT_MAINTENANCE + ";COMMIT;";
	}

	public static String resetTimeOutMaintenance() {
		return "BEGIN;RESET statement_timeout;COMMIT;";
	}

	/**
	 * essaie d'exectuer une requete et si elle n'échoue ne fait rien
	 */
	public static String tryQuery(String query) {
		return "do $$ begin " + query + " exception when others then end; $$; ";
	}
	
	/**
	 * build a query to limit the number of row given of a given query
	 * @param query
	 * @return
	 */
	public static GenericPreparedStatementBuilder limitQuery(GenericPreparedStatementBuilder query, int limit)
	{
		GenericPreparedStatementBuilder limitQuery = new GenericPreparedStatementBuilder();
		limitQuery.build(SQL.SELECT, "*", SQL.FROM, "(", query, ") ww ", SQL.LIMIT, limit );
		return limitQuery;
	}

	/**
	 * build a query that try a generic query and report the result
	 * @param query
	 * @return
	 */
	public static GenericPreparedStatementBuilder tryQueryAndReport(GenericPreparedStatementBuilder query) {
		GenericPreparedStatementBuilder queryToTestInputQuery = new GenericPreparedStatementBuilder();
		queryToTestInputQuery.build(SQL.SELECT, "public.try_query(", queryToTestInputQuery.quoteText(query.getQueryWithParameters()), "::text)");
		return queryToTestInputQuery;
	}
	
	
	/**
	 * Lance un vacuum d'un certain type sur une table
	 * 
	 * @param table
	 * @param type
	 * @return
	 */
	public static String vacuumSecured(String table, String type) {
		return "VACUUM " + type + " ANALYZE " + table + "; COMMIT; \n";
	}

	/**
	 * Lance un vacuum d'un certain type sur une table
	 * 
	 * @param table
	 * @param type
	 * @return
	 */
	public static String analyzeSecured(String table) {
		return "ANALYZE " + table + "; COMMIT; \n";
	}

	/**
	 * CREATE TABLE @tableOut as SELECT all_columns FROM @tableIn WHERE @where
	 * 
	 * @param tableIn
	 * @param tableOut
	 * @param where
	 * @return
	 */
	@SqlInjectionChecked
	public static GenericPreparedStatementBuilder createTableAsSelectWhere(String tableIn, String tableOut, String where) {
		GenericPreparedStatementBuilder requete = new GenericPreparedStatementBuilder();
		requete.append(FormatSQL.dropTable(tableOut));

		requete.append("\n CREATE ");
		if (!tableOut.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append(" ");
		}
		requete.append("TABLE ").append(tableOut).append(" ").append(FormatSQL.WITH_NO_VACUUM)
				.append(" AS SELECT * FROM ").append(tableIn).append(" a WHERE ").append(where);
		requete.append("; ");
		return requete;
	}

	/**
	 * Recopie une table à l'identique
	 *
	 * @param table
	 * @param where
	 * @param triggersAndIndexes
	 * @return
	 */
	public static GenericPreparedStatementBuilder rebuildTableAsSelectWhere(String table, String where) {
		String tableRebuild = temporaryTableName(table, "RB");

		GenericPreparedStatementBuilder requete = new GenericPreparedStatementBuilder();
		requete.append("set enable_nestloop=off; ");

		requete.append(createTableAsSelectWhere(table, tableRebuild, where));

		requete.append(FormatSQL.dropTable(table));

		requete.append(
				"\n ALTER TABLE " + tableRebuild + " RENAME TO " + ManipString.substringAfterFirst(table, ".") + " ;");
		requete.append("set enable_nestloop=on; ");
		return requete;
	}

	/**
	 * this sql block test is the query to test is true to execute the other query
	 * 
	 * @param queryToTest
	 * @param queryToExecute
	 * @return
	 */
	public static String executeIf(String queryToTest, String queryToExecute) {
		StringBuilder query = new StringBuilder();
		query.append("do $$ declare b boolean; begin execute ").append(escapeLiteralForPgFunction(queryToTest)).append(" into b; ")
				.append("if (b) then execute ").append(escapeLiteralForPgFunction(queryToExecute)).append("; end if; end; $$;");
		return query.toString();
	}

	public static String executeIf(StringBuilder queryToTest, StringBuilder queryToExecute) {
		return executeIf(queryToTest.toString(), queryToExecute.toString());
	}

	/**
	 * query that return true is the query as at least one record
	 * 
	 * @param tableIn
	 * @return
	 */
	public static String hasRecord(String tableIn) {
		return "SELECT (count(*)>0) as has_record FROM (SELECT 1 FROM " + tableIn + " LIMIT 1) u";
	}

	/**
	 * check if table is temporary according to its name no SQL.DOT in temporary
	 * 
	 * @return
	 */
	public static boolean isTemporary(String tablename) {
		return !tablename.contains(SQL.DOT.getSqlCode());
	}

	/**
	 * Ajoute un suffixe de table temporaire au nom de table {@code aName}
	 *
	 * @param aName
	 * @return
	 */
	public static final String temporaryTableName(String aName) {
		String newName = aName.split(REGEX_TMP)[0];
		return new StringBuilder(newName.toLowerCase()).append(TMP).append(new TemporaryToken().getToken()).toString();
	}

	/**
	 * Ajoute un suffixe de table temporaire au nom de table {@code prefix}
	 *
	 * @param aName  le nom de la table
	 * @param suffix un suffixe
	 * @return
	 */
	public static final String temporaryTableName(String aName, String suffix) {
		String newName = aName.split(REGEX_TMP)[0];
		return temporaryTableName(newName + underscore + suffix);
	}

	/**
	 * Ajoute un suffixe image à un objet de base de données
	 * @param databaseObject
	 * @return
	 */
	public static final String imageObjectName(String databaseObject) {
		return databaseObject + Delimiters.SQL_TOKEN_DELIMITER + IMG;
	}
	
	/**
	 * Ne garde que les séparateurs
	 *
	 * @param tokens
	 * @param separator
	 * @return
	 */
	public static String toNullRow(Collection<?> tokens) {
		return (tokens == null || tokens.isEmpty()) ? "(" + empty + ")"
				: "(" + StringUtils.repeat(",", tokens.size() - 1) + ")";
	}

	/**
	 * Renvoie les tables héritant de celle-ci Colonnes de résultat:
	 * 
	 * @child (schema.table)
	 */
	public static GenericPreparedStatementBuilder getAllInheritedTables(String tableSchema, String tableName) {
		GenericPreparedStatementBuilder requete = new GenericPreparedStatementBuilder();
		requete.append("\n SELECT cn.nspname||'.'||c.relname AS child ");
		requete.append("\n FROM pg_inherits  ");
		requete.append("\n JOIN pg_class AS c ON (inhrelid=c.oid) ");
		requete.append("\n JOIN pg_class as p ON (inhparent=p.oid) ");
		requete.append("\n JOIN pg_namespace pn ON pn.oid = p.relnamespace ");
		requete.append("\n JOIN pg_namespace cn ON cn.oid = c.relnamespace ");
		requete.append("\n WHERE p.relname = " + requete.quoteText(tableName) + " and pn.nspname = "
				+ requete.quoteText(tableSchema) + " ");
		return requete;
	}

	

	/**
	 * converti une chaine de caractere pour etre mise en parametre d'un sql si
	 * c'est vide, ca devient "null" quote devient quote quote
	 *
	 * @param val
	 * @return
	 */
	public static String quoteText(String val) {
		return val == null ? "NULL" : new StringBuilder("'").append(convertSqlQuotes(val)).append("'").toString();
	}

	
	private static String convertSqlQuotes(String val) {
		return val.replace("'", "''");
	}

	/**
	 * escape quote through function
	 * 
	 * @param s
	 * @return
	 * @throws ArcException
	 */
	public static String escapeLiteralForPgFunction(String s) {
			try {
				return "'" + Utils.escapeLiteral(null, s, true) + "'";
			} catch (SQLException e) {
				LoggerHelper.errorAsComment(LOGGER, "The string "+ s + " cannot be escaped to postgres database format");
				throw new IllegalArgumentException(s);
			}
	}

	/**
	 * query expression to convert a date format
	 * 
	 * @param dateTextIn
	 * @param formatIn
	 * @return
	 */
	public static GenericPreparedStatementBuilder toDate(String dateTextIn, String formatIn) {
		return new GenericPreparedStatementBuilder().append("to_date(").appendTextWithoutBinding(dateTextIn).append("::text,").appendTextWithoutBinding(formatIn).append(")");
	}

	public static GenericPreparedStatementBuilder truncate(String fullName) {
		return new GenericPreparedStatementBuilder("truncate " + fullName + ";");
	}

	/**
	 * convert a java array to a sql array
	 * @param colonnes
	 * @return
	 */
	public static String javaArrayToSqlArray(String[] colonnes) {
		return Arrays.asList(colonnes).toString().replaceFirst("^\\[", "{").replaceFirst("\\]$", "}");
	}

}
