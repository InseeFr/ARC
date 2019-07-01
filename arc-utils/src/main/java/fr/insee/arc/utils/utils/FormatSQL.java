package fr.insee.arc.utils.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.textUtils.IConstanteNumerique;
import fr.insee.arc.utils.textUtils.SequentialUntokenizer;

public class FormatSQL implements IConstanteCaractere, IConstanteNumerique {

    private static final String END_COMMENTARY = "*/";
    private static final String BEGIN_COMMENTARY = "/*";
    private static final String COLUMN_NAME_DATA_TYPE_UDT_NAME_AS_T = " column_name||'.'||data_type||'.'||udt_name as t ";
    private static final String VACUUM_ = "VACUUM ";
    private static final String _VACUUM_ = " vacuum ";
    private static final String COALESCE_OPEN = "coalesce(";
    private static final String TEMPORARY_ = "TEMPORARY ";
    private static final String _DISTINCT_ = " DISTINCT ";
    private static final String _ALTER_ = " ALTER ";
    private static final String _VALUES_ = " VALUES ";
    private static final String _JOIN_ = " JOIN ";
    private static final String NO_ = "NO ";
    private static final String INHERIT_ = "INHERIT ";
    private static final String ALTER_ = "ALTER ";
    private static final String OFFSET_ = "OFFSET ";
    private static final String LIMIT_ = "LIMIT ";
    private static final String DESC = "DESC";
    private static final String ORDER_BY = "ORDER BY";
    private static final String ORDER_BY_ = ORDER_BY + SPACE;
    private static final String _COMMA_ = SPACE + COMMA + SPACE;
    private static final String TABLE_SCHEMA = "table_schema";
    private static final String COLUMN_NAME = "column_name";
    private static final String AS = "AS";
    private static final String _AS_ = SPACE + AS + SPACE;
    private static final String TABLENAME = "tablename";
    private static final String SQL_CONCAT_DOT = "||'.'||";
    private static final String SCHEMANAME = "schemaname";
    private static final String PG_TABLES = "pg_tables";
    private static final String INFORMATION_SCHEMA_COLUMNS = "INFORMATION_SCHEMA.COLUMNS";
    private static final String DROP = "DROP";
    private static final String IF_EXISTS = "IF EXISTS";
    private static final String IF_NOT_EXISTS = "IF NOT EXISTS";

    private static final String CASCADE = "CASCADE";
    private static final String DROP_ = DROP + SPACE;
    private static final String _IF_EXISTS_ = SPACE + IF_EXISTS + SPACE;
    private static final String _IF_NOT_EXISTS_ = SPACE + IF_NOT_EXISTS + SPACE;

    private static final String _CASCADE_ = SPACE + CASCADE + SPACE;
    private static final String COMIT = "COMIT";
    private static final String COMIT_END = COMIT + SEMI_COLON;
    private static final String UNION = "UNION";
    private static final String _UNION_ = SPACE + "UNION" + SPACE;
    private static final String _AND_ = SPACE + "AND" + SPACE;
    private static final String INSERT_INTO_ = "INSERT INTO" + SPACE;
    private static final String SELECT = "SELECT";
    private static final String SELECT_ = "SELECT" + SPACE;
    private static final String _SELECT_ = SPACE + SELECT_;
    private static final String __SELECT_ = NEWLINE + _SELECT_;
    private static final String FROM = "FROM";
    private static final String FROM_ = FROM + SPACE;
    private static final String _FROM_ = SPACE + FROM_;
    private static final String CREATE = "CREATE";
    private static final String CREATE_ = CREATE + SPACE;
    private static final String LIKE = "LIKE";
    private static final String _LIKE_ = SPACE + LIKE + SPACE;

    private static final String WHERE = "WHERE";
    private static final String WHERE_ = WHERE + SPACE;
    private static final String _WHERE_ = SPACE + WHERE_;
    public static final String NULL = "null";
    public static final String WITH_AUTOVACUUM_FALSE = "" + FormatSQL.WITH_NO_VACUUM + "";

    public static final String COLLATE_C = "COLLATE pg_catalog.\"C\"";
    private static final String TEXT = "text";
    public static final String TEXT_COLLATE_C = TEXT + SPACE + COLLATE_C;
    public static final String IS_NOT_DISTINCT_FROM = "IS NOT DISTINCT FROM";
    public static final String NO_VACUUM = " (autovacuum_enabled = false, toast.autovacuum_enabled = false) ";
    public static final String WITH_NO_VACUUM = " WITH" + NO_VACUUM;
    public static final String defaultSeparator = ";\n";
    public static final String _TMP = "$tmp$";
    public static final String _REGEX_TMP = "\\$tmp\\$";
    private static final String _ID = "id_";
    public static final String PARALLEL_WORK_MEM = "24MB";
    public static final String SEQUENTIAL_WORK_MEM = "32MB";
    private static final String SQL_SEPARATOR = "chr(1)";

    public static final int _LONGUEUR_MAXIMALE_NOM_TABLE = 63;
    public static final int _LONGUEUR_MAXIMALE_NOM_TABLE_RAW = _LONGUEUR_MAXIMALE_NOM_TABLE - 10 - 4;
    private static final String MAX_VALUE_BIGINT_SQL_POSTGRES = new BigInteger("2").pow(63).subtract(BigInteger.ONE)
	    .toString();
    private static final Logger LOGGER = Logger.getLogger(FormatSQL.class);
    public static final boolean DROP_FIRST_FALSE = false;
    public static final boolean DROP_FIRST_TRUE = true;
    public static final int TAILLE_MAXIMAL_BLOC_SQL = 300000;
    public static final int TIME_OUT_SQL_EN_HEURE = 100;
    public static final int TIMEOUT_MAINTENANCE = 600000;

    public static final String EXPRESSION_TYPE_SQL_SEUL = "CASE WHEN lower(data_type)='array' \n THEN replace(replace(replace(ltrim(udt_name,'_'),'int4','int'),'int8','bigint'),'float8','float')||'[]' \n ELSE lower(data_type) \n END ";
    public static final String EXPRESSION_TYPE_SQL = EXPRESSION_TYPE_SQL_SEUL + " AS data_type";
    private static final String FALSE = "FALSE";
    private static final String TRUE = "TRUE";
    private static final String _CASCADE = SPACE + CASCADE;
    private static final String TABLE_NAME = "table_name";

    public enum ObjectType {
	TABLE("TABLE"), VIEW("VIEW"), TEMPORARY_TABLE("TEMPORARY TABLE");
	private String name;

	private ObjectType(String aName) {
	    this.name = aName;
	}

	@Override
	public String toString() {
	    return this.name;
	}
    }

    public static String end(String[] separator) {
	String end;
	if (separator == null || separator.length == 0) {
	    end = defaultSeparator;
	} else {
	    end = separator[0];
	}
	return end;
    }

    /**
     * Replace les rubriques entre crochet par des vraies valeurs
     *
     * @param req
     * @param args
     * @return
     */
    public static String getRequete(String req, String... args) {
	String s = req;
	String exp;
	for (int i = 0; i < args.length; i++) {
	    exp = OPENING_BRACE + i + CLOSING_BRACE;
	    s = s.replace(exp, args[i]);
	}
	return s;
    }

    /**
     * Requête de paramétrage. Utile pour faire des jointures sur des colonnes
     * NULLABLE.
     *
     * @param isTransformNullEquals
     * @return la requête qui permet de valoriser l'expression {@code machin = NULL}
     *         à une des deux valeurs de vérité {@code true} ou {@code false}.
     */
    public static final String setTransformNullEquals(boolean isTransformNullEquals) {
	return "set transform_null_equals=" + isTransformNullEquals + ";";
    }

    public static String dropUniqueTable(String aTableName) {
	return dropUniqueObject(ObjectType.TABLE, aTableName);
    }

    public static String dropUniqueView(String aTableName) {
	return dropUniqueObject(ObjectType.VIEW, aTableName);
    }

    public static String dropUniqueObject(ObjectType tableOrView, String anObjectName) {
	StringBuilder sql = new StringBuilder(
		NEWLINE + DROP_ + tableOrView + _IF_EXISTS_ + anObjectName + _CASCADE_ + SEMI_COLON);
	return sql.toString();
    }

    public static String dropViews(Collection<String> someViewNames) {
	return dropObjects(ObjectType.VIEW, someViewNames);
    }

    public static String dropTables(Collection<String> someTableNames) {
	return dropObjects(ObjectType.TABLE, someTableNames);
    }

    public static String dropObjects(ObjectType tableOrView, Collection<String> someTableNames) {
	StringBuilder returned = new StringBuilder();
	for (String nomTable : someTableNames) {
	    returned.append(dropUniqueObject(tableOrView, nomTable) + NEWLINE + COMIT_END);
	}
	return returned.toString();
    }

    public static String dropTable(String tableName, String... separator) {
	StringBuilder returned = new StringBuilder();
	returned.append(DROP_ + ObjectType.TABLE + _IF_EXISTS_ + tableName + _CASCADE_ + end(separator));
	return returned.toString();
    }

    /**
     * Revoi le sch�ma d'une table
     * 
     * @param table
     * @return
     */
    public static String getSchema(String table) {
	return ManipString.substringBeforeFirst(table, DOT);
    }

    /**
     * Revoi le nom court d'une table
     * 
     * @param table
     * @return
     */
    public static String getName(String table) {
	return ManipString.substringAfterFirst(table, DOT);
    }

    public static String tableExists(String table, String... separator) {
	String tableSchema = ManipString.substringBeforeFirst(table, DOT);
	String tableName = ManipString.substringAfterLast(table, DOT);
	StringBuilder requete = new StringBuilder();
	requete.append(SELECT_ + SCHEMANAME + SQL_CONCAT_DOT + TABLENAME + _AS_ + "table_name");
	requete.append(NEWLINE_TABULATION + FROM_ + PG_TABLES);
	requete.append(
		NEWLINE_TABULATION + WHERE_ + TABLENAME + EQUALS + QUOTE + tableName.toLowerCase() + QUOTE + SPACE);
	if (table.contains(DOT)) {
	    requete.append(_AND_ + SCHEMANAME + EQUALS + QUOTE + tableSchema.toLowerCase() + QUOTE + SPACE);
	}
	requete.append(end(separator));

	return requete.toString();
    }

