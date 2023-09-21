package fr.insee.arc.utils.utils;

import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.core.Utils;

import fr.insee.arc.utils.dao.GenericPreparedStatementBuilder;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.textUtils.IConstanteNumerique;

public class FormatSQL implements IConstanteCaractere, IConstanteNumerique
{
    public static final String NULL = "null";
    public static final String NO_VACUUM = " (autovacuum_enabled = false, toast.autovacuum_enabled = false) ";
    public static final String WITH_NO_VACUUM = " WITH" + NO_VACUUM;
    
    // temporary table generation token name
    public static final String TMP = "$tmp$";
    public static final String REGEX_TMP = "\\$tmp\\$";

    
    public static final boolean DROP_FIRST_FALSE = false;
    public static final boolean DROP_FIRST_TRUE = true;
    
    public static final int TAILLE_MAXIMAL_BLOC_SQL = 700000;

    public static final int TIMEOUT_MAINTENANCE = 600000;
    
    public static final String VACUUM_OPTION_NONE="";
    public static final String VACUUM_OPTION_FULL="full";
    
    private static final Logger LOGGER = LogManager.getLogger(FormatSQL.class);
    
    /**
     * query to drop a table in database
     * @param tableName
     * @return
     */
    public static String dropTable(String... someTables) {
    	GenericPreparedStatementBuilder query = new GenericPreparedStatementBuilder();
    	for (String tableName:someTables)
    	{
    		query.build(SQL.DROP, SQL.TABLE, SQL.IF_EXISTS, tableName, SQL.END_QUERY, SQL.BR);
    	}
        return query.toString();
    }
    
    /**
     * query to retrieve
     * @param table
     * @return
     */
    public static GenericPreparedStatementBuilder tableExists(String table) {
	String tableSchema = extractSchemaNameToken(table);
	String tableName = extractTableNameToken(table);
	
	GenericPreparedStatementBuilder requete = new GenericPreparedStatementBuilder();
	requete.append("SELECT schemaname||'.'||tablename AS table_name FROM pg_tables ");
	requete.append("\n WHERE tablename like " + requete.quoteText(tableName.toLowerCase()) + " ");
	if (tableSchema!=null) {
		requete.append("\n AND schemaname = " + requete.quoteText(tableSchema.toLowerCase()) + " ");
	}
	return requete;
    }

    
    public static String extractSchemaNameToken(String fullTableName)
    {
    	return fullTableName.contains(SQL.DOT.getSqlCode()) ? ManipString.substringBeforeFirst(fullTableName, SQL.DOT.getSqlCode()) : null;
    }
    
    public static String extractTableNameToken(String fullTableName)
    {
    	return ManipString.substringAfterFirst(fullTableName, SQL.DOT.getSqlCode());
    }

    
   
    /**
     * Pour récupérer la liste des colonnes d'une table rapidement
     *
     * @param table
     * @return
     */
    public static GenericPreparedStatementBuilder listeColonneByHeaders(String table)
    {
        return new GenericPreparedStatementBuilder("select * from " + table + " where false; ");
    }

    /**
     * Switch the database user
     * @param roleName
     * @return
     * @throws ArcException
     */
	public static String changeRole(String roleName)
	{
		return "SET role='"+roleName+"';COMMIT;";
	}


    
    /**
     * timeOut
     */
    public static String setTimeOutMaintenance()
    {
        return "BEGIN;SET statement_timeout="+TIMEOUT_MAINTENANCE+";COMMIT;";
    }
    
    public static String resetTimeOutMaintenance()
    {
        return "BEGIN;RESET statement_timeout;COMMIT;";
    }
    
    /**
     * essaie d'exectuer une requete et si elle n'échoue ne fait rien
     */
    public static String tryQuery(String query)
    {
        return "do $$ begin " + query + " exception when others then end; $$; ";
    }

    /**
     * Met entre cote ou renvoie null (comme pour un champ de base de donnée)
     *
     * @param t
     * @return
     */
    public static String cast(String t)
    {
        if (t == null)
        {
            return "null";
        }
        else
        {
            return "'" + t + "'";
        }
    }

    /**
     * Lance un vacuum d'un certain type sur une table
     * @param table
     * @param type
     * @return
     */
    public static String vacuumSecured(String table, String type)
    {    		
    	return "VACUUM "+ type +" " + table + "; COMMIT; \n"; 
    }

    /**
     * Lance un vacuum d'un certain type sur une table
     * @param table
     * @param type
     * @return
     */
    public static String analyzeSecured(String table)
    {    		
    	return "ANALYZE " + table + "; COMMIT; \n"; 
    }
    
    /**
     * CREATE TABLE @tableOut as SELECT all_columns FROM @tableIn WHERE @where
     * @param tableIn
     * @param tableOut
     * @param where
     * @return
     */
    public static String createTableAsSelectWhere(String tableIn, String tableOut, String where)
    {
        StringBuilder requete = new StringBuilder();
		requete.append(FormatSQL.dropTable(tableOut));

        requete.append("\n CREATE ");
        if (!tableOut.contains("."))
        {
            requete.append("TEMPORARY ");
        }
        else
        {
            requete.append(" ");
        }
        requete.append("TABLE ").append(tableOut).append(" ").append(FormatSQL.WITH_NO_VACUUM)
        .append(" AS SELECT * FROM ").append(tableIn).append(" a WHERE ").append(where);
        requete.append("; ");
        return requete.toString();
    }
    
    
    /**
     * Recopie une table à l'identique
     *
     * @param table
     * @param where
     * @param triggersAndIndexes
     * @return
     */
    public static StringBuilder rebuildTableAsSelectWhere(String table, String where)
    {
        String tableRebuild = temporaryTableName(table, "RB");
        
        StringBuilder requete = new StringBuilder();
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
     * @param queryToTest
     * @param queryToExecute
     * @return
     */
    public static String executeIf(String queryToTest, String queryToExecute)
    {
    	StringBuilder query=new StringBuilder();
    	query
    	.append("do $$ declare b boolean; begin execute ")
    	.append(quoteText(queryToTest))
    	.append(" into b; ")
    	.append("if (b) then execute ")
    	.append(quoteText(queryToExecute))
    	.append("; end if; end; $$;");
    	return query.toString();
    }

    public static String executeIf(StringBuilder queryToTest, StringBuilder queryToExecute)
    {
    	return executeIf(queryToTest.toString(), queryToExecute.toString());
    }

    /**
     * query that return true is the query as at least one record
     * @param tableIn
     * @return
     */
    public static String hasRecord(String tableIn)
    {
    	return "SELECT (count(*)>0) as has_record FROM (SELECT 1 FROM " + tableIn + " LIMIT 1) u";
    }

    /**
     * check if table is temporary according to its name
     * no SQL.DOT in temporary
     * @return
     */
    public static boolean isTemporary(String tablename)
    {
    	return !tablename.contains(SQL.DOT.getSqlCode());
    }
    
    
     /**
     * Ajoute un suffixe de table temporaire au nom de table {@code aName}
     *
     * @param aName
     * @return
     */
    public static final String temporaryTableName(String aName)
    {
        String newName = aName.split(REGEX_TMP)[0];
        // on met la date du jour dans le nom de la table
        String l = System.currentTimeMillis() + "";
        // on prend que les 10 derniers chiffres (durrée de vie : 6 mois)
        l = l.substring(l.length() - 10);
        // on inverse la chaine de caractere pour avoir les millisecondes en
        // premier en cas de troncature
        l = new StringBuffer(l).reverse().toString();
        return new StringBuilder(newName).append(TMP).append(l).append(dollar).append(randomNumber(4)).toString();
    }

    /**
     * Ajoute un suffixe de table temporaire au nom de table {@code prefix}
     *
     * @param aName
     *            le nom de la table
     * @param suffix
     *            un suffixe
     * @return
     */
    public static final String temporaryTableName(String aName, String suffix)
    {
        String newName = aName.split(REGEX_TMP)[0];
        return temporaryTableName(newName + underscore + suffix);
    }

    /**
     *
     * @return Un nombre aléatoire d'une certaine précision
     */
    public static final String randomNumber(int precision)
    {
        String rn = ((int) Math.floor((Math.random() * (Math.pow(10, precision))))) + "";
        return ManipString.padLeft(rn, "0", precision);
    }

    /**
     * converti une chaine de caractere pour etre mise en parametre d'un sql si
     * c'est vide, ca devient "null" quote devient quote quote
     *
     * @param val
     * @return
     */
    public static String textToSql(String val)
    {
    	return val == null ? "NULL" : "'" + val.replace("'", "''") + "'";
    }

    /**
     * Ne garde que les séparateurs
     *
     * @param tokens
     * @param separator
     * @return
     */
    public static String toNullRow(Collection<?> tokens)
    {
        return (tokens == null || tokens.isEmpty()) ? "(" + empty + ")"
                : "(" + StringUtils.repeat(",", tokens.size() - 1) + ")";
    }

    /**
     * Renvoie les tables héritant de celle-ci
     * Colonnes de résultat:
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
    	requete.append("\n WHERE p.relname = "+requete.quoteText(tableName)+" and pn.nspname = "+requete.quoteText(tableSchema)+" ");
    	return requete;
    }
    
    /**
     * escape quote return value through function
     * @param s
     * @return
     * @throws ArcException 
     */
    public static String quoteText(String s)
    {
    	try {
			return "'" + Utils.escapeLiteral(null, s, true) + "'";
		} catch (SQLException e) {
			LoggerHelper.errorAsComment(LOGGER, "This string cannot be escaped to postgres database format");
			return null;
		}
    }
    
    /**
     * query expression to convert a date format
     * @param dateTextIn
     * @param formatIn
     * @return
     */
    public static String toDate(String dateTextIn, String formatIn)
    {
    	return "to_date("+dateTextIn+"::text,"+formatIn+")";
    }
    
}