    /**
     * Pour récupérer la liste des colonnes d'une table
     *
     * @param table
     * @return
     */

    public static String listeColonne(String table, String... listeAttribut) {
	String tableSchema = ManipString.substringBeforeFirst(table, DOT);
	String tableName = ManipString.substringAfterLast(table, DOT);
	StringBuilder requete = new StringBuilder();
	requete.append(SELECT_ + COLUMN_NAME + _AS_ + COLUMN_NAME);
	for (int i = 0; i < listeAttribut.length; i++) {
	    requete.append(COMMA + SPACE + listeAttribut[i]/* + " AS " + listeAttribut[i] */);
	}
	requete.append(NEWLINE);
	requete.append(_FROM_ + INFORMATION_SCHEMA_COLUMNS);
	requete.append(
		NEWLINE_TABULATION + WHERE_ + TABLE_NAME + EQUALS + QUOTE + tableName.toLowerCase() + QUOTE + SPACE);
	if (table.contains(DOT)) {
	    requete.append(_AND_ + TABLE_SCHEMA + EQUALS + QUOTE + tableSchema.toLowerCase() + "'; ");
	}
	return requete.toString();
    }

    /**
     * Pour récupérer la liste des colonnes d'une table rapidement
     *
     * @param table
     * @return
     */
    public static String listeColonneByHeaders(String table) {
	StringBuilder requete = new StringBuilder();
	requete.append(SELECT_ + STAR + _FROM_ + table + _WHERE_ + FALSE + SEMI_COLON);
	return requete.toString();
    }

    /**
     * Requête de sélection de la liste des colonnes des tables métier associée à
     * une norme
     *
     * @param listeTable
     * @return
     */
    public static String listeColonneTableMetierSelonFamilleNorme(String anEnvironnement, String idFamille) {
	return new StringBuilder(SELECT_ + _DISTINCT_ + "nom_variable_metier, type_variable_metier\n")//
		.append(NEWLINE_TABULATION + FROM_ + anEnvironnement + "_mod_variable_metier\n")//
		.append(NEWLINE_TABULATION + _WHERE_ + " lower(id_famille)=lower('" + idFamille + "')").toString();
    }

    /**
     * ecrit une clause de selection in
     *
     * @param fields
     * @param query
     * @return
     */
    public static String writeInQuery(String fields, String query) {
	StringBuilder listIn = new StringBuilder();
	listIn.append(SPACE + fields + " IN (");
	listIn.append(SELECT_ + " distinct " + fields + FROM_);
	listIn.append(query);
	listIn.append(") q1 ");
	listIn.append(") ");
	return listIn.toString();
    }

    /**
     * Configuration de la base de données pour des petites requetes
     *
     * @param defaultSchema
     * @return requete
     */

    public static String modeParallel(String defaultSchema) {
	return "set enable_nestloop=on; set enable_mergejoin=off; set enable_hashjoin=on; set enable_material=off; set enable_seqscan=off;"
		+ "set work_mem='" + PARALLEL_WORK_MEM + "'; set maintenance_work_mem='" + PARALLEL_WORK_MEM
		+ "'; set temp_buffers='" + PARALLEL_WORK_MEM + "'; set statement_timeout="
		+ (3600000 * TIME_OUT_SQL_EN_HEURE) + "; "
		+ "set from_collapse_limit=10000; set join_collapse_limit=10000;"
		+ "set enable_hashagg=on; set search_path=" + defaultSchema.toLowerCase() + ", public;";
    }

    /**
     * Configuration de la base de données pour des traitements lourds
     *
     * @param defaultSchema
     * @param work_mem
     * @return requete
     */
    public static String modeSequential(String defaultSchema) {
	StringBuilder requete = new StringBuilder(
		"set enable_nestloop=on; set enable_mergejoin=off; set enable_hashjoin=on; "
			+ " set enable_seqscan=off; " + " set from_collapse_limit=100; set join_collapse_limit=100;"
			+ " set statement_timeout=" + (3600000 * TIME_OUT_SQL_EN_HEURE) + ";" + " set search_path="
			+ defaultSchema.toLowerCase() + ", public;");
	return requete.toString();
    }

    /**
     * timeOut
     */
    public static String setTimeOutMaintenance() {
	return "set statement_timeout=" + TIMEOUT_MAINTENANCE + "; COMMIT; ";
    }

    public static String resetTimeOutMaintenance() {
	return "reset statement_timeout; COMMIT; ";
    }

    /**
     * essaie d'exectuer une requete et si elle n'échoue ne fait rien
     */
    public static String tryQuery(String query) {
	return "do $$ begin " + query + " exception when others then end; $$; ";
    }

    /**
     * Retourne la requête d'insertion dans {@code table} des valeurs {@code values}
     * de type {@code columnTypes} dans les colonnes {@code columnNames}, en
     * respectant cet ordre.
     *
     * <br/>
     *
     * Aucun contrôle n'est fait sur la taille des paramètres en entrée.
     *
     * @param table
     * @param columnNames
     * @param columnTypes
     * @param values
     * @return
     */

    public static String insertInto(String table, List<String> columnNames, List<String> columnTypes,
	    List<String> values) {
	StringBuilder sb = new StringBuilder(INSERT_INTO_ + table + " (");
	for (int i = 0; i < columnNames.size(); i++) {
	    if (i != 0) {
		sb.append(_COMMA_);
	    }
	    sb.append(columnNames.get(i));
	}
	sb.append(CLOSING_PARENTHESIS + _VALUES_ + OPENING_PARENTHESIS);
	for (int i = 0; i < values.size(); i++) {
	    if (i != 0) {
		sb.append(", ");
	    }
	    sb.append(//
		    (values.get(i) == null ? NULL : //
			    (QUOTE + values.get(i).replace(QUOTE, QUOTE_QUOTE) + QUOTE)) + "::" + columnTypes.get(i));
	}
	sb.append(CLOSING_PARENTHESIS);
	return sb.toString();
    }

    /**
     * Met entre cote ou renvoie null (comme pour un champ de base de donnée)
     *
     * @param t
     * @return
     */
    public static String cast(String t) {
	if (t == null) {
	    return "null";
	} else {
	    return "'" + t + "'";
	}
    }

    /**
     * Lance un vacuum d'un certain type sur une table
     * 
     * @param table
     * @param type
     * @return
     */
    public static String vacuumSecured(String table, String type) {
	return VACUUM_ + type + SPACE + table + ";\n";
    }

    /**
     * Recopie une table à l'identique; ceci pour éviter l'hérésie du vacuum
     *
     * @param table
     * @param where
     * @param triggersAndIndexes
     * @return
     */

    public static StringBuilder rebuildTableAsSelectWhere(String table, String where, String... triggersAndIndexes) {
	String tableRebuild = temporaryTableName(table, "RB");
	StringBuilder requete = new StringBuilder();
	requete.append("set enable_nestloop=off; ");
	requete.append(NEWLINE + DROP_ + ObjectType.TABLE + _IF_EXISTS_ + tableRebuild + _CASCADE_ + SEMI_COLON);
	requete.append(NEWLINE + SPACE);
	requete.append(CREATE_);
	if (!table.contains(DOT)) {
	    requete.append(TEMPORARY_);
	}
	requete.append(ObjectType.TABLE + SPACE + tableRebuild + SPACE + FormatSQL.WITH_NO_VACUUM + AS + _SELECT_ + STAR
		+ _FROM_ + table + " a " + _WHERE_ + where + SEMI_COLON);
	requete.append(NEWLINE + DROP_ + ObjectType.TABLE + _IF_EXISTS_ + table + _CASCADE_ + SEMI_COLON);
	requete.append(NEWLINE + _ALTER_ + " " + ObjectType.TABLE + " " + tableRebuild + " rename to "
		+ ManipString.substringAfterFirst(table, DOT) + SEMI_COLON);
	requete.append("set enable_nestloop=on; ");
	for (int i = 0; i < triggersAndIndexes.length; i++) {
	    requete.append(triggersAndIndexes[i]);
	}
	return requete;
    }

    /**
     * Création d'une table à partir d'une map contenant les nom des colonnes et les
     * types SQL
     * 
     * @param aNomTable
     * @param aMapColonneToType
     * @return
     */
    public static String createFromSqlModele(String aNomTable, Map<String, String> aMapColonneToType) {
	StringBuilder requete = new StringBuilder();
	requete.append(FormatSQL.dropUniqueTable(aNomTable));
	requete.append(NEWLINE);
	requete.append(CREATE_);
	requete.append(ObjectType.TABLE + aNomTable);
	requete.append(NEWLINE + OPENING_PARENTHESIS);
	requete.append(NEWLINE + aMapColonneToType.keySet().stream().map(t -> t + SPACE + aMapColonneToType.get(t))
		.collect(Collectors.joining(NEWLINE + COMMA + SPACE)));
	requete.append(NEWLINE + CLOSING_PARENTHESIS);
	requete.append(NEWLINE + SEMI_COLON);
	return requete.toString();
    }

    public static String createAsSelectFrom(String aNomTableCible, String aNomTableSource, boolean dropFirst) {
	return createAsSelectFrom(aNomTableCible, aNomTableSource, STAR, null, dropFirst);
    }

    /**
     *
     * @param aNomTableCible
     * @param aNomTableSource
     * @param clauseWhere
     * @param dropFirst
     * @return
     */
    public static String createAsSelectFrom(String aNomTableCible, String aNomTableSource, String clauseWhere,
	    boolean dropFirst) {
	return createAsSelectFrom(aNomTableCible, aNomTableSource, STAR, clauseWhere, dropFirst);
    }

    /**
     *
     * Méthode de création d'une requête
     * {@code CREATE TABLE aNomTableCible AS SELECT columns FROM aNomTableSource WHERE clauseWhere;}
     * , éventuellement précédée d'un {@code DROP}
     *
     * @param aNomTableCible
     * @param aNomTableSource
     * @param columns
     * @param clauseWhere
     * @param dropFirst
     * @return
     */
    public static String createAsSelectFrom(String aNomTableCible, String aNomTableSource, String columns,
	    String clauseWhere, boolean dropFirst) {

	// Si la table contient un . on est dans un schema, sinon c'est du temps
	if (aNomTableCible.contains(DOT)) {
	    return createObjectAsSelectFrom(ObjectType.TABLE, aNomTableCible, aNomTableSource, columns, clauseWhere,
		    dropFirst);
	} else {
	    return createObjectAsSelectFrom(ObjectType.TEMPORARY_TABLE, aNomTableCible, aNomTableSource, columns,
		    clauseWhere, dropFirst);
	}

    }

    public static String createViewAsSelectFrom(String aNomTableCible, String aNomTableSource) {
	return createObjectAsSelectFrom(ObjectType.VIEW, aNomTableCible, aNomTableSource, STAR, null, false);
    }

    /**
     * 
     * @param tableOrView
     * @param aNomTableCible
     * @param aNomTableSource
     * @param columns
     * @param clauseWhere
     * @param dropFirst
     * @return
     */
    private static String createObjectAsSelectFrom(ObjectType tableOrView, String aNomTableCible,
	    String aNomTableSource, String columns, String clauseWhere, boolean dropFirst) {
	StringBuilder requete = new StringBuilder();
	if (dropFirst) {
	    requete.append(dropUniqueObject(tableOrView, aNomTableCible));
	}
	String whereCondition = ((StringUtils.isBlank(clauseWhere)) ? EMPTY : _WHERE_ + clauseWhere);
	/*
	 * Attention ! Les vues ne peuvent être créées avec un autovacuum_enabled
	 */
	String vacuumIfNeeded = tableOrView.equals(ObjectType.TABLE) ? SPACE + FormatSQL.WITH_NO_VACUUM : EMPTY;
	String orReplaceForViewsOnly = tableOrView.equals(ObjectType.VIEW) ? "OR REPLACE " : EMPTY;
	requete.append(NEWLINE + SPACE + CREATE_ + orReplaceForViewsOnly + tableOrView + " " + aNomTableCible
		+ vacuumIfNeeded + _AS_);
	requete.append(__SELECT_ + columns + _FROM_ + aNomTableSource);
	requete.append(whereCondition + SEMI_COLON);
	return requete.toString();
    }

    /**
     *
     * Méthode de création d'une requête
     * {@code CREATE TABLE aNomTableCible AS SELECT columns FROM aNomTableSource WHERE clauseWhere;}
     * , éventuellement précédée d'un {@code DROP}
     *
     * @param aNomTableCible
     * @param aNomTableSource
     * @param columns
     * @param clauseWhere
     * @param dropFirst
     * @return
     */
    public static String createTempAsSelectFrom(String aNomTableCible, String aNomTableSource, String columns,
	    String clauseWhere, boolean dropFirst) {
	StringBuilder requete = new StringBuilder();
	if (dropFirst) {
	    requete.append(dropUniqueTable(aNomTableCible));
	}
	String whereConditions = ((StringUtils.isBlank(clauseWhere)) ? EMPTY : _WHERE_ + clauseWhere);

	requete.append(NEWLINE + CREATE_ + ObjectType.TEMPORARY_TABLE + SPACE + aNomTableCible + SPACE
		+ FormatSQL.WITH_NO_VACUUM + _AS_);
	requete.append(__SELECT_ + columns + _FROM_ + aNomTableSource);
	requete.append(whereConditions + SEMI_COLON);
	return requete.toString();
    }

    /**
     *
     * @param aNomTableCible
     * @param aNomTableSource
     * @param clauseWhere
     *            le WHERE n'y est pas, je le rajouterai tout seul merci.
     * @return
     */
    public static String createAsSelectFrom(String aNomTableCible, String aNomTableSource, String columns,
	    String clauseWhere) {
	return createAsSelectFrom(aNomTableCible, aNomTableSource, columns, clauseWhere, DROP_FIRST_FALSE);
    }

    /**
     *
     * @param aNomTableCible
     * @param aNomTableSource
     * @return la requête de copie simple de la table {@code aNomTableCible}
     */
    public static String createAsSelectFrom(String aNomTableCible, String aNomTableSource) {
	return createAsSelectFrom(aNomTableCible, aNomTableSource, EMPTY, false);
    }

    /**
     *
     * @param aNomTableCible
     * @param aNomTableSource
     * @param clauseWhere
     *            le WHERE n'y est pas, je le rajouterai tout seul merci.
     * @return
     */
    public static String createAsSelectFrom(String aNomTableCible, String aNomTableSource, String clauseWhere) {
	return createAsSelectFrom(aNomTableCible, aNomTableSource, STAR, clauseWhere, DROP_FIRST_FALSE);
    }

    /**
     *
     * @param aNomTableCible
     * @param aNomTableSource
     * @param clauseWhere
     *            le WHERE n'y est pas, je le rajouterai tout seul merci.
     * @return
     */
    public static String createTempAsSelectFrom(String aNomTableCible, String aNomTableSource, String clauseWhere) {
	return createTempAsSelectFrom(aNomTableCible, aNomTableSource, STAR, clauseWhere, DROP_FIRST_FALSE);
    }

    public static String createIfNotExistsAsSelectFrom(String aNomTableCible, String aNomTableSource,
	    String clauseWhere) {
	StringBuilder requete = new StringBuilder();
	requete.append(CREATE_ + ObjectType.TABLE + _IF_NOT_EXISTS_ + aNomTableCible + WITH_NO_VACUUM + _AS_ + _SELECT_
		+ STAR + _FROM_ + aNomTableSource + _WHERE_ + clauseWhere + SEMI_COLON);
	return requete.toString();
    }

    /**
     * 
     * @param someCols
     *            : a collection of the needed columns
     * @param aOutputTableName
     *            : the created table name
     * @param aTableSourceName
     *            : the table from which the data are copied
     * @param clauseWhere
     *            : some where clauses. Just list the condition, no and between
     * @return a query to create the aOutputTableName looking like :
     * 
     *         CREATE TABLE IF NOT EXITS aOutputTableName </br>
     *         WITH (autovacuum_enabled = false, toast.autovacuum_enabled = false)
     *         </br>
     *         AS SELECT someCols </br>
     *         FROM aOutputTableName </br>
     *         WHERE clauseWhere </br>
     */
    public static String createIfNotExistsAsSelectFrom(Collection<String> someCols, String aOutputTableName,
	    String aTableSourceName, String... clauseWhere) {
	StringBuilder requete = new StringBuilder();
	requete.append(
		CREATE_ + ObjectType.TABLE + _IF_NOT_EXISTS_ + aOutputTableName + FormatSQL.WITH_NO_VACUUM + _AS_);
	requete.append(getSomeReccordFromATable(someCols, aTableSourceName, clauseWhere));
	return requete.toString();
    }

    public static StringBuilder dupliquerVers(List<String> sources, List<String> targets) {
	StringBuilder returned = new StringBuilder();
	for (int i = 0; i < sources.size(); i++) {
	    returned.append(dupliquerVers(sources.get(i), targets.get(i)));
	}
	return returned;
    }

    public static StringBuilder dupliquerVers(String source, String target) {
	return new StringBuilder(createAsSelectFrom(target, source, true));
    }

    public static StringBuilder dupliquerVers(List<String> sources, List<String> targets, String clauseWhere) {
	StringBuilder returned = new StringBuilder();
	for (int i = 0; i < sources.size(); i++) {
	    returned.append(dupliquerVers(sources.get(i), targets.get(i), clauseWhere));
	}
	return returned;
    }

    public static StringBuilder dupliquerVers(String source, String target, String clauseWhere) {
	return new StringBuilder(createAsSelectFrom(target, source, clauseWhere, true));

    }

    public static final String listeTablesExistantes(List<String> tables) {
	List<String> liste = new ArrayList<>();
	for (int i = 0; i < tables.size(); i++) {
	    liste.add(tableExists(tables.get(i)).toString());
	}
	return Format.untokenize(liste, NEWLINE + "UNION ");
    }

    public static final String listeTablesExistantes(String... tables) {
	return listeTablesExistantes(Arrays.asList(tables));
    }

    private static final StringBuilder tableExists(String table) {
	String tokenJoin = table.contains(DOT) ?
	/*
	 * Le nom de la table contient "." ? Il est précédé du nom du schéma.
	 */
		" INNER " + _JOIN_ + "pg_namespace ON pg_class.relnamespace = pg_namespace.oid" :
		/*
		 * Sinon, aucune jointure sur le nom de schéma.
		 */
		EMPTY;
	String tokenCond = table.contains(DOT) ?
	/*
	 * Le nom de la table contient "." ? Il est précédé du nom de schéma.
	 */
		"lower(pg_namespace.nspname||'.'||pg_class.relname)" :
		/*
		 * Sinon, la condition d'égalité porte sur le nom de la table
		 */
		"pg_class.relname";
	StringBuilder requete = new StringBuilder(SELECT_ + " " + _DISTINCT_ + " '" + table + "' table_existe");
	requete.append(NEWLINE_TABULATION + FROM_ + "pg_class" + tokenJoin);
	requete.append(NEWLINE_TABULATION + WHERE_ + tokenCond + " = lower('" + table + "')");
	return requete;
    }

    /**
     * @param table
     * @return
     */

    public static StringBuilder isTableExists(String table) {
	String tokenJoin = table.contains(DOT) ?
	/*
	 * Le nom de la table contient "." ? Il est précédé du nom du schéma.
	 */
		" INNER " + _JOIN_ + "pg_namespace ON pg_class.relnamespace = pg_namespace.oid" :
		/*
		 * Sinon, aucune jointure sur le nom de schéma.
		 */
		EMPTY;
	String tokenCond = table.contains(DOT) ?
	/*
	 * Le nom de la table contient "." ? Il est précédé du nom de schéma.
	 */
		"lower(pg_namespace.nspname||'.'||pg_class.relname)" :
		/*
		 * Sinon, la condition d'égalité porte sur le nom de la table
		 */
		"pg_class.relname";
	StringBuilder requete = new StringBuilder(
		SELECT_ + " CASE WHEN count(1)>0 THEN TRUE ELSE FALSE END table_existe\n");
	requete.append(NEWLINE_TABULATION + FROM_ + "pg_class" + tokenJoin);
	requete.append(NEWLINE_TABULATION + WHERE_ + tokenCond + " = lower('" + table + "')");
	return requete;
    }

    /**
     * Ajoute un suffixe de table temporaire au nom de table {@code aName}
     *
     * @param aName
     * @return
     */
    public static final String temporaryTableName(String aName) {
	String newName = aName.split(_REGEX_TMP)[0];
	/**
	 * Impératif : permet de s'assurer que les noms des tables ne causeront aucune
	 * collision 2 milliseconds : pour éviter les problèmes d'arrondis
	 */
	try {
	    Thread.sleep(2);
	} catch (InterruptedException e) {
	    LoggerHelper.error(LOGGER, "sleep(int, int)", e);
	}
	// on met la date du jour dans le nom de la table
	String l = Long.toString(System.currentTimeMillis());
	// on prend que les 10 derniers chiffres (durrée de vie : 6 mois)
	l = l.substring(l.length() - 10);
	// on inverse la chaine de caractere pour avoir les millisecondes en
	// premier en cas de troncature
	l = new StringBuffer(l).reverse().toString();
	// troncature par la fin
	l = l.substring(0, l.length() > _LONGUEUR_MAXIMALE_NOM_TABLE ? _LONGUEUR_MAXIMALE_NOM_TABLE : l.length());
	return new StringBuilder(newName).append(_TMP).append(l).append(DOLLAR).append(randomNumber(4)).toString();
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
    public static final String temporaryTableName(String aName, String suffix) {
	String newName = aName.split(_REGEX_TMP)[0];
	return temporaryTableName(newName + UNDERSCORE + suffix);
    }

    /**
     *
     * @return Un nombre aléatoire d'une certaine précision
     */
    public static final String randomNumber(int precision) {
	String rn = Integer.toString(((int) Math.floor((Math.random() * (Math.pow(10, precision))))));
	return ManipString.padLeft(rn, "0", precision);
    }

    public static final String id(String suffix) {
	return _ID + suffix;
    }

    /**
     * Mise à niveau d'une table qui a des colonnes manquantes
     *
     * @param table
     * @param listeColonne
     * @return
     */
    public static String addColonnePourGenericBeanData(String table, ArrayList<ArrayList<String>> listeColonne) {
	return addColonne(table, listeColonne);
    }

    /**
     * Mise à niveau d'une table qui a des colonnes manquantes
     *
     * @param table
     * @param listeColonne
     * @return
     */
    public static String addColonnePourGenericBean(String table, ArrayList<ArrayList<String>> listeColonne) {
	return addColonne(table, listeColonne.subList(ARRAY_THIRD_COLUMN_INDEX, listeColonne.size()));
    }

    /**
     * Mise à niveau d'une table qui a des colonnes manquantes
     *
     * @param table
     * @param listeColonne
     * @return
     */

    public static <T extends List<String>> String addColonne(String table, List<T> listeColonne) {
	StringBuilder requete = new StringBuilder();
	String nomColonne;
	String typeColonne;
	// élement (1er noms, 2e types)
	if (CollectionUtils.isEmpty(listeColonne)) {
	    return requete.toString();
	}
	requete.append(_ALTER_ + ObjectType.TABLE + SPACE + table + SPACE);
	for (int i = ARRAY_FIRST_COLUMN_INDEX; i < listeColonne.size(); i++) {
	    nomColonne = listeColonne.get(i).get(0);
	    typeColonne = listeColonne.get(i).get(1);
	    if (i > 0) {
		requete.append(_COMMA_);
	    }
	    requete.append("ADD COLUMN " + nomColonne + SPACE + typeColonne + SPACE);
	    if (typeColonne.equals(TEXT)) {
		requete.append(SPACE + COLLATE_C);
	    }
	}
	requete.append(SEMI_COLON);
	return requete.toString();
    }

    public static String dropColonne(String table, String colonne) {
	StringBuilder requete = new StringBuilder();
	requete.append(_ALTER_ + " " + ObjectType.TABLE + SPACE + table + SPACE);
	requete.append(DROP_ + " COLUMN " + colonne);
	requete.append(SEMI_COLON);
	return requete.toString();
    }

    /**
     * Détection des colonnes d'une table {@code tableIn} qui ne sont pas encore
     * dans l'autre table {@code tableOut}
     *
     * @param tableIn
     *            table servant de référence
     * @param tableOut
     * @return
     */

    public static String listAjoutColonne(String tableIn, String tableOut) {
	// scission du nom en entrée en schema + nom de table
	String tableSchemaIn = ManipString.substringBeforeFirst(tableIn, DOT);
	String tableNameIn = ManipString.substringAfterLast(tableIn, DOT);
	String tableSchemaOut = ManipString.substringBeforeFirst(tableOut, DOT);
	String tableNameOut = ManipString.substringAfterLast(tableOut, DOT);
	StringBuilder requete = new StringBuilder();
	requete.append("WITH ");
	requete.append("def AS (	" + _SELECT_ + " column_name, data_type ");
	requete.append(NEWLINE_TABULATION + FROM_ + INFORMATION_SCHEMA_COLUMNS);
	requete.append(NEWLINE_TABULATION + _WHERE_ + " TABLE_NAME='" + tableNameOut.toLowerCase() + "' ");
	if (tableOut.contains(DOT)) {
	    requete.append("	AND TABLE_schema='" + tableSchemaOut.toLowerCase() + "' ");
	}
	requete.append("), ");
	requete.append("temp AS (	" + _SELECT_ + " column_name, udt_name, data_type");
	requete.append(NEWLINE_TABULATION + FROM_ + INFORMATION_SCHEMA_COLUMNS);
	requete.append(NEWLINE_TABULATION + _WHERE_ + " TABLE_NAME='" + tableNameIn.toLowerCase() + "' ");
	if (tableIn.contains(DOT)) {
	    requete.append(NEWLINE_TABULATION + "AND TABLE_schema='" + tableSchemaIn.toLowerCase() + "'");
	}
	requete.append(") ");
	requete.append(SELECT_
		+ " column_name, ltrim(udt_name,'_')||(CASE WHEN lower(data_type)='ARRAY' THEN '[]' ELSE '' END) AS data_type ");
	requete.append(FROM_ + "temp ");
	requete.append(_WHERE_ + " NOT EXISTS (" + _SELECT_ + " 1 " + FROM_ + "def " + _WHERE_
		+ " temp.column_name=def.column_name); ");
	return requete.toString();
    }

    /**
     * Verrouille une table
     *
     * @param tableName
     * @return
     */
    public static String lock(String... tableName) {
	StringBuilder requete = new StringBuilder();
	for (int i = 0; i < tableName.length; i++) {
	    requete.append("LOCK " + ObjectType.TABLE + " " + tableName[i] + " IN ACCESS EXCLUSIVE MODE;\n");
	}
	return requete.toString();
    }

    /**
     * converti une chaine de caractere pour etre mise en parametre d'un sql si
     * c'est vide, ca devient "null" quote devient quote quote
     *
     * @param val
     * @return
     */
    public static String textToSql(String val) {
	if (val == null || val.trim().equals(EMPTY) || val == NULL) {
	    return NULL;
	} else {
	    return QUOTE + val.replace(QUOTE, QUOTE_QUOTE) + QUOTE;
	}
    }

    public static String textToSqlNoNull(String val) {
	if (val == null || val.trim().equals(EMPTY)) {
	    return QUOTE_QUOTE;
	} else {
	    return QUOTE + val.replace(QUOTE, QUOTE_QUOTE) + QUOTE;
	}
    }

    public static String int8ToSqlNoNull(String val) {
	if (val == null || val.trim().equals(EMPTY)) {
	    return ZERO_STRING;
	} else {
	    return QUOTE + val.replace(QUOTE, QUOTE_QUOTE) + QUOTE;
	}
    }

    public static String boolToSqlNoNull(String val, String escape) {
	if (val == null || val.trim().equals(EMPTY)) {
	    return escape;
	} else {
	    return QUOTE + val.replace(QUOTE, QUOTE_QUOTE) + QUOTE;
	}
    }

    public static String expressionJointure(String lefty, List<String> variablesLeft, String righty,
	    List<String> variablesRight, String indent, String operator) {
	return expressionJointure(lefty, variablesLeft, righty, variablesRight, indent, operator, null);
    }

    /**
     *
     * @param lefty
     * @param variablesLeft
     * @param righty
     * @param variablesRight
     * @param indent
     * @param operator
     * @param cast
     * @return l'expression de jointure entre les tables {@code lefty} et
     *         {@code righty} sur l'égalité (ou autre opérateur {@code operator} )
     *         des variables de {@code variablesLeft} et {@code variablesRight}.
     */
    public static String expressionJointure(String lefty, List<String> variablesLeft, String righty,
	    List<String> variablesRight, String indent, String operator, String cast) {

	return expressionTermeATerme(lefty, variablesLeft, righty, variablesRight, indent, operator, cast, _AND_);
    }

    /**
     *
     * @param lefty
     * @param variablesLeft
     * @param righty
     * @param variablesRight
     * @param indent
     * @param operator
     * @param cast
     * @return l'expression de mise à jour des variables {@code variablesLeft} de
     *         {@code lefty} à partir des variables {@code variablesRight} de
     *         {@code righty}.
     */
    public static String expressionEgaliteUpdate(String lefty, List<String> variablesLeft, String righty,
	    List<String> variablesRight, String indent, String cast) {
	return expressionTermeATerme(lefty, variablesLeft, righty, variablesRight, indent, EQUALS, cast, COMMA);
    }

    public static String expressionTermeATerme(String lefty, List<String> variablesLeft, String righty,
	    List<String> variablesRight, String indent, String operator, String cast, String separator) {
	LoggerHelper.trace(LOGGER, "lefty =", lefty, "variablesLeft =", variablesLeft, "righty =", righty,
		"variablesRight =", variablesRight, "indent =", indent, "operator =", operator, "cast =", cast,
		"separator =", separator);
	String token = (StringUtils.isBlank(cast) ? EMPTY : "::" + cast);
	StringBuilder returned = new StringBuilder();
	for (int i = 0; i < variablesLeft.size(); i++) {
	    if (i > 0) {
		returned.append(NEWLINE + indent + separator + SPACE);
	    }
	    if (StringUtils.isNotBlank(lefty)) {
		returned.append(lefty + DOT);
	    }
	    returned.append(variablesLeft.get(i) + token + SPACE + operator + SPACE);
	    if (StringUtils.isNotBlank(righty)) {
		returned.append(righty + DOT);
	    }
	    returned.append(variablesRight.get(i) + token);
	}
	return returned.toString();
    }

    /**
     * Pour faire des jointures même avec des null, on enveloppe les variables dans
     * row
     *
     * @param lefty
     *            nom de la table de gauche
     * @param variablesLeft
     *            nom des variables de la table de gauche sur lesqelles on fait la
     *            jointure
     * @param righty
     *            nom de la table de droite
     * @param variablesRight
     *            nom des variables de la table de droite sur lesqelles on fait la
     *            jointure
     * @param indent
     *            indentation pour la mise en forme dans les log
     * @param operator
     *            operateur de comparaison pour la jointure
     * @return
     */
    public static String expressionJointureRow(String lefty, List<String> variablesLeft, String righty,
	    List<String> variablesRight, String indent, String operator) {
	StringBuilder returned = new StringBuilder();
	returned.append(expressionRow(lefty, variablesLeft)).append(operator)
		.append(expressionRow(righty, variablesRight));
	return returned.toString();
    }

    /**
     * Fait un row d'une liste de variable
     *
     * @param table
     * @param variables
     * @return
     */
    public static String expressionRow(String table, List<String> variables) {
	StringBuilder returned = new StringBuilder("row(");
	for (int i = 0; i < variables.size(); i++) {
	    if (i > 0) {
		returned.append(_COMMA_);
	    }
	    returned.append(table + DOT + variables.get(i));
	}
	returned.append(")::" + TEXT_COLLATE_C);
	return returned.toString();
    }

    /**
     * arrayRemoveNulls prend une liste et calcule le tableau resultat les null sont
     * exclus du tableau
     *
     * @param table
     * @param variables
     * @return
     */
    public static String arrayRemoveNulls(String table, List<String> variables) {
	StringBuilder returned = new StringBuilder();
	String prefix = EMPTY;
	if (table != null && !table.trim().equals(EMPTY)) {
	    prefix = table + DOT;
	}
	returned.append("string_to_array(rtrim(");
	boolean first = true;
	for (String var : variables) {
	    if (!first) {
		returned.append("||");
	    }
	    returned.append(COALESCE_OPEN + prefix + var + "::text||" + SQL_SEPARATOR + ", '' )");
	    first = false;
	}
	returned.append("," + SQL_SEPARATOR + "), " + SQL_SEPARATOR + CLOSING_PARENTHESIS);
	return returned.toString();
    }

    /**
     * arrayKeepNulls prend une liste et calcule le tableau resultat les null sont
     * inclus dans le tableau
     *
     * @param table
     * @param variables
     * @return
     */

    public static String arrayKeepNulls(String table, List<String> variables) {
	StringBuilder returned = new StringBuilder();
	String prefix = EMPTY;
	if (table != null && !table.trim().equals(EMPTY)) {
	    prefix = table + DOT;
	}
	returned.append("string_to_array(");
	boolean first = true;
	for (String var : variables) {
	    if (!first) {
		returned.append("||" + SQL_SEPARATOR + "||");
	    }
	    returned.append(COALESCE_OPEN + prefix + var + "::text, '' )");
	    first = false;
	}
	returned.append("," + SQL_SEPARATOR + CLOSING_PARENTHESIS);
	return returned.toString();
    }

    /**
     * Pour faire des jointures même avec des null, on enveloppe les variables dans
     * row
     *
     * @param lefty
     *            nom de la table de gauche
     * @param variablesLeft
     *            nom des variables de la table de gauche sur lesqelles on fait la
     *            jointure
     * @param righty
     *            nom de la table de droite
     * @param variablesRight
     *            nom des variables de la table de droite sur lesqelles on fait la
     *            jointure
     * @param indent
     *            indentation pour la mise en forme dans les log
     * @param operator
     *            operateur de comparaison pour la jointure
     * @return
     */

    public static String expressionJointureRowIgnoreCase(String lefty, List<String> variablesLeft, String righty,
	    List<String> variablesRight, String operator) {
	StringBuilder returned = new StringBuilder();
	StringBuilder leftSide = new StringBuilder("upper(row(");
	StringBuilder rightSide = new StringBuilder("upper(row(");
	for (int i = 0; i < variablesLeft.size(); i++) {
	    if (i > 0) {
		leftSide.append(", ");
		rightSide.append(", ");
	    }
	    leftSide.append(lefty + DOT + variablesLeft.get(i));
	    rightSide.append(righty + DOT + variablesRight.get(i));
	}
	leftSide.append(")::" + TEXT_COLLATE_C + CLOSING_PARENTHESIS + SPACE);
	rightSide.append(")::" + TEXT_COLLATE_C + CLOSING_PARENTHESIS + SPACE);
	returned.append(leftSide).append(operator).append(rightSide);
	return returned.toString();
    }

    /**
     * Il est préférable que le nom de la séquence contienne un nom de schema
     *
     * @param nomSequence
     * @return
     */

    public static String createSequenceIfNotExists(String nomSequence) {
	String token = nomSequence.split("\\.")[nomSequence.contains(DOT) ? 1 : 0];
	String schema = nomSequence.contains(DOT) ? nomSequence.split("\\.")[0] : EMPTY;
	StringBuilder returned = new StringBuilder("DO");
	returned.append(NEWLINE + " $$");
	returned.append(NEWLINE + " BEGIN");
	returned.append(NEWLINE_TABULATION + _IF_NOT_EXISTS_ + " (" + _SELECT_ + " 1 ");
	returned.append(NEWLINE_TABULATION + TABULATION + FROM_ + "pg_class ");
	returned.append(NEWLINE_TABULATION + TABULATION + "INNER " + _JOIN_ + "pg_namespace ");
	returned.append(NEWLINE_TABULATION + TABULATION + "ON pg_class.relnamespace = pg_namespace.oid ");
	returned.append(NEWLINE_TABULATION + TABULATION + _WHERE_ + " lower(relname) = lower('" + token + "')");
	if (!schema.isEmpty()) {
	    returned.append(NEWLINE_TABULATION + "AND lower(nspname)=lower('" + schema + "')");
	}
	returned.append(NEWLINE_TABULATION + ")");
	returned.append(NEWLINE_TABULATION + "THEN");
	returned.append(NEWLINE_TABULATION + TABULATION + "EXECUTE '" + CREATE_ + " SEQUENCE " + nomSequence);
	returned.append(NEWLINE_TABULATION + TABULATION + TABULATION + "INCREMENT 1");
	returned.append(NEWLINE_TABULATION + TABULATION + "MINVALUE 1");
	returned.append(NEWLINE_TABULATION + TABULATION + "MAXVALUE " + MAX_VALUE_BIGINT_SQL_POSTGRES);
	returned.append(NEWLINE_TABULATION + TABULATION + "START 1");
	returned.append(NEWLINE_TABULATION + TABULATION + "CACHE 1;';");
	returned.append(NEWLINE_TABULATION + "END IF;");
	returned.append(NEWLINE + " END");
	returned.append(NEWLINE + " $$;");
	return returned.toString();
    }

    public static String safelyViewTableTo(String ancienne, String nouvelle) {
	StringBuilder returned = new StringBuilder();
	returned.append(createViewTo(ancienne, nouvelle));
	return returned.toString();
    }

    public static String safelyRenameTableTo(String ancienne, String nouvelle) {
	StringBuilder returned = new StringBuilder();
	returned.append(dropUniqueTable(nouvelle));
	returned.append(renameTableTo(ancienne, nouvelle));
	return returned.toString();
    }

    public static String renameTableTo(String ancienne, String nouvelle) {
	String token = nouvelle.split("\\.")[nouvelle.contains(DOT) ? 1 : 0];
	return NEWLINE + " " + _ALTER_ + " " + ObjectType.TABLE + " " + ancienne + " RENAME TO " + token + ";";
    }

    public static String dropConstraint(String aTable, String aConstraint) {
	return NEWLINE + " " + _ALTER_ + " " + ObjectType.TABLE + " " + aTable + " " + DROP_ + " CONSTRAINT "
		+ _IF_EXISTS_ + " " + aConstraint + _CASCADE_ + SEMI_COLON;
    }

    public static String createViewTo(String ancienne, String nouvelle) {
	return tryQuery(NEWLINE + " " + CREATE_ + ObjectType.VIEW + nouvelle + _AS_ + _SELECT_ + STAR + _FROM_
		+ ancienne + SEMI_COLON);
    }

    public static StringBuilder createIndex(String nomIndex, String nomTable, List<String> listeColonnes) {
	StringBuilder returned = new StringBuilder();
	returned.append(NEWLINE + " " + CREATE_ + " INDEX " + nomIndex + " ON " + nomTable + " ("
		+ Format.untokenize(listeColonnes, ", ") + ");");
	return returned;
    }

    public static StringBuilder createIndex(String nomIndex, String nomTable, String indexType,
	    List<String> someColonnes) {
	return createIndex(nomIndex, nomTable, indexType, someColonnes, Arrays.asList());
    }

    public static StringBuilder createIndex(String nomIndex, String nomTable, String indexType,
	    List<String> someColonnes, String indexImplementation) {
	return createIndex(nomIndex, nomTable, indexType, someColonnes, IntStream.range(0, someColonnes.size()).boxed()
		.map((i) -> indexImplementation).collect(Collectors.toList()));
    }

    public static String dropIndex(String nomIndex) {
	return NEWLINE + " " + DROP_ + " INDEX " + nomIndex + " ; ";
    }

    /**
     * 
     * @param nomIndex
     *            (optionnel, peut valoir null) le nom de l'index
     * @param nomTable
     *            le nom de la table sur lequel l'index est défini
     * @param indexType
     *            (optionnel, peut valoir null) le type de l'index
     * @param someColonnes
     *            les colonnes sur lesquelles porte l'index
     * @param indexTypes
     *            (optionnel, peut valoir null ou être plus petit que
     *            {@code someColonnes}) l'implémentation de l'index pour chaque
     *            colonne
     * @return {@code CREATE INDEX <nomIndex> ON <nomTable> USING <typeIndex> (someColonnes_1 indexTypes_1 [, ...]);}
     */

    public static StringBuilder createIndex(String nomIndex, String nomTable, String indexType,
	    List<String> someColonnes, List<String> indexTypes) {
	IntFunction<String> iTypes = (i) -> (indexTypes != null) && (indexTypes.size() == someColonnes.size())
		? " " + indexTypes.get(i)
		: EMPTY;
	StringBuilder returned = new StringBuilder();
	returned.append(NEWLINE + " " + CREATE_ + " INDEX");
	if (nomIndex != null) {
	    returned.append(" " + nomIndex);
	}
	returned.append(" ON " + nomTable);
	if (indexType != null) {
	    returned.append(" USING " + indexType);
	}
	returned.append(" (");
	returned.append(IntStream.range(0, someColonnes.size()).boxed()
		.map((i) -> someColonnes.get(i) + iTypes.apply(i)).collect(Collectors.joining(", ")));
	returned.append(CLOSING_PARENTHESIS);
	return returned;
    }

    public static String createSchema(String aNomSchema, String authorization) {
	return NEWLINE + " " + CREATE_ + " SCHEMA " + _IF_NOT_EXISTS_ + " " + aNomSchema
		+ (StringUtils.isBlank(authorization) ? EMPTY : (" AUTHORIZATION " + authorization)) + ";";
    }

    public static StringBuilder deleteSomeLine(String tableName, String... conditions) {
	StringBuilder query = new StringBuilder();
	query.append(NEWLINE + "DELETE " + FROM_);
	query.append(tableName);
	query.append(NEWLINE + _WHERE_);
	query.append(String.join(_AND_, conditions));
	query.append(SEMI_COLON);
	return query;
    }

    /**
     * Ne garde que les séparateur
     *
     * @param tokens
     * @param separator
     * @return
     */
    public static String toNullRow(Collection<?> tokens) {
	return (CollectionUtils.isEmpty(tokens)) ? OPENING_PARENTHESIS + EMPTY + CLOSING_PARENTHESIS
		: OPENING_PARENTHESIS + StringUtils.repeat(COMMA, tokens.size() - 1) + CLOSING_PARENTHESIS;
    }

    /**
     * Pour tester qu'une variable est NULL ou vide (quotequote en SQL)
     *
     * @param aNomVariable
     * @return
     */
    public static String rowEmpty(String aNomVariable) {
	return "row(" + aNomVariable + ")::text IN ('()','(\"\")')";
    }

    /**
     * recrée la table avec son modèle quand la structure de la table est différente
     * du modèle (la table a changée)
     *
     * @param table
     * @param model
     * @return
     */

    public static String replaceIfTableChanged(String table, String model) {
	String tableSchema = ManipString.substringBeforeFirst(table, DOT).toLowerCase();
	String tableName = ManipString.substringAfterFirst(table, DOT).toLowerCase();
	String modelSchema = ManipString.substringBeforeFirst(model, DOT).toLowerCase();
	String modelName = ManipString.substringAfterFirst(model, DOT).toLowerCase();
	StringBuilder returned = new StringBuilder();
	returned.append(NEWLINE + " do $$ ");
	returned.append("DECLARE n integer; ");
	returned.append("BEGIN ");
	returned.append(SELECT_ + " count(1) into n " + FROM_ + "( ");
	returned.append("( ");
	returned.append(SELECT_ + COLUMN_NAME_DATA_TYPE_UDT_NAME_AS_T + FROM_ + INFORMATION_SCHEMA_COLUMNS + _WHERE_
		+ " table_name='" + tableName + "' and table_schema='" + tableSchema + "' ");
	returned.append("EXCEPT ");
	returned.append(SELECT_ + COLUMN_NAME_DATA_TYPE_UDT_NAME_AS_T + FROM_ + "information_schema.columns " + _WHERE_
		+ " table_name='" + modelName + "' and table_schema='" + modelSchema + "' ");
	returned.append(") ");
	returned.append("UNION ALL ");
	returned.append("( ");
	returned.append(SELECT_ + COLUMN_NAME_DATA_TYPE_UDT_NAME_AS_T + FROM_ + "information_schema.columns " + _WHERE_
		+ " table_name='" + modelName + "' and table_schema='" + modelSchema + "' ");
	returned.append("EXCEPT ");
	returned.append(SELECT_ + COLUMN_NAME_DATA_TYPE_UDT_NAME_AS_T + FROM_ + "information_schema.columns " + _WHERE_
		+ " table_name='" + tableName + "' and table_schema='" + tableSchema + "' ");
	returned.append(") ");
	returned.append(") v;");
	returned.append(NEWLINE + " if (n>0) then ");
	returned.append(
		DROP_ + " " + ObjectType.TABLE + " " + _IF_EXISTS_ + " " + tableSchema + DOT + tableName + "; ");
	returned.append(_ALTER_ + " " + ObjectType.TABLE + " " + modelSchema + DOT + modelName + " rename to "
		+ tableName + "; ");
	returned.append("else ");
	returned.append(
		DROP_ + " " + ObjectType.TABLE + " " + _IF_EXISTS_ + " " + modelSchema + DOT + modelName + "; ");
	returned.append("end if; ");
	returned.append("END; ");
	returned.append("$$;\n");
	return returned.toString();
    }

    /**
     *
     * @param anExpression
     * @return retourne l'expression brackettée <{> anExpression <}>
     */
    public static String encapsulerBracket(String anExpression) {
	return new StringBuilder(OPENING_BRACE + anExpression + CLOSING_BRACE).toString();
    }

    public static String addColonne(String aNomTable, String aNomColonne, String aType, String aValeurParDefaut) {
	StringBuilder returned = new StringBuilder();
	returned.append(NEWLINE + " " + _ALTER_ + " " + ObjectType.TABLE + " " + aNomTable + " ADD COLUMN "
		+ aNomColonne + " " + aType + " DEFAULT " + aValeurParDefaut + ";");
	return returned.toString();
    }

    public static String addColonne(String aNomTable, String aNomColonne, String aType) {
	StringBuilder returned = new StringBuilder();
	returned.append(NEWLINE + " " + _ALTER_ + " " + ObjectType.TABLE + " " + aNomTable + " ADD COLUMN "
		+ aNomColonne + " " + aType + ";");
	return returned.toString();
    }

    public static String listeContraintes(String aSchema, String aTable, String aTypeContrainte) {
	StringBuilder returned = new StringBuilder();
	returned.append(NEWLINE + " " + _SELECT_ + " conname, nam.nspname, cla.relname");
	returned.append(NEWLINE + " " + FROM_ + "pg_constraint con");
	returned.append(NEWLINE + "   INNER " + _JOIN_ + "pg_class cla ON con.conrelid = cla.oid");
	returned.append(NEWLINE + "   INNER " + _JOIN_ + "pg_namespace nam ON nam.oid = cla.relnamespace");
	returned.append(NEWLINE + " " + _WHERE_ + " lower(cla.relname) = lower('" + aTable + "')");
	returned.append(NEWLINE + "   AND lower(nam.nspname) = lower('" + aSchema + "')");
	returned.append(NEWLINE + "   AND lower(con.contype) = lower('" + aTypeContrainte + "')");
	returned.append(";");
	return returned.toString();
    }

    public static String addPrimaryKey(String aSchema, String aTable, List<String> aListeVariables) {
	StringBuilder returned = new StringBuilder();
	returned.append(NEWLINE + " " + _ALTER_ + " " + ObjectType.TABLE + " " + aSchema + DOT + aTable);
	returned.append(NEWLINE + " ADD CONSTRAINT " + aSchema + "_" + aTable + "_pkey");
	returned.append(NEWLINE + " PRIMARY KEY");
	returned.append(
		NEWLINE + " " + Format.untokenize(aListeVariables, "(", EMPTY, EMPTY, ", ", CLOSING_PARENTHESIS));
	returned.append(";");
	return returned.toString();
    }

    public static String alterTableSetSchema(String nomTable, String schema) {
	StringBuilder returned = new StringBuilder();
	returned.append(_ALTER_ + " " + ObjectType.TABLE + " " + nomTable + " SET SCHEMA " + schema + ";");
	return returned.toString();
    }

    /**
     * 
     * @param fromTable
     * @param groupBy
     * @param toTable
     * @return
     */

    public static String createAsSelectDistinct(String fromTable, Set<String> groupBy, String toTable) {
	StringBuilder returned = new StringBuilder();
	String tempTable = temporaryTableName(fromTable);
	String zeGroupBy = new SequentialUntokenizer<>().untokenize(groupBy);
	returned.append(NEWLINE + " " + CREATE_ + " " + ObjectType.TABLE + " " + tempTable + " AS ");
	returned.append(__SELECT_ + zeGroupBy + _FROM_ + fromTable + " GROUP BY " + zeGroupBy + ";");
	if (fromTable.equalsIgnoreCase(toTable)) {
	    returned.append(dropUniqueTable(fromTable));
	}
	returned.append(renameTableTo(tempTable, toTable));
	return returned.toString();
    }

    public static String selectDistinct(String fromTable, Set<String> groupBy) {
	StringBuilder returned = new StringBuilder();
	// String tempTable = temporaryTableName(fromTable);
	String zeGroupBy = new SequentialUntokenizer<>().untokenize(groupBy);
	returned.append(__SELECT_ + zeGroupBy + _FROM_ + fromTable + " GROUP BY " + zeGroupBy);
	return returned.toString();
    }

    /**
     * Requête pour récupérer le modèle de données d'une table Attention, les noms
     * des colonnes qui contiennent l'information sont attname et typname.
     * 
     * @param table
     * @return
     */

    public static String modeleDeDonneesTable(String tableSchema, String tableName) {
	StringBuilder requete = new StringBuilder();
	requete.append(NEWLINE + " " + _SELECT_ + " lower(column_name) as attname");
	requete.append(NEWLINE + "   , " + EXPRESSION_TYPE_SQL_SEUL + " as typname");
	requete.append(NEWLINE + " " + FROM_ + INFORMATION_SCHEMA_COLUMNS);
	requete.append(NEWLINE + " " + _WHERE_ + " table_name = '" + tableName.toLowerCase() + "' ");
	requete.append(" AND table_schema = '" + tableSchema.toLowerCase() + "'; ");
	return requete.toString();
    }

    /**
     * Requête pour récupérer le modèle de données d'une table Attention, les noms
     * des colonnes qui contiennent l'information sont attname et typname.
     * 
     * @param table
     * @return
     */
    public static String modeleDeDonneesTable(String tableSchemaName) {
	String tableSchema = tableSchemaName.split("\\.")[0];
	String tableName = tableSchemaName.split("\\.")[1];
	return modeleDeDonneesTable(tableSchema, tableName);
    }

    /**
     * Requête pour récupérer les tables ayant un pattern donné
     * 
     * @param table
     * @return
     */
    public static String fetchTableNames(String tableSchema, String aPattern) {
	return fetchObjectNames(ObjectType.TABLE, tableSchema, aPattern);
    }

    /**
     * Requête pour récupérer les tables ayant un pattern donné (préfixées par le
     * schéma)
     * 
     * @param table
     * @return
     */
    public static String fetchTableNamesWithSchema(String tableSchema, String aPattern) {
	return fetchObjectNamesWithSchema(ObjectType.TABLE, tableSchema, aPattern);
    }

    /**
     * Requête pour récupérer les vues ayant un pattern donné
     * 
     * @param table
     * @return
     */
    public static String fetchViewNames(String viewSchema, String aPattern) {
	return fetchObjectNames(ObjectType.VIEW, viewSchema, aPattern);
    }

    /**
     * Requête pour récupérer les tables ayant un pattern donné
     * 
     * @param table
     * @return
     */

    public static String fetchObjectNames(ObjectType tableOrView, String tableSchema, String aPattern) {
	String tableType = (tableOrView.equals(ObjectType.TABLE) ? "BASE " : EMPTY) + tableOrView;
	StringBuilder requete = new StringBuilder();
	requete.append(NEWLINE + " " + _SELECT_ + " table_name");
	requete.append(NEWLINE + " " + FROM_ + "INFORMATION_SCHEMA.TABLES ");
	requete.append(NEWLINE + " " + _WHERE_ + " table_name ~ '" + aPattern + "' ");
	requete.append(NEWLINE + "   AND table_schema = '" + tableSchema.toLowerCase() + "'");
	requete.append(NEWLINE + "   AND table_type = '" + tableType + "'; ");
	return requete.toString();
    }

    /**
     * Requête pour récupérer les tables ayant un pattern donné (préfixées par le
     * schéma)
     * 
     * @param table
     * @return
     */
    public static String fetchObjectNamesWithSchema(ObjectType tableOrView, String tableSchema, String aPattern) {
	String tableType = (tableOrView.equals(ObjectType.TABLE) ? "BASE " : EMPTY) + tableOrView;
	StringBuilder requete = new StringBuilder();
	requete.append(NEWLINE + " " + _SELECT_ + " table_schema,table_name");
	requete.append(NEWLINE_TABULATION + " " + FROM_ + "INFORMATION_SCHEMA.TABLES ");
	requete.append(NEWLINE_TABULATION + _WHERE_ + " table_name ~ '" + aPattern + "' ");
	requete.append(NEWLINE_TABULATION + _AND_ + "table_schema = '" + tableSchema.toLowerCase() + "'");
	requete.append(NEWLINE_TABULATION + _AND_ + "table_type = '" + tableType + "'; ");
	return requete.toString();
    }

    /**
     * TODO à finir
     * 
     * @param nomTable
     * @param nomEtTypeAttributs
     * @return
     */

    public static String requeteCreate(String nomTable, List<List<String>> nomEtTypeAttributs) {
	StringBuilder requete = new StringBuilder();
	requete.append(FormatSQL.dropUniqueTable(nomTable));
	requete.append(NEWLINE + CREATE_ + ObjectType.TABLE + _IF_NOT_EXISTS_);
	requete.append(nomTable);
	requete.append(OPENING_PARENTHESIS + NEWLINE);
	requete.append(FormatSQL.mettreEnFormeAttributTypeCreate(nomEtTypeAttributs));
	requete.append(NEWLINE + CLOSING_PARENTHESIS);
	requete.append(NEWLINE + SEMI_COLON);
	return requete.toString();
    }

    public static String mettreEnFormeAttributTypeCreate(List<List<String>> nomEtTypeAttributs) {
	StringBuilder listEnForm = new StringBuilder();
	if (!nomEtTypeAttributs.isEmpty()) {
	    // Parcour toute la liste sauf le dernier element
	    for (int i = 0; i < nomEtTypeAttributs.size() - 1; i++) {
		listEnForm.append(nomEtTypeAttributs.get(i).get(0));
		listEnForm.append(SPACE);
		listEnForm.append(nomEtTypeAttributs.get(i).get(1));
		listEnForm.append(COMMA + NEWLINE);
	    }
	    listEnForm.append(nomEtTypeAttributs.get(nomEtTypeAttributs.size() - 1).get(0));
	    listEnForm.append(SPACE);
	    listEnForm.append(nomEtTypeAttributs.get(nomEtTypeAttributs.size() - 1).get(1));
	    return listEnForm.toString();
	}
	return EMPTY;
    }

    /**
     * Requête pour récupérer le modèle de données de colonnes précises d'une table.
     * Attention, les noms des colonnes qui contiennent l'information sont attname
     * et typname.
     * 
     * @param table
     * @return
     */

    public static String modeleDeDonneesTable(String tableSchema, String tableName, String... colonnes) {
	StringBuilder requete = new StringBuilder();
	requete.append(NEWLINE + _SELECT_ + "lower(column_name)" + _AS_ + "attname");
	requete.append(NEWLINE + COMMA + " lower(data_type)" + _AS_ + "typname");
	requete.append(NEWLINE + FROM_ + INFORMATION_SCHEMA_COLUMNS);
	requete.append(NEWLINE + _WHERE_ + " table_name " + EQUALS + QUOTE + tableName.toLowerCase() + QUOTE);
	requete.append(_AND_ + "table_schema = '" + tableSchema.toLowerCase() + "' ");
	requete.append(_AND_ + "column_name IN" + OPENING_PARENTHESIS);
	requete.append(Format.untokenize(colonnes, EMPTY, QUOTE, QUOTE, COMMA, QUOTE));
	requete.append(CLOSING_PARENTHESIS);
	return requete.toString();
    }

    /**
     * Requête pour récupérer le modèle de données d'une table Attention, les noms
     * des colonnes qui contiennent l'information sont attname et typname.
     * 
     * @param table
     * @return
     */
    public static String modeleDeDonneesTable(String tableSchemaName, String... colonnes) {
	String tableSchema = tableSchemaName.split("\\.")[0];
	String tableName = tableSchemaName.split("\\.")[1];
	return modeleDeDonneesTable(tableSchema, tableName, colonnes);
    }

    /**
     * Pour savoir si une function existe dans la base voir pour verrouiller sur le
     * schéma également.
     * 
     * @param aNom
     * @param aNbArg
     * @return
     */

    public static String checkIfFunctionExist(String aNom, int aNbArg) {
	StringBuilder requete = new StringBuilder();
	requete.append(NEWLINE + " " + _SELECT_ + TRUE);
	requete.append(NEWLINE + " " + FROM_ + "pg_proc");
	requete.append(NEWLINE + " " + _WHERE_ + " proname " + EQUALS + FormatSQL.textToSql(aNom) + _AND_
		+ "pronargs = " + aNbArg);
	requete.append(NEWLINE + " ;");
	return requete.toString();
    }

    /**
     * Renvoie les tables héritant de celle-ci Colonnes de résultat:
     * 
     * @child (schema.table)
     */

    public static String getAllInheritedTables(String tableSchema, String tableName) {
	StringBuilder requete = new StringBuilder();
	requete.append(NEWLINE + " " + _SELECT_ + " cn.nspname||'.'||c.relname AS child ");
	requete.append(NEWLINE + " " + FROM_ + "pg_inherits  ");
	requete.append(NEWLINE + _JOIN_ + "pg_class AS c ON (inhrelid=c.oid) ");
	requete.append(NEWLINE + _JOIN_ + "pg_class as p ON (inhparent=p.oid) ");
	requete.append(NEWLINE + _JOIN_ + "pg_namespace pn ON pn.oid = p.relnamespace ");
	requete.append(NEWLINE + _JOIN_ + "pg_namespace cn ON cn.oid = c.relnamespace ");
	requete.append(
		NEWLINE + " " + _WHERE_ + " p.relname = '" + tableName + "' and pn.nspname = '" + tableSchema + "' ; ");
	return requete.toString();
    }

    /**
     * Change l'héritage d'une table
     */

    public static String deleteTableHeritage(String table, String ancienneTableHeritage) {
	StringBuilder requete = new StringBuilder();
	requete.append(NEWLINE + ALTER_ + ObjectType.TABLE + table);
	requete.append(NEWLINE + NO_ + INHERIT_ + ancienneTableHeritage);
	requete.append(NEWLINE + SEMI_COLON);
	return requete.toString();
    }

    /**
     * Change l'héritage d'une table
     */

    public static String addTableHeritage(String table, String nouvelleTableHeritage) {
	StringBuilder requete = new StringBuilder();
	requete.append(NEWLINE + ALTER_ + ObjectType.TABLE + table);
	requete.append(NEWLINE + INHERIT_ + nouvelleTableHeritage);
	requete.append(NEWLINE + SEMI_COLON);
	return requete.toString();
    }

    /**
     * Format a sql select request paramatrizes by the user
     * 
     * @param someCols
     *            : the cols to get (is null or empty will be replaced by *)
     * @param atableName
     *            : the table to get the data from
     * @param anOrderByColumn
     *            : a col to order
     * @param isAsc
     *            : asc order (by default yes)
     * @param someCondition
     *            : a condition
     * @param aLimit
     *            : a limit
     * @param anOffset
     *            : offset
     * @return a request lookking like :
     * 
     *         SELECT somecols FROM atablename WHERE a condition ORDER BY
     *         anOrderColumn ASC? LIMIT aLimit OFFSET anOffset
     * 
     */

    public static StringBuilder simpleSelectRequest(Collection<String> someCols, String atableName,
	    String anOrderByColumn, Boolean isAsc, Integer aLimit, Integer anOffset, String... someCondition) {
	StringBuilder requete = new StringBuilder();
	requete.append(__SELECT_);
	if (CollectionUtils.isEmpty(someCols)) {
	    requete.append(STAR);
	} else {
	    requete.append(String.join(_COMMA_, someCols));
	}

	requete.append(NEWLINE_TABULATION + FROM_ + atableName);
	// someCondition can have
	if (ArrayUtils.isNotEmpty(someCondition)) {
	    requete.append(NEWLINE_TABULATION + WHERE_ + String.join(_AND_, someCondition));
	}
	if (StringUtils.isNotBlank(anOrderByColumn)) {
	    requete.append(NEWLINE_TABULATION + ORDER_BY_ + anOrderByColumn);
	    if (isAsc != null && !isAsc) {
		requete.append(SPACE + DESC);
	    }
	}
	if (aLimit != null) {
	    requete.append(NEWLINE_TABULATION + LIMIT_ + aLimit);
	}
	if (anOffset != null) {
	    requete.append(NEWLINE_TABULATION + OFFSET_ + anOffset);
	}
	return requete;

    }

    /**
     * Format a SELECT * FROM atableName request
     * 
     * @param atableName
     *            : the table to get the reccords
     * @return SELECT * FROM atableName
     */
    public static StringBuilder getAllReccordsFromATable(String atableName) {
	return simpleSelectRequest(atableName, null, null, null);
    }

    /**
     * Format SELECT * FROM atableName ORDER BY anOrderByColumn ASC request
     * 
     * @param atableName
     * @param anOrderByColumn
     * @return SELECT * FROM atableName ORDER BY anOrderByColumn ASC
     */
    public static StringBuilder getAllReccordsFromATableAscOrder(String atableName, String anOrderByColumn) {
	return simpleSelectRequest(atableName, anOrderByColumn, true, null);
    }

    /**
     * 
     * @param atableName
     * @param anOrderByColumn
     * @return SELECT * FROM atableName ORDER BY anOrderByColumn DESC
     */
    public static StringBuilder getAllReccordsFromATableDescOrder(String atableName, String anOrderByColumn) {
	return simpleSelectRequest(atableName, anOrderByColumn, false, null);
    }

    /**
     * 
     * @param atableName
     * @param someConditions
     * @return SELECT * FROM atableName WHERE someConditions
     */
    public static StringBuilder getSomeReccordFromATable(String atableName, String... someConditions) {
	return simpleSelectRequest(null, atableName, null, false, null, null, someConditions);
    }

    /**
     * 
     * @param atableName
     * @param anOrderByColumn
     * @param someConditions
     * @return SELECT * FROM atableName WHERE someConditions ORDER BY
     *         anOrderByColumn ASC
     */
    public static StringBuilder getSomeReccordFromATableAscOrdered(String atableName, String anOrderByColumn,
	    String... someConditions) {
	return simpleSelectRequest(null, atableName, anOrderByColumn, true, null, null, someConditions);
    }

    /**
     * 
     * @param atableName
     * @param anOrderByColumn
     * @param someConditions
     * @return SELECT * FROM atableName WHERE someConditions ORDER BY
     *         anOrderByColumn DESC
     */
    public static StringBuilder getSomeReccordFromATableDescOrdered(String atableName, String anOrderByColumn,
	    String... someConditions) {
	return simpleSelectRequest(null, atableName, anOrderByColumn, false, null, null, someConditions);
    }

    /**
     * 
     * @param atableName
     * @param anOrderByColumn
     * @param isAsc
     * @param someConditions
     * @param aLimit
     * @return SELECT * FROM atableName WHERE someConditions ORDER BY
     *         anOrderByColumn ASC/DESC LIMIT aLimit
     */
    public static StringBuilder simpleSelectRequest(String atableName, String anOrderByColumn, Boolean isAsc,
	    Integer aLimit, String... someConditions) {
	return simpleSelectRequest(null, atableName, anOrderByColumn, isAsc, aLimit, null, someConditions);
    }

    /**
     * Format a SELECT * FROM atableName request
     * 
     * @param atableName
     *            : the table to get the reccords
     * @return SELECT * FROM atableName
     */
    public static StringBuilder getAllReccordsFromATable(Collection<String> someCols, String atableName) {
	return simpleSelectRequest(someCols, atableName, null, null, null);
    }

    /**
     * Format SELECT * FROM atableName ORDER BY anOrderByColumn ASC request
     * 
     * @param atableName
     * @param anOrderByColumn
     * @return SELECT * FROM atableName ORDER BY anOrderByColumn ASC
     */
    public static StringBuilder getAllReccordsFromATableAscOrder(Collection<String> someCols, String atableName,
	    String anOrderByColumn) {
	return simpleSelectRequest(someCols, atableName, anOrderByColumn, true, null);
    }

    /**
     * 
     * @param atableName
     * @param anOrderByColumn
     * @return SELECT * FROM atableName ORDER BY anOrderByColumn DESC
     */
    public static StringBuilder getAllReccordsFromATableDescOrder(Collection<String> someCols, String atableName,
	    String anOrderByColumn) {
	return simpleSelectRequest(someCols, atableName, anOrderByColumn, false, null);
    }

    /**
     * 
     * @param atableName
     * @param someConditions
     * @return SELECT * FROM atableName WHERE someConditions
     */
    public static StringBuilder getSomeReccordFromATable(Collection<String> someCols, String atableName,
	    String... someConditions) {
	return simpleSelectRequest(someCols, atableName, null, false, null, null, someConditions);
    }

    /**
     * 
     * @param atableName
     * @param anOrderByColumn
     * @param someConditions
     * @return SELECT * FROM atableName WHERE someConditions ORDER BY
     *         anOrderByColumn ASC
     */
    public static StringBuilder getSomeReccordFromATableAscOrdered(Collection<String> someCols, String atableName,
	    String anOrderByColumn, String... someConditions) {
	return simpleSelectRequest(someCols, atableName, anOrderByColumn, true, null, null, someConditions);
    }

    /**
     * 
     * @param atableName
     * @param anOrderByColumn
     * @param someConditions
     * @return SELECT * FROM atableName WHERE someConditions ORDER BY
     *         anOrderByColumn DESC
     */
    public static StringBuilder getSomeReccordFromATableDescOrdered(Collection<String> someCols, String atableName,
	    String anOrderByColumn, String... someConditions) {
	return simpleSelectRequest(someCols, atableName, anOrderByColumn, false, null, null, someConditions);
    }

    /**
     * 
     * @param atableName
     * @param anOrderByColumn
     * @param isAsc
     * @param someConditions
     * @param aLimit
     * @return SELECT * FROM atableName WHERE someConditions ORDER BY
     *         anOrderByColumn ASC/DESC LIMIT aLimit
     */
    public static StringBuilder simpleSelectRequest(Collection<String> someCols, String atableName,
	    String anOrderByColumn, Boolean isAsc, Integer aLimit, String... someConditions) {
	return simpleSelectRequest(someCols, atableName, anOrderByColumn, isAsc, aLimit, null, someConditions);
    }

    public static StringBuilder createTemporaryTableWithColumn(String tableName, Pair<String, String> column) {

	return createTemporaryTableWithColumn(tableName, Arrays.asList(column));

    }

    public static StringBuilder createTemporaryTableWithColumn(String tableName,
	    Collection<Pair<String, String>> columns) {

	return createObjectWithColumn(ObjectType.TEMPORARY_TABLE, tableName, columns);

    }

    public static StringBuilder createObjectWithColumn(ObjectType tableOrView, String tableName,
	    Collection<Pair<String, String>> columns) {
	StringBuilder request = new StringBuilder();
	request.append(CREATE_);
	request.append(tableOrView);
	request.append(SPACE +tableName + SPACE+ OPENING_PARENTHESIS);
	// concat the pairs column type
	request.append(
		columns.stream().map(p -> (p.getFirst() + SPACE + p.getSecond())).collect(Collectors.joining(_COMMA_)));
	request.append(CLOSING_PARENTHESIS);
	request.append(SEMI_COLON);
	return request;

    }

    /**
     * Add an header sql commentary to a query
     * 
     * @param theQuery
     *            : the query that will be modified
     * @param theHeader
     *            : the header to add
     * @return the query with a sql commentary header
     */
    public static String addCommentaryHeaderToQuery(String theQuery, String theHeader) {
	StringBuilder outputed = new StringBuilder();
	outputed.append(toCommentary(theHeader));
	outputed.append(NEWLINE + theQuery);

	return outputed.toString();

    }

    /**
     * Add an header sql commentary to a query
     * 
     * @param theQuery
     *            : the query that will be modified
     * @param someHeaders
     *            : somme headers to add
     * @return the query with a sql commentary header
     */
    public static String addCommentaryHeaderToQuery(String theQuery, Collection<String> someHeaders) {
	StringBuilder outputed = new StringBuilder();
	if (CollectionUtils.isNotEmpty(someHeaders)) {
	    outputed.append(toCommentary(String.join(NEWLINE, someHeaders)));	    
	    outputed.append(NEWLINE);
	}
	outputed.append(theQuery);

	return outputed.toString();

    }

    /**
     * Add an header sql commentary to a query
     * 
     * @param theQuery
     *            : the query that will be modified
     * @param theHeader
     *            : the header to add
     * @return the query with a sql commentary header
     */
    public static String toCommentary(String noneFormatedCommentary) {
	return BEGIN_COMMENTARY + noneFormatedCommentary + END_COMMENTARY;

    }
}
