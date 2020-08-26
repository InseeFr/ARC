package fr.insee.arc.utils.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.textUtils.IConstanteCaractere;
import fr.insee.arc.utils.textUtils.IConstanteNumerique;
import fr.insee.arc.utils.textUtils.SequentialUntokenizer;

public class FormatSQL implements IConstanteCaractere, IConstanteNumerique
{
    public static final String NULL = "null";
    public static final String WITH_AUTOVACUUM_FALSE = "" + FormatSQL.WITH_NO_VACUUM + "";
    public static final String COLLATE_C = "COLLATE pg_catalog.\"C\"";
    private static final String TEXT = "text";
    public static final String TEXT_COLLATE_C = TEXT + space + COLLATE_C;
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
    private static final Logger LOGGER = LogManager.getLogger(FormatSQL.class);
    public static final boolean DROP_FIRST_FALSE = false;
    public static final boolean DROP_FIRST_TRUE = true;
    public static final int TAILLE_MAXIMAL_BLOC_SQL = 300000;
    public static final int MAX_LOCK_PER_TRANSACTION = 50;
    public static final int TIME_OUT_SQL_EN_HEURE = 100;
    public static final int TIMEOUT_MAINTENANCE = 600000;
    

    public static final String EXPRESSION_TYPE_SQL_SEUL = "CASE WHEN lower(data_type)='array' \n THEN replace(replace(replace(ltrim(udt_name,'_'),'int4','int'),'int8','bigint'),'float8','float')||'[]' \n ELSE lower(data_type) \n END ";
    public static final String EXPRESSION_TYPE_SQL = EXPRESSION_TYPE_SQL_SEUL + " AS data_type";
    private static final String BEGIN_COMMENTARY = "/*";
    private static final String END_COMMENTARY = "*/";
    
    public static enum ObjectType
    {
        TABLE("TABLE"), VIEW("VIEW"), TEMPORARY_TABLE ("TEMPORARY TABLE");
        private String name;

        private ObjectType(String aName)
        {
            this.name = aName;
        }

        public String toString()
        {
            return this.name;
        }
    }

    public static String end(String[] separator)
    {
        String end = new String();
        if (separator == null || separator.length == 0)
        {
            end = defaultSeparator;
        }
        else
        {
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
    public static String getRequete(String req, String... args)
    {
        String s = req;
        String exp = "";
        for (int i = 0; i < args.length; i++)
        {
            exp = "{" + i + "}";
            s = s.replace(exp, args[i]);
        }
        return s;
    }

    /**
     * Requête de paramétrage. Utile pour faire des jointures sur des colonnes
     * NULLABLE.
     *
     * @param isTransformNullEquals
     * @return la requête qui permet de valoriser l'expression
     *         {@code machin = NULL} à une des deux valeurs de vérité
     *         {@code true} ou {@code false}.
     */
    public static final String setTransformNullEquals(boolean isTransformNullEquals)
    {
        return "set transform_null_equals=" + isTransformNullEquals + ";";
    }

     public static String dropUniqueTable(String aTableName)
    {
        return dropUniqueObject(ObjectType.TABLE, aTableName);
    }

    public static String dropUniqueView(String aTableName)
    {
        return dropUniqueObject(ObjectType.VIEW, aTableName);
    }

    public static String dropUniqueObject(ObjectType tableOrView, String anObjectName)
    {
        StringBuilder sql = new StringBuilder("\n DROP " + tableOrView + " IF EXISTS " + anObjectName + " CASCADE;");
        return sql.toString();
    }

    public static String dropViews(Collection<String> someViewNames)
    {
        return dropObjects(ObjectType.VIEW, someViewNames);
    }

    public static String dropTables(Collection<String> someTableNames)
    {
        return dropObjects(ObjectType.TABLE, someTableNames);
    }

    public static String dropObjects(ObjectType tableOrView, Collection<String> someTableNames)
    {
        StringBuilder returned = new StringBuilder();
        for (String nomTable : someTableNames)
        {
            returned.append(dropUniqueObject(tableOrView, nomTable) + "\n COMMIT;");
        }
        return returned.toString();
    }
    
    public static String dropTable(String tableName, String... separator) {
	StringBuilder returned = new StringBuilder();
	returned.append("DROP " + ObjectType.TABLE + " IF EXISTS " + tableName + " CASCADE " + end(separator));
	return returned.toString();
    }

    /**
     * Revoi le sch�ma d'une table
     * 
     * @param table
     * @return
     */
    public static String getSchema(String table)
    {
        return ManipString.substringBeforeFirst(table, ".");
    }

    /**
     * Revoi le nom court d'une table
     * 
     * @param table
     * @return
     */
    public static String getName(String table)
    {
        return ManipString.substringAfterFirst(table, ".");
    }

    
    public static String tableExists(String table, String... separator) {
	String tableSchema = ManipString.substringBeforeFirst(table, DOT);
	String tableName = ManipString.substringAfterLast(table, DOT);
	StringBuilder requete = new StringBuilder();
	requete.append("SELECT schemaname||'.'||tablename AS table_name FROM pg_tables ");
	requete.append("\n WHERE tablename like '" + tableName.toLowerCase() + "' ");
	if (table.contains(DOT)) {
		requete.append("\n AND schemaname = '" + tableSchema.toLowerCase() + "' ");
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
    public static String listeColonne(String table, String... listeAttribut)
    {
        String tableSchema = ManipString.substringBeforeFirst(table, ".");
        String tableName = ManipString.substringAfterLast(table, ".");
        StringBuilder requete = new StringBuilder();
        requete.append("SELECT column_name as column_name");
        for (int i = 0; i < listeAttribut.length; i++)
        {
            requete.append(", " + listeAttribut[i]/* + " AS " + listeAttribut[i]*/);
        }
        requete.append(" ");
        requete.append("FROM INFORMATION_SCHEMA.COLUMNS ");
        requete.append("WHERE table_name='" + tableName.toLowerCase() + "' ");
        if (table.contains("."))
        {
            requete.append(" AND table_schema='" + tableSchema.toLowerCase() + "'; ");
        }
        return requete.toString();
    }
    
    /**
     * Pour récupérer la liste des colonnes d'une table rapidement
     *
     * @param table
     * @return
     */
    public static String listeColonneByHeaders(String table)
    {
        StringBuilder requete = new StringBuilder();
        requete.append("select * from " + table + " where false; ");
        return requete.toString();
    }

    /**
     * Requête de sélection de la liste des colonnes des tables métier associée
     * à une norme
     *
     * @param listeTable
     * @return
     */
    public static String listeColonneTableMetierSelonFamilleNorme(String anEnvironnement, String idFamille)
    {
        return new StringBuilder("SELECT DISTINCT nom_variable_metier, type_variable_metier\n")//
                .append("  FROM " + anEnvironnement + "_mod_variable_metier\n")//
                .append("  WHERE lower(id_famille)=lower('" + idFamille + "')").toString();
    }

    
    /**
     * ecrit une clause de selection in
     *
     * @param fields
     * @param query
     * @return
     */
    public static String writeInQuery(String fields, String query)
    {
        StringBuilder listIn = new StringBuilder();
        listIn.append(" " + fields + " IN (");
        listIn.append(" SELECT distinct " + fields + " FROM ( ");
        listIn.append(query);
        listIn.append(") q1 ");
        listIn.append(") ");
        return listIn.toString();
    }

    public static String getIdColumns(String tempTableA)
    {
        return "SELECT upper(substr(column_name,3)) FROM information_schema.columns WHERE  table_name   = 'a' and substr(column_name,1,2)='i_' order by ordinal_position;\n";
    }

    public static String getDataColumns(String tempTableA)
    {
        return "SELECT upper(substr(column_name,3)) FROM information_schema.columns WHERE  table_name   = 'a' and substr(column_name,1,2)='v_' order by ordinal_position;\n";
    }

    /**
     * Configuration de la base de données pour des petites requetes
     *
     * @param defaultSchema
     * @return requete
     */
    public static String modeParallel(String defaultSchema)
    {
        // return "";
        return "set enable_nestloop=on; set enable_mergejoin=off; set enable_hashjoin=on; set enable_material=off; set enable_seqscan=off;"
                + "set work_mem='" + PARALLEL_WORK_MEM + "'; set maintenance_work_mem='" + PARALLEL_WORK_MEM
                + "'; set temp_buffers='" + PARALLEL_WORK_MEM + "'; set statement_timeout="
                + (3600000 * TIME_OUT_SQL_EN_HEURE) + "; "
                // + "set geqo=off;
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
    public static String modeSequential(String defaultSchema, String work_mem)
    {
        StringBuilder requete = new StringBuilder(
                "set enable_nestloop=on; set enable_mergejoin=off; set enable_hashjoin=on; "
                        + " set enable_seqscan=off; " + " set from_collapse_limit=100; set join_collapse_limit=100;"
                        + " set statement_timeout=" + (3600000 * TIME_OUT_SQL_EN_HEURE) + ";" + " set search_path="
                        + defaultSchema.toLowerCase() + ", public;");
        // if (work_mem != null) {
        // requete.append("set work_mem='" + work_mem + "';");
        // }
        // else
        // {
        // requete.append("set work_mem='"+SEQUENTIAL_WORK_MEM+"';");
        // }
        return requete.toString();
    }

    
    /**
     * timeOut
     */
    public static String setTimeOutMaintenance()
    {
        return "set statement_timeout="+TIMEOUT_MAINTENANCE+"; COMMIT; ";
    }
    
    public static String resetTimeOutMaintenance()
    {
        return "reset statement_timeout; COMMIT; ";
    }
    
    
    /**
     * essaie d'exectuer une requete et si elle n'échoue ne fait rien
     */
    public static String tryQuery(String query)
    {
        return "do $$ begin " + query + " exception when others then end; $$; ";
    }

    
    /**
     * Retourne la requête d'insertion dans {@code table} des valeurs
     * {@code values} de type {@code columnTypes} dans les colonnes
     * {@code columnNames}, en respectant cet ordre.
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
            List<String> values)
    {
        StringBuilder sb = new StringBuilder("INSERT INTO " + table + " (");
        for (int i = 0; i < columnNames.size(); i++)
        {
            if (i != 0)
            {
                sb.append(", ");
            }
            sb.append(columnNames.get(i));
        }
        sb.append(") VALUES (");
        for (int i = 0; i < values.size(); i++)
        {
            if (i != 0)
            {
                sb.append(", ");
            }
            sb.append(//
                    (values.get(i) == null ? "null" : //
                            ("'" + values.get(i).replace("'", "''") + "'")) + "::" + columnTypes.get(i));
        }
        sb.append(")");
        return sb.toString();
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
    	return "VACUUM "+ type +" " + table + ";\n"; 
    }

    /**
     * Recopie une table à l'identique; ceci pour éviter l'hérésie du vacuum
     *
     * @param table
     * @param where
     * @param triggersAndIndexes
     * @return
     */
    public static StringBuilder rebuildTableAsSelectWhere(String table, String where, String... triggersAndIndexes)
    {
        String tableRebuild = temporaryTableName(table, "RB");
        StringBuilder requete = new StringBuilder();
        requete.append("set enable_nestloop=off; ");
        requete.append("\n DROP TABLE IF EXISTS " + tableRebuild + " CASCADE; ");
        requete.append("\n CREATE ");
        if (!table.contains("."))
        {
            requete.append("TEMPORARY ");
        }
        else
        {
            requete.append(" ");
        }
        requete.append("TABLE " + tableRebuild + " " + FormatSQL.WITH_NO_VACUUM + " as select * from " + table
                + " a where " + where + "; ");
        requete.append("\n DROP TABLE IF EXISTS " + table + " CASCADE;");
        requete.append(
                "\n ALTER TABLE " + tableRebuild + " rename to " + ManipString.substringAfterFirst(table, ".") + " ;");
        requete.append("set enable_nestloop=on; ");
        for (int i = 0; i < triggersAndIndexes.length; i++)
        {
            requete.append(triggersAndIndexes[i]);
        }
        return requete;
    }

    /**
     * Création d'une table à partir d'une map contenant les nom des colonnes et les types SQL
     * @param aNomTable
     * @param aMapColonneToType
     * @return
     */
    public static String createFromSqlModele(String aNomTable, Map<String, String> aMapColonneToType)
    {
        StringBuilder requete = new StringBuilder();
        requete.append(FormatSQL.dropUniqueTable(aNomTable));
        requete.append("\n CREATE TABLE "+aNomTable);
        requete.append("\n (");
        requete.append("\n " +aMapColonneToType.keySet().stream().map(t-> t +" " + aMapColonneToType.get(t)).collect(Collectors.joining("\n, ")));
        requete.append("\n )");
        requete.append("\n ;");
        return requete.toString();
    }
    
    
    public static String createAsSelectFrom(String aNomTableCible, String aNomTableSource, boolean dropFirst)
    {
        // StringBuilder requete = new StringBuilder();
        // if (dropFirst)
        // {
        // requete.append(dropUniqueTable(aNomTableCible));
        // }
        // return requete.append(createAsSelectFrom(aNomTableCible,
        // aNomTableSource)).toString();
        return createAsSelectFrom(aNomTableCible, aNomTableSource, "*", null, dropFirst);
    }

    /**
     * TODO refactor
     *
     * @param aNomTableCible
     * @param aNomTableSource
     * @param clauseWhere
     * @param dropFirst
     * @return
     */
    public static String createAsSelectFrom(String aNomTableCible, String aNomTableSource, String clauseWhere,
            boolean dropFirst)
    {
        // StringBuilder requete = new StringBuilder();
        // if (dropFirst)
        // {
        // requete.append(dropUniqueTable(aNomTableCible));
        // }
        // return requete.append(createAsSelectFrom(aNomTableCible,
        // aNomTableSource, clauseWhere)).toString();
        return createAsSelectFrom(aNomTableCible, aNomTableSource, "*", clauseWhere, dropFirst);
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
            String clauseWhere, boolean dropFirst)
    {
        // StringBuilder requete = new StringBuilder();
        // if (dropFirst)
        // {
        // requete.append(dropUniqueTable(aNomTableCible));
        // }
        // String where = ((StringUtils.isBlank(clauseWhere)) ? empty : " WHERE
        // " + clauseWhere);
        // requete.append("\n CREATE TABLE " + aNomTableCible + " " +
        // FormatSQL.WITH_NO_VACUUM + " AS ");
        // requete.append("\n SELECT " + columns + " FROM " + aNomTableSource);
        // requete.append(where + ";");
        // return requete.toString();
        
        // Si la table contient un . on est dans un schema, sinon c'est du temps
        if (aNomTableCible.contains(".")) {
            return createObjectAsSelectFrom(ObjectType.TABLE, aNomTableCible, aNomTableSource, columns, clauseWhere,
                    dropFirst);
        } else {
            return createObjectAsSelectFrom(ObjectType.TEMPORARY_TABLE, aNomTableCible, aNomTableSource, columns, clauseWhere,
                    dropFirst);
        }
        
       
    }

    public static String createViewAsSelectFrom(String aNomTableCible, String aNomTableSource, Object object)
    {
        return createObjectAsSelectFrom(ObjectType.VIEW, aNomTableCible, aNomTableSource, "*", null, false);
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
            String aNomTableSource, String columns, String clauseWhere, boolean dropFirst)
    {
        StringBuilder requete = new StringBuilder();
        if (dropFirst)
        {
            requete.append(dropUniqueObject(tableOrView, aNomTableCible));
        }
        String where = ((StringUtils.isBlank(clauseWhere)) ? empty : " WHERE " + clauseWhere);
        /*
         * Attention ! Les vues ne peuvent être créées avec un
         * autovacuum_enabled
         */
        String vacuumIfNeeded = tableOrView.equals(ObjectType.TABLE) ? " " + FormatSQL.WITH_NO_VACUUM : "";
        String orReplaceForViewsOnly = tableOrView.equals(ObjectType.VIEW) ? "OR REPLACE " : "";
        requete.append(
                "\n CREATE " + orReplaceForViewsOnly + tableOrView + " " + aNomTableCible + vacuumIfNeeded + " AS ");
        requete.append("\n SELECT " + columns + " FROM " + aNomTableSource);
        requete.append(where + ";");
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
            String clauseWhere, boolean dropFirst)
    {
        StringBuilder requete = new StringBuilder();
        if (dropFirst)
        {
            requete.append(dropUniqueTable(aNomTableCible));
        }
        String where = ((StringUtils.isBlank(clauseWhere)) ? empty : " WHERE " + clauseWhere);
        // return requete.append(createAsSelectFrom(aNomTableCible,
        // aNomTableSource, columns, clauseWhere)).toString();
        // StringBuilder requete = new StringBuilder();
        requete.append("\n CREATE TEMPORARY TABLE " + aNomTableCible + " " + FormatSQL.WITH_NO_VACUUM + " AS ");
        requete.append("\n SELECT " + columns + " FROM " + aNomTableSource);
        requete.append(where + ";");
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
            String clauseWhere)
    {
        // StringBuilder requete = new StringBuilder();
        // requete.append("\n CREATE TABLE " + aNomTableCible + "
        // "+FormatSQL.WITH_NO_VACUUM+" AS ");
        // requete.append("\n SELECT " + columns + " FROM " + aNomTableSource);
        // requete.append(((StringUtils.isBlank(clauseWhere)) ? empty : "\n
        // WHERE " + clauseWhere) + ";");
        // return requete.toString();
        return createAsSelectFrom(aNomTableCible, aNomTableSource, columns, clauseWhere, DROP_FIRST_FALSE);
    }

    /**
     *
     * @param aNomTableCible
     * @param aNomTableSource
     * @return la requête de copie simple de la table {@code aNomTableCible}
     */
    public static String createAsSelectFrom(String aNomTableCible, String aNomTableSource)
    {
        // StringBuilder requete = new StringBuilder();
        // requete.append("CREATE TABLE " + aNomTableCible +
        // " "+FormatSQL.WITH_NO_VACUUM+" AS SELECT * FROM "
        // + aNomTableSource + ";");
        // return requete.toString();
        return createAsSelectFrom(aNomTableCible, aNomTableSource, empty, false);
    }

    /**
     *
     * @param aNomTableCible
     * @param aNomTableSource
     * @param clauseWhere
     *            le WHERE n'y est pas, je le rajouterai tout seul merci.
     * @return
     */
    public static String createAsSelectFrom(String aNomTableCible, String aNomTableSource, String clauseWhere)
    {
        return createAsSelectFrom(aNomTableCible, aNomTableSource, "*", clauseWhere, DROP_FIRST_FALSE);
    }

    /**
     *
     * @param aNomTableCible
     * @param aNomTableSource
     * @param clauseWhere
     *            le WHERE n'y est pas, je le rajouterai tout seul merci.
     * @return
     */
    public static String createTempAsSelectFrom(String aNomTableCible, String aNomTableSource, String clauseWhere)
    {
        return createTempAsSelectFrom(aNomTableCible, aNomTableSource, "*", clauseWhere, DROP_FIRST_FALSE);
    }

    public static String createIfNotExistsAsSelectFrom(String aNomTableCible, String aNomTableSource,
            String clauseWhere)
    {
        StringBuilder requete = new StringBuilder();
        requete.append("CREATE TABLE IF NOT EXISTS " + aNomTableCible + " " + FormatSQL.WITH_NO_VACUUM
                + " AS SELECT * FROM " + aNomTableSource + " WHERE " + clauseWhere + ";");
        return requete.toString();
    }

    public static String upsert(String nomTableUpdatee, String nomTableUpdator, List<String> listeColonnesClefs,
            List<String> listeColonnesUpdatees, List<String> listeColonnesIgnorees)
    {
        /*
         * Calcul de la condition de jointure
         */
        StringBuilder conditionJointure = new StringBuilder();
        for (int i = 0; i < listeColonnesClefs.size(); i++)
        {
            if (i > 0)
            {
                conditionJointure.append(" AND ");
            }
            conditionJointure
                    .append("updatee." + listeColonnesClefs.get(i) + " = updator." + listeColonnesClefs.get(i));
        }
        /*
         * ÉTAPE 1 : UPDATE
         */
        StringBuilder returned = new StringBuilder();
        StringBuilder update = new StringBuilder();
        update.append("\nUPDATE " + nomTableUpdatee + " updatee");
        update.append("\nSET ");
        for (int i = 0; i < listeColonnesUpdatees.size(); i++)
        {
            if (i > 0)
            {
                update.append(",\n  ");
            }
            update.append(listeColonnesUpdatees.get(i) + " = updator." + listeColonnesUpdatees.get(i));
        }
        update.append("\nFROM " + nomTableUpdator + " updator");
        update.append("\nWHERE " + conditionJointure + ";");
        returned.append(fastUpdate(update));
        /*
         * ÉTAPE 2 : INSERT
         */
        returned.append("\nINSERT INTO " + nomTableUpdatee + " (");
        String colonnes = Format
                .untokenize(Arrays.asList(listeColonnesClefs, listeColonnesUpdatees, listeColonnesIgnorees), ", ");
        returned.append(colonnes);
        returned.append(")");
        returned.append("\nSELECT " + colonnes);
        returned.append("\nFROM " + nomTableUpdator + " updator");
        returned.append("\nWHERE NOT EXISTS (");
        returned.append("\n  SELECT 1 FROM " + nomTableUpdatee + " updatee");
        returned.append("\n  WHERE " + conditionJointure);
        returned.append(");");
        return returned.toString();
    }

    /**
     * Transforme un tableau resultat de requete : chaque colonne va etre parsée
     * et éclatée selon un séparateur ca permet de pivoter, transposer tres
     * facilement (on utilise string_agg avec le separateur pour les colonnes
     * qu'on souhaite pivoter);
     *
     * @param r
     * @param separator
     * @return
     */
    public static ArrayList<ArrayList<String>> pivot(ArrayList<ArrayList<String>> r, String separator)
    {
        HashMap<String, ArrayList<String>> m = new GenericBean(r).mapContent();
        HashMap<String, ArrayList<ArrayList<String>>> z = new HashMap<String, ArrayList<ArrayList<String>>>();
        ArrayList<ArrayList<String>> tabFinal = new ArrayList<ArrayList<String>>();
        // on boucle sur chaque colonne
        for (int j = 0; j < r.get(0).size(); j++)
        {
            ArrayList<String> colonne = m.get(r.get(0).get(j));
            ArrayList<ArrayList<String>> tabColonne = new ArrayList<ArrayList<String>>();
            int maxNb = 0;
            // on parcours toutes les lignes de la colonne
            for (int i = 0; i < colonne.size(); i++)
            {
                ArrayList<String> ligne = new ArrayList<String>();
                String cell = ManipString.replaceNull(colonne.get(i));
                String q[] = cell.split(separator);
                // on split selon le separateur les cellules
                // pour creer les colonnes
                if (q.length > maxNb)
                {
                    maxNb = q.length;
                }
                for (int k = 0; k < q.length; k++)
                {
                    ligne.add(q[k]);
                }
                tabColonne.add(ligne);
            }
            // equilibrage du tableau : avoir le meme nombre d'element dans
            // chacune des lignes
            for (int i = 0; i < colonne.size(); i++)
            {
                for (int k = tabColonne.get(i).size(); k < maxNb; k++)
                {
                    tabColonne.get(i).add("");
                }
            }
            // ajout dans la map finale
            z.put(r.get(0).get(j), tabColonne);
        }
        // reconstitution du tableau final
        for (int i = 0; i < r.size() - 2; i++)
        {
            ArrayList<String> ligne = new ArrayList<String>();
            for (int j = 0; j < r.get(0).size(); j++)
            {
                ligne.addAll(z.get(r.get(0).get(j)).get(i));
            }
            if (i == 0)
            {
                ArrayList<String> headers = new ArrayList<String>();
                ArrayList<String> types = new ArrayList<String>();
                for (int j = 0; j < r.get(0).size(); j++)
                {
                    // if (z.get(r.get(0).get(j)).get(0).size()>1)
                    // {
                    for (int k = 0; k < z.get(r.get(0).get(j)).get(0).size(); k++)
                    {
                        headers.add(r.get(0).get(j) + "_" + k);
                        types.add(r.get(1).get(j));
                    }
                    // }
                    // else
                    // {
                    // headers.add(r.get(0).get(j));
                    // types.add(r.get(1).get(j));
                    // }
                }
                tabFinal.add(headers);
                tabFinal.add(types);
            }
            tabFinal.add(ligne);
        }
        return tabFinal;
    }

    public static StringBuilder dupliquerVers(List<String> sources, List<String> targets)
    {
        StringBuilder returned = new StringBuilder();
        for (int i = 0; i < sources.size(); i++)
        {
            returned.append(dupliquerVers(sources.get(i), targets.get(i)));
        }
        return returned;
    }

    public static StringBuilder dupliquerVers(String source, String target)
    {
        return new StringBuilder("DROP TABLE IF EXISTS " + target + ";")//
                .append("CREATE TABLE " + target + "  " + FormatSQL.WITH_NO_VACUUM + " AS SELECT * FROM " + source
                        + ";");
    }

    public static StringBuilder dupliquerVers(List<String> sources, List<String> targets, String clauseWhere)
    {
        StringBuilder returned = new StringBuilder();
        for (int i = 0; i < sources.size(); i++)
        {
            returned.append(dupliquerVers(sources.get(i), targets.get(i), clauseWhere));
        }
        return returned;
    }

    public static StringBuilder dupliquerVers(String source, String target, String clauseWhere)
    {
        return new StringBuilder("DROP TABLE IF EXISTS " + target + ";")//
                .append("CREATE TABLE " + target + "  " + FormatSQL.WITH_NO_VACUUM + " AS SELECT * FROM " + source
                        + " WHERE " + clauseWhere + ";");
    }

    public static final String listeTablesExistantes(List<String> tables)
    {
        List<String> liste = new ArrayList<>();
        for (int i = 0; i < tables.size(); i++)
        {
            liste.add(tableExiste(tables.get(i)).toString());
        }
        return Format.untokenize(liste, "\nUNION ");
    }

    public static final String listeTablesExistantes(String... tables)
    {
        return listeTablesExistantes(Arrays.asList(tables));
    }

    private static final StringBuilder tableExiste(String table)
    {
        String tokenJoin = table.contains(".") ?
        /*
         * Le nom de la table contient "." ? Il est précédé du nom du schéma.
         */
                " INNER JOIN pg_namespace ON pg_class.relnamespace = pg_namespace.oid" :
                /*
                 * Sinon, aucune jointure sur le nom de schéma.
                 */
                "";
        String tokenCond = table.contains(".") ?
        /*
         * Le nom de la table contient "." ? Il est précédé du nom de schéma.
         */
                "lower(pg_namespace.nspname||'.'||pg_class.relname)" :
                /*
                 * Sinon, la condition d'égalité porte sur le nom de la table
                 */
                "pg_class.relname";
        StringBuilder requete = new StringBuilder("SELECT DISTINCT '" + table + "' table_existe\n");
        requete.append("  FROM pg_class" + tokenJoin);
        requete.append("  WHERE " + tokenCond + " = lower('" + table + "')");
        return requete;
    }

    /**
     * @param table
     * @return
     */
    public static StringBuilder isTableExists(String table)
    {
        String tokenJoin = table.contains(".") ?
        /*
         * Le nom de la table contient "." ? Il est précédé du nom du schéma.
         */
                " INNER JOIN pg_namespace ON pg_class.relnamespace = pg_namespace.oid" :
                /*
                 * Sinon, aucune jointure sur le nom de schéma.
                 */
                "";
        String tokenCond = table.contains(".") ?
        /*
         * Le nom de la table contient "." ? Il est précédé du nom de schéma.
         */
                "lower(pg_namespace.nspname||'.'||pg_class.relname)" :
                /*
                 * Sinon, la condition d'égalité porte sur le nom de la table
                 */
                "pg_class.relname";
        StringBuilder requete = new StringBuilder(
                "SELECT CASE WHEN count(1)>0 THEN TRUE ELSE FALSE END table_existe\n");
        requete.append("  FROM pg_class" + tokenJoin);
        requete.append("  WHERE " + tokenCond + " = lower('" + table + "')");
        return requete;
    }

    /**
     * Ajoute un suffixe de table temporaire au nom de table {@code aName}
     *
     * @param aName
     * @return
     */
    public static final String temporaryTableName(String aName)
    {
        String newName = aName.split(_REGEX_TMP)[0];
        /**
         * Impératif : permet de s'assurer que les noms des tables ne causeront
         * aucune collision 2 milliseconds : pour éviter les problèmes
         * d'arrondis
         */
        Sleep.sleep(2);
        // on met la date du jour dans le nom de la table
        String l = System.currentTimeMillis() + "";
        // on prend que les 10 derniers chiffres (durrée de vie : 6 mois)
        l = l.substring(l.length() - 10);
        // on inverse la chaine de caractere pour avoir les millisecondes en
        // premier en cas de troncature
        l = new StringBuffer(l).reverse().toString();
        return new StringBuilder(newName).append(_TMP).append(l).append(dollar).append(randomNumber(4)).toString();
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
    public final static String temporaryTableName(String aName, String suffix)
    {
        String newName = aName.split(_REGEX_TMP)[0];
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

    public static final String id(String suffix)
    {
        return _ID + suffix;
    }

    /**
     * Mise à niveau d'une table qui a des colonnes manquantes
     *
     * @param table
     * @param listeColonne
     * @return
     */
    public static String addColonnePourGenericBeanData(String table, ArrayList<ArrayList<String>> listeColonne)
    {
        return addColonne(table, listeColonne);
    }

    /**
     * Mise à niveau d'une table qui a des colonnes manquantes
     *
     * @param table
     * @param listeColonne
     * @return
     */
    public static String addColonnePourGenericBean(String table, ArrayList<ArrayList<String>> listeColonne)
    {
        return addColonne(table, listeColonne.subList(ARRAY_THIRD_COLUMN_INDEX, listeColonne.size()));
    }

    /**
     * Mise à niveau d'une table qui a des colonnes manquantes
     *
     * @param table
     * @param listeColonne
     * @return
     */
    public static <T extends List<String>> String addColonne(String table, List<T> listeColonne)
    {
        StringBuilder requete = new StringBuilder();
        String nomColonne = "";
        String typeColonne = "";
        // élement (1er noms, 2e types)
        if (listeColonne.size() == 0) { return requete.toString(); }
        requete.append("ALTER TABLE " + table + " ");
        for (int i = ARRAY_FIRST_COLUMN_INDEX; i < listeColonne.size(); i++)
        {
            nomColonne = listeColonne.get(i).get(0);
            typeColonne = listeColonne.get(i).get(1);
            if (i > 0)
            {
                requete.append(",");
            }
            requete.append("ADD COLUMN " + nomColonne + " " + typeColonne + " ");
            if (typeColonne.equals("text"))
            {
                requete.append(" collate \"C\" ");
            }
        }
        requete.append(";");
        return requete.toString();
    }
    
    public static String dropColonne(String table, String colonne)
    {
        StringBuilder requete = new StringBuilder();
        requete.append("ALTER TABLE " + table + " ");
        requete.append("DROP COLUMN " + colonne);
        requete.append(";");
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
    public static String listAjoutColonne(String tableIn, String tableOut)
    {
        // scission du nom en entrée en schema + nom de table
        String tableSchemaIn = ManipString.substringBeforeFirst(tableIn, DOT);
        String tableNameIn = ManipString.substringAfterLast(tableIn, DOT);
        String tableSchemaOut = ManipString.substringBeforeFirst(tableOut, DOT);
        String tableNameOut = ManipString.substringAfterLast(tableOut, DOT);
        StringBuilder requete = new StringBuilder();
        requete.append("WITH ");
        requete.append("def AS (	SELECT column_name, data_type ");
        requete.append("		FROM INFORMATION_SCHEMA.COLUMNS ");
        requete.append("		WHERE TABLE_NAME='" + tableNameOut.toLowerCase() + "' ");
        if (tableOut.contains(DOT))
        {
            requete.append("	AND TABLE_schema='" + tableSchemaOut.toLowerCase() + "' ");
        }
        requete.append("), ");
        requete.append("temp AS (	SELECT column_name, udt_name, data_type");
        requete.append("		FROM INFORMATION_SCHEMA.COLUMNS ");
        requete.append("		WHERE TABLE_NAME='" + tableNameIn.toLowerCase() + "' ");
        if (tableIn.contains(DOT))
        {
            requete.append("			AND TABLE_schema='" + tableSchemaIn.toLowerCase() + "'");
        }
        requete.append(") ");
        requete.append(
                "SELECT column_name, ltrim(udt_name,'_')||(CASE WHEN lower(data_type)='ARRAY' THEN '[]' ELSE '' END) AS data_type ");
        requete.append("FROM temp ");
        requete.append("WHERE NOT EXISTS (SELECT 1 FROM def WHERE temp.column_name=def.column_name); ");
        return requete.toString();
    }

    /**
     * Verrouille une table
     *
     * @param tableName
     * @return
     */
    public static String lock(String... tableName)
    {
        StringBuilder requete = new StringBuilder();
        for (int i = 0; i < tableName.length; i++)
        {
            requete.append("LOCK TABLE " + tableName[i] + " IN ACCESS EXCLUSIVE MODE;\n");
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
    public static String textToSql(String val)
    {
        if (val == null || val.trim().equals("") || val == "null")
        {
            return "null";
        }
        else
        {
            return "'" + val.replace("'", "''") + "'";
        }
    }

    public static String textToSqlNoNull(String val)
    {
        if (val == null || val.trim().equals(""))
        {
            return "''";
        }
        else
        {
            return "'" + val.replace("'", "''") + "'";
        }
    }

    public static String int8ToSqlNoNull(String val)
    {
        if (val == null || val.trim().equals(""))
        {
            return "0";
        }
        else
        {
            return "'" + val.replace("'", "''") + "'";
        }
    }

    public static String boolToSqlNoNull(String val, String escape)
    {
        if (val == null || val.trim().equals(""))
        {
            return escape;
        }
        else
        {
            return "'" + val.replace("'", "''") + "'";
        }
    }

    public static String expressionJointure(String lefty, List<String> variablesLeft, String righty,
            List<String> variablesRight, String indent, String operator)
    {
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
     *         {@code righty} sur l'égalité (ou autre opérateur {@code operator}
     *         ) des variables de {@code variablesLeft} et
     *         {@code variablesRight}.
     */
    public static String expressionJointure(String lefty, List<String> variablesLeft, String righty,
            List<String> variablesRight, String indent, String operator, String cast)
    {
        // String token = (StringUtils.isBlank(cast) ? empty : "::" + cast);
        // StringBuilder returned = new StringBuilder();
        // for (int i = 0; i < variablesLeft.size(); i++) {
        // if (i > 0) {
        // returned.append("\n" + indent + "AND ");
        // }
        // returned.append(lefty + "." + variablesLeft.get(i) + token + " " +
        // operator + " " + righty + "." + variablesRight.get(i) +
        // token);
        // }
        // return returned.toString();
        return expressionTermeATerme(lefty, variablesLeft, righty, variablesRight, indent, operator, cast, "AND");
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
     * @return l'expression de mise à jour des variables {@code variablesLeft}
     *         de {@code lefty} à partir des variables {@code variablesRight} de
     *         {@code righty}.
     */
    public static String expressionEgaliteUpdate(String lefty, List<String> variablesLeft, String righty,
            List<String> variablesRight, String indent, String cast)
    {
        return expressionTermeATerme(lefty, variablesLeft, righty, variablesRight, indent, equals, cast, comma);
    }

    public static String expressionTermeATerme(String lefty, List<String> variablesLeft, String righty,
            List<String> variablesRight, String indent, String operator, String cast, String separator)
    {
        LoggerHelper.traceAsComment(LOGGER, "lefty =", lefty, "variablesLeft =", variablesLeft, "righty =", righty,
                "variablesRight =", variablesRight, "indent =", indent, "operator =", operator, "cast =", cast,
                "separator =", separator);
        String token = (StringUtils.isBlank(cast) ? empty : "::" + cast);
        StringBuilder returned = new StringBuilder();
        for (int i = 0; i < variablesLeft.size(); i++)
        {
            if (i > 0)
            {
                returned.append("\n" + indent + separator + space);
            }
            if (StringUtils.isNotBlank(lefty))
            {
                returned.append(lefty + DOT);
            }
            returned.append(variablesLeft.get(i) + token + space + operator + space);
            if (StringUtils.isNotBlank(righty))
            {
                returned.append(righty + DOT);
            }
            returned.append(variablesRight.get(i) + token);
        }
        return returned.toString();
    }

    /**
     * Pour faire des jointures même avec des null, on enveloppe les variables
     * dans row
     *
     * @param lefty
     *            nom de la table de gauche
     * @param variablesLeft
     *            nom des variables de la table de gauche sur lesqelles on fait
     *            la jointure
     * @param righty
     *            nom de la table de droite
     * @param variablesRight
     *            nom des variables de la table de droite sur lesqelles on fait
     *            la jointure
     * @param indent
     *            indentation pour la mise en forme dans les log
     * @param operator
     *            operateur de comparaison pour la jointure
     * @return
     */
    public static String expressionJointureRow(String lefty, List<String> variablesLeft, String righty,
            List<String> variablesRight, String indent, String operator)
    {
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
    public static String expressionRow(String table, List<String> variables)
    {
        StringBuilder returned = new StringBuilder("row(");
        for (int i = 0; i < variables.size(); i++)
        {
            if (i > 0)
            {
                returned.append(", ");
            }
            returned.append(table + "." + variables.get(i));
        }
        returned.append(")::text collate \"C\" ");
        return returned.toString();
    }

    /**
     * arrayRemoveNulls prend une liste et calcule le tableau resultat les null
     * sont exclus du tableau
     *
     * @param table
     * @param variables
     * @return
     */
    public static String arrayRemoveNulls(String table, List<String> variables)
    {
        StringBuilder returned = new StringBuilder();
        String prefix = "";
        if (table != null && !table.trim().equals(""))
        {
            prefix = table + DOT;
        }
        returned.append("string_to_array(rtrim(");
        boolean first = true;
        for (String var : variables)
        {
            if (!first)
            {
                returned.append("||");
            }
            returned.append("coalesce(" + prefix + var + "::text||" + SQL_SEPARATOR + ", '' )");
            first = false;
        }
        returned.append("," + SQL_SEPARATOR + "), " + SQL_SEPARATOR + ")");
        return returned.toString();
    }

    /**
     * arrayKeepNulls prend une liste et calcule le tableau resultat les null
     * sont inclus dans le tableau
     *
     * @param table
     * @param variables
     * @return
     */
    public static String arrayKeepNulls(String table, List<String> variables)
    {
        StringBuilder returned = new StringBuilder();
        String prefix = "";
        if (table != null && !table.trim().equals(""))
        {
            prefix = table + DOT;
        }
        returned.append("string_to_array(");
        boolean first = true;
        for (String var : variables)
        {
            if (!first)
            {
                returned.append("||" + SQL_SEPARATOR + "||");
            }
            returned.append("coalesce(" + prefix + var + "::text, '' )");
            first = false;
        }
        returned.append("," + SQL_SEPARATOR + ")");
        return returned.toString();
    }

    /**
     * Pour faire des jointures même avec des null, on enveloppe les variables
     * dans row
     *
     * @param lefty
     *            nom de la table de gauche
     * @param variablesLeft
     *            nom des variables de la table de gauche sur lesqelles on fait
     *            la jointure
     * @param righty
     *            nom de la table de droite
     * @param variablesRight
     *            nom des variables de la table de droite sur lesqelles on fait
     *            la jointure
     * @param indent
     *            indentation pour la mise en forme dans les log
     * @param operator
     *            operateur de comparaison pour la jointure
     * @return
     */
    public static String expressionJointureRowIgnoreCase(String lefty, List<String> variablesLeft, String righty,
            List<String> variablesRight, String indent, String operator)
    {
        StringBuilder returned = new StringBuilder();
        StringBuilder leftSide = new StringBuilder("upper(row(");
        StringBuilder rightSide = new StringBuilder("upper(row(");
        for (int i = 0; i < variablesLeft.size(); i++)
        {
            if (i > 0)
            {
                leftSide.append(", ");
                rightSide.append(", ");
            }
            leftSide.append(lefty + "." + variablesLeft.get(i));
            rightSide.append(righty + "." + variablesRight.get(i));
        }
        leftSide.append(")::text collate \"C\") ");
        rightSide.append(")::text collate \"C\") ");
        returned.append(leftSide).append(operator).append(rightSide);
        return returned.toString();
    }

    /**
     * Il est préférable que le nom de la séquence contienne un nom de schema
     *
     * @param nomSequence
     * @return
     */
    public static String createSequenceIfNotExists(String nomSequence)
    {
        String token = nomSequence.split("\\.")[nomSequence.contains(DOT) ? 1 : 0];
        String schema = nomSequence.contains(DOT) ? nomSequence.split("\\.")[0] : empty;
        StringBuilder returned = new StringBuilder("DO");
        returned.append("\n $$");
        returned.append("\n BEGIN");
        returned.append("\n  IF NOT EXISTS (SELECT 1 ");
        returned.append("\n                 FROM pg_class ");
        returned.append("\n                 INNER JOIN pg_namespace ");
        returned.append("\n                   ON pg_class.relnamespace = pg_namespace.oid ");
        returned.append("\n                 WHERE lower(relname) = lower('" + token + "')");
        if (!schema.isEmpty())
        {
            returned.append("\n                    AND lower(nspname)=lower('" + schema + "')");
        }
        returned.append("\n                 )");
        returned.append("\n  THEN");
        returned.append("\n     EXECUTE 'CREATE SEQUENCE " + nomSequence);
        returned.append("\n       INCREMENT 1");
        returned.append("\n       MINVALUE 1");
        returned.append("\n       MAXVALUE " + MAX_VALUE_BIGINT_SQL_POSTGRES);
        returned.append("\n       START 1");
        returned.append("\n       CACHE 1;';");
        returned.append("\n   END IF;");
        returned.append("\n END");
        returned.append("\n $$;");
        return returned.toString();
    }

    
    public static String safelyViewTableTo(String ancienne, String nouvelle)
    {
        StringBuilder returned = new StringBuilder();
        returned.append(createViewTo(ancienne, nouvelle));
        return returned.toString();
    }
    
    
    public static String safelyRenameTableTo(String ancienne, String nouvelle)
    {
        StringBuilder returned = new StringBuilder();
        returned.append(dropUniqueTable(nouvelle));
        returned.append(renameTableTo(ancienne, nouvelle));
        return returned.toString();
    }

    public static String renameTableTo(String ancienne, String nouvelle)
    {
        String token = nouvelle.split("\\.")[nouvelle.contains(DOT) ? 1 : 0];
        return "\n ALTER TABLE " + ancienne + " RENAME TO " + token + ";";
    }
    
    public static String dropConstraint(String aTable, String aConstraint)
    {
        return "\n ALTER TABLE " + aTable + " DROP CONSTRAINT IF EXISTS " + aConstraint + " CASCADE ;";
    }
    
    public static String createViewTo(String ancienne, String nouvelle)
    {
        return tryQuery("\n CREATE VIEW "+nouvelle+" AS SELECT * FROM "+ancienne+";");
    }

    public static StringBuilder createIndex(String nomIndex, String nomTable, List<String> listeColonnes)
    {
        StringBuilder returned = new StringBuilder();
        returned.append("\n CREATE INDEX " + nomIndex + " ON " + nomTable + " ("
                + Format.untokenize(listeColonnes, ", ") + ");");
        return returned;
    }

    public static StringBuilder createIndex(String nomIndex, String nomTable, String indexType,
            List<String> someColonnes)
    {
        return createIndex(nomIndex, nomTable, indexType, someColonnes, Arrays.asList());
    }

    public static StringBuilder createIndex(String nomIndex, String nomTable, String indexType,
            List<String> someColonnes, String indexImplementation)
    {
        return createIndex(nomIndex, nomTable, indexType, someColonnes, IntStream.range(0, someColonnes.size()).boxed()
                .map((i) -> indexImplementation).collect(Collectors.toList()));
    }
    public static String dropIndex (String nomIndex) {
	return "\n DROP INDEX "+ nomIndex + " ; ";
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
            List<String> someColonnes, List<String> indexTypes)
    {
        Function<Integer, String> iTypes = (i) -> (indexTypes != null) && (indexTypes.size() == someColonnes.size())
                ? " " + indexTypes.get(i) : "";
        StringBuilder returned = new StringBuilder();
        returned.append("\n CREATE INDEX");
        if (nomIndex != null)
        {
            returned.append(" " + nomIndex);
        }
        returned.append(" ON " + nomTable);
        if (indexType != null)
        {
            returned.append(" USING " + indexType);
        }
        returned.append(" (");
        returned.append(IntStream.range(0, someColonnes.size()).boxed()
                .map((i) -> someColonnes.get(i) + iTypes.apply(i)).collect(Collectors.joining(", ")));
        returned.append(")");
        return returned;
    }

    public static String createSchema(String aNomSchema, String authorization)
    {
        return "\n CREATE SCHEMA IF NOT EXISTS " + aNomSchema
                + (StringUtils.isBlank(authorization) ? empty : (" AUTHORIZATION " + authorization)) + ";";
    }

    public static String fastUpdate(String query)
    {
        String returned = query;
        String tableName = ManipString
                .substringBeforeFirst(ManipString.substringAfterFirst(returned.toLowerCase(), "update "), " ");
        returned = returned + "\n ; vacuum " + tableName + ";";
        return returned;
        // return "\n SELECT public.fastUpdate('" + query.replace("'", "''") +
        // "');";
    }

    public static String vacuum(String nomTable)
    {
        return "VACUUM " + nomTable + ";";
    }

    public static String fastUpdate(StringBuilder query)
    {
        return fastUpdate(query.toString());
    }

    public static String fastDelete(String query)
    {
        String returned = query;
        String tableName = ManipString
                .substringBeforeFirst(ManipString.substringAfterFirst(returned.toLowerCase(), "delete from "), " ");
        returned = returned + "\n ; vacuum " + tableName + ";";
        return returned;
    }

    public static String fastDelete(StringBuilder query)
    {
        return fastDelete(query.toString());
    }

    /**
     * Ne garde que les séparateur
     *
     * @param tokens
     * @param separator
     * @return
     */
    public static String toNullRow(Collection<?> tokens)
    {
        return (tokens == null || tokens.size() == 0) ? "(" + empty + ")"
                : "(" + StringUtils.repeat(",", tokens.size() - 1) + ")";
    }

    /**
     * Pour tester qu'une variable est NULL ou vide (quotequote en SQL)
     *
     * @param aNomVariable
     * @return
     */
    public static String rowEmpty(String aNomVariable)
    {
        return "row(" + aNomVariable + ")::text IN ('()','(\"\")')";
    }

    /**
     * recrée la table avec son modèle quand la structure de la table est
     * différente du modèle (la table a changée)
     *
     * @param table
     * @param model
     * @return
     */
    public static String replaceIfTableChanged(String table, String model)
    {
        String tableSchema = ManipString.substringBeforeFirst(table, ".").toLowerCase();
        String tableName = ManipString.substringAfterFirst(table, ".").toLowerCase();
        String modelSchema = ManipString.substringBeforeFirst(model, ".").toLowerCase();
        String modelName = ManipString.substringAfterFirst(model, ".").toLowerCase();
        StringBuilder returned = new StringBuilder();
        returned.append("\n do $$ ");
        returned.append("DECLARE n integer; ");
        returned.append("BEGIN ");
        returned.append("select count(1) into n from ( ");
        returned.append("( ");
        returned.append(
                "select column_name||'.'||data_type||'.'||udt_name as t from information_schema.columns where table_name='"
                        + tableName + "' and table_schema='" + tableSchema + "' ");
        returned.append("EXCEPT ");
        returned.append(
                "select column_name||'.'||data_type||'.'||udt_name as t from information_schema.columns where table_name='"
                        + modelName + "' and table_schema='" + modelSchema + "' ");
        returned.append(") ");
        returned.append("UNION ALL ");
        returned.append("( ");
        returned.append(
                "select column_name||'.'||data_type||'.'||udt_name as t from information_schema.columns where table_name='"
                        + modelName + "' and table_schema='" + modelSchema + "' ");
        returned.append("EXCEPT ");
        returned.append(
                "select column_name||'.'||data_type||'.'||udt_name as t from information_schema.columns where table_name='"
                        + tableName + "' and table_schema='" + tableSchema + "' ");
        returned.append(") ");
        returned.append(") v;");
        returned.append("\n if (n>0) then ");
        returned.append("DROP TABLE IF EXISTS " + tableSchema + "." + tableName + "; ");
        returned.append("ALTER TABLE " + modelSchema + "." + modelName + " rename to " + tableName + "; ");
        returned.append("else ");
        returned.append("DROP TABLE IF EXISTS " + modelSchema + "." + modelName + "; ");
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
    public static String encapsulerBracket(String anExpression)
    {
        return new StringBuilder(openingBrace + anExpression + closingBrace).toString();
    }

    public static String addColonne(String aNomTable, String aNomColonne, String aType, String aValeurParDefaut)
    {
        StringBuilder returned = new StringBuilder();
        returned.append("\n ALTER TABLE " + aNomTable + " ADD COLUMN " + aNomColonne + " " + aType + " DEFAULT "
                + aValeurParDefaut + ";");
        return returned.toString();
    }
    
    public static String addColonne(String aNomTable, String aNomColonne, String aType)
    {
        StringBuilder returned = new StringBuilder();
        returned.append("\n ALTER TABLE " + aNomTable + " ADD COLUMN " + aNomColonne + " " + aType + ";");
        return returned.toString();
    }

    public static String listeContraintes(String aSchema, String aTable, String aTypeContrainte)
    {
        StringBuilder returned = new StringBuilder();
        returned.append("\n SELECT conname, nam.nspname, cla.relname");
        returned.append("\n FROM pg_constraint con");
        returned.append("\n   INNER JOIN pg_class cla ON con.conrelid = cla.oid");
        returned.append("\n   INNER JOIN pg_namespace nam ON nam.oid = cla.relnamespace");
        returned.append("\n WHERE lower(cla.relname) = lower('" + aTable + "')");
        returned.append("\n   AND lower(nam.nspname) = lower('" + aSchema + "')");
        returned.append("\n   AND lower(con.contype) = lower('" + aTypeContrainte + "')");
        returned.append(";");
        return returned.toString();
    }

    public static String addPrimaryKey(String aSchema, String aTable, List<String> aListeVariables)
    {
        StringBuilder returned = new StringBuilder();
        returned.append("\n ALTER TABLE " + aSchema + "." + aTable);
        returned.append("\n ADD CONSTRAINT " + aSchema + "_" + aTable + "_pkey");
        returned.append("\n PRIMARY KEY");
        returned.append("\n " + Format.untokenize(aListeVariables, "(", empty, empty, ", ", ")"));
        returned.append(";");
        return returned.toString();
    }

    public static String alterTableSetSchema(String nomTable, String schema)
    {
        StringBuilder returned = new StringBuilder();
        returned.append("ALTER TABLE " + nomTable + " SET SCHEMA " + schema + ";");
        return returned.toString();
    }

    /**
     * 
     * @param fromTable
     * @param groupBy
     * @param toTable
     * @return
     */
    public static String createAsSelectDistinct(String fromTable, Set<String> groupBy, String toTable)
    {
        StringBuilder returned = new StringBuilder();
        String tempTable = temporaryTableName(fromTable);
        String zeGroupBy = new SequentialUntokenizer<>().untokenize(groupBy);
        returned.append("\n CREATE TABLE " + tempTable + " AS ");
        returned.append("\n SELECT " + zeGroupBy + " FROM " + fromTable + " GROUP BY " + zeGroupBy + ";");
        if (fromTable.equalsIgnoreCase(toTable))
        {
            returned.append(dropUniqueTable(fromTable));
        }
        returned.append(renameTableTo(tempTable, toTable));
        return returned.toString();
    }

    public static String selectDistinct(String fromTable, Set<String> groupBy)
    {
        StringBuilder returned = new StringBuilder();
        // String tempTable = temporaryTableName(fromTable);
        String zeGroupBy = new SequentialUntokenizer<>().untokenize(groupBy);
        returned.append("\n SELECT " + zeGroupBy + " FROM " + fromTable + " GROUP BY " + zeGroupBy);
        return returned.toString();
    }

    /**
     * Requête pour récupérer le modèle de données d'une table Attention, les
     * noms des colonnes qui contiennent l'information sont attname et typname.
     * 
     * @param table
     * @return
     */
    public static String modeleDeDonneesTable(String tableSchema, String tableName)
    {
        StringBuilder requete = new StringBuilder();
        requete.append("\n SELECT lower(column_name) as attname");
        requete.append("\n   , "+EXPRESSION_TYPE_SQL_SEUL+" as typname");
        requete.append("\n FROM INFORMATION_SCHEMA.COLUMNS ");
        requete.append("\n WHERE table_name = '" + tableName.toLowerCase() + "' ");
        requete.append(" AND table_schema = '" + tableSchema.toLowerCase() + "'; ");
        return requete.toString();
    }
    
    /**
     * Requête pour récupérer le modèle de données d'une table Attention, les
     * noms des colonnes qui contiennent l'information sont attname et typname.
     * 
     * @param table
     * @return
     */
    public static String modeleDeDonneesTable(String tableSchemaName)
    {
    	String tableSchema = tableSchemaName.split("\\.")[0];
		String tableName = tableSchemaName.split("\\.")[1];
		return modeleDeDonneesTable(tableSchema,tableName);
    }

    /**
     * Requête pour récupérer les tables ayant un pattern donné
     * 
     * @param table
     * @return
     */
    public static String fetchTableNames(String tableSchema, String aPattern)
    {
        return fetchObjectNames(ObjectType.TABLE, tableSchema, aPattern);
    }
    
    /**
     * Requête pour récupérer les tables ayant un pattern donné (préfixées par le schéma)
     * 
     * @param table
     * @return
     */
    public static String fetchTableNamesWithSchema(String tableSchema, String aPattern)
    {
        return fetchObjectNamesWithSchema(ObjectType.TABLE, tableSchema, aPattern);
    }

    /**
     * Requête pour récupérer les vues ayant un pattern donné
     * 
     * @param table
     * @return
     */
    public static String fetchViewNames(String viewSchema, String aPattern)
    {
        return fetchObjectNames(ObjectType.VIEW, viewSchema, aPattern);
    }

    /**
     * Requête pour récupérer les tables ayant un pattern donné
     * 
     * @param table
     * @return
     */
    public static String fetchObjectNames(ObjectType tableOrView, String tableSchema, String aPattern)
    {
        String tableType = (tableOrView.equals(ObjectType.TABLE) ? "BASE " : "") + tableOrView;
        StringBuilder requete = new StringBuilder();
        requete.append("\n SELECT table_name");
        requete.append("\n FROM INFORMATION_SCHEMA.TABLES ");
        requete.append("\n WHERE table_name ~ '" + aPattern + "' ");
        requete.append("\n   AND table_schema = '" + tableSchema.toLowerCase() + "'");
        requete.append("\n   AND table_type = '" + tableType + "'; ");
        return requete.toString();
    }
    
    /**
     * Requête pour récupérer les tables ayant un pattern donné (préfixées par le schéma)
     * 
     * @param table
     * @return
     */
    public static String fetchObjectNamesWithSchema(ObjectType tableOrView, String tableSchema, String aPattern)
    {
        String tableType = (tableOrView.equals(ObjectType.TABLE) ? "BASE " : "") + tableOrView;
        StringBuilder requete = new StringBuilder();
        requete.append("\n SELECT table_schema,table_name");
        requete.append("\n FROM INFORMATION_SCHEMA.TABLES ");
        requete.append("\n WHERE table_name ~ '" + aPattern + "' ");
        requete.append("\n   AND table_schema = '" + tableSchema.toLowerCase() + "'");
        requete.append("\n   AND table_type = '" + tableType + "'; ");
        return requete.toString();
    }

    public static String coalesce(String cast, String defaultValue, String... colNames)
    {
        List<String> cols = colNames == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(colNames));
        cols.add(defaultValue);
        return cols.stream().map((t) -> t + "::" + cast).collect(Collectors.joining(", ", "coalesce(", ")"));
    }
    
    /**
     * TODO à finir
     * @param nomTable
     * @param nomEtTypeAttributs
     * @return
     */
	public static String requeteCreate(String nomTable, List<List<String>> nomEtTypeAttributs) {
		StringBuilder requete = new StringBuilder();
		requete.append(FormatSQL.dropUniqueTable(nomTable));
		requete.append("\n CREATE TABLE IF NOT EXISTS ");
		requete.append(nomTable);
		requete.append("(\n");
		requete.append(FormatSQL.mettreEnFormeAttributTypeCreate(nomEtTypeAttributs));
		requete.append("\n)");
	      requete.append("\n;");
		return requete.toString();
	}
	
	public static String mettreEnFormeAttributTypeCreate(List<List<String>> nomEtTypeAttributs){
		StringBuilder listEnForm = new StringBuilder();	
		if(nomEtTypeAttributs.size() > 0){
			//Parcour toute la liste sauf le dernier element
			for(int i=0; i<nomEtTypeAttributs.size()-1; i++){
				listEnForm.append(nomEtTypeAttributs.get(i).get(0));
				listEnForm.append(" ");
				listEnForm.append(nomEtTypeAttributs.get(i).get(1));
				listEnForm.append(",\n");
			}
			listEnForm.append(nomEtTypeAttributs.get(nomEtTypeAttributs.size()-1).get(0));
			listEnForm.append(" ");
			listEnForm.append(nomEtTypeAttributs.get(nomEtTypeAttributs.size()-1).get(1));
			return listEnForm.toString();
		}
		return "";
	}
	
	
	/**
	 * Select avec attribut et where
	 * @param table
	 * @param attributs
	 * @param clauses
	 * @return
	 */
	public static String select(String table, String attributs, String... clauses){
		StringBuilder returned = new StringBuilder();
		returned.append("(SELECT ");
		returned.append(attributs);
		returned.append("\n FROM ");
		returned.append(table);
		returned.append(formerClauses(clauses));
		returned.append(")");
		return returned.toString();
	}
	
	/**
	 * Select avec attribut, limit et where
	 * @param table
	 * @param attributs
	 * @param limit
	 * @param clauses
	 * @return
	 */
	public static String select(String table, String attributs, int limit, String... clauses){
		StringBuilder returned = new StringBuilder();
		returned.append("(SELECT ");
		returned.append(attributs);
		returned.append("\n FROM ");
		returned.append(table);
		returned.append(formerClauses(clauses));
		returned.append("\n LIMIT " + limit);
		returned.append(")");
		return returned.toString();
	}
	
	public static String formerClauses(String... clauses){
		StringBuilder returned = new StringBuilder();
		if(clauses.length > 0){
			returned.append("\n WHERE ");
			returned.append(clauses[0]);
			for(int i=1; i<clauses.length; i++){
				returned.append("\n AND ");
				returned.append(clauses[i]);
			}
		}
		return returned.toString();
	}
	

    /**
     * Requête pour récupérer le modèle de données de colonnes précises d'une table. Attention, les
     * noms des colonnes qui contiennent l'information sont attname et typname.
     * 
     * @param table
     * @return
     */
    public static String modeleDeDonneesTable(String tableSchema, String tableName, String... colonnes)
    {
        StringBuilder requete = new StringBuilder();
        requete.append("\n SELECT lower(column_name) as attname");
        requete.append("\n   , lower(data_type) as typname");
        requete.append("\n FROM INFORMATION_SCHEMA.COLUMNS ");
        requete.append("\n WHERE table_name = '" + tableName.toLowerCase() + "' ");
        requete.append(" AND table_schema = '" + tableSchema.toLowerCase() + "' ");
        requete.append(" AND  column_name IN (");
        requete.append(Format.untokenize(colonnes, "", "'", "'", ",",""));
        requete.append(" ) ");
        return requete.toString();
    }
    
    /**
     * Requête pour récupérer le modèle de données d'une table Attention, les
     * noms des colonnes qui contiennent l'information sont attname et typname.
     * 
     * @param table
     * @return
     */
    public static String modeleDeDonneesTable(String tableSchemaName, String... colonnes)
    {
    	String tableSchema = tableSchemaName.split("\\.")[0];
		String tableName = tableSchemaName.split("\\.")[1];
		return modeleDeDonneesTable(tableSchema,tableName, colonnes);
    }
    
    /**
     * Pour savoir si une function existe dans la base
     * TODO voir pour verrouiller sur le schéma également.
     * @param aNom
     * @param aNbArg
     * @return
     */
    public static String checkIfFunctionExist(String aNom, int aNbArg) {
    	 StringBuilder requete = new StringBuilder();
         requete.append("\n SELECT true");
         requete.append("\n FROM pg_proc");
         requete.append("\n WHERE proname  = "+FormatSQL.textToSql(aNom)+" AND pronargs = "+ aNbArg );
         requete.append("\n ");
         return requete.toString();
    }
    
    /**
     * Renvoie les tables héritant de celle-ci
     * Colonnes de résultat:
     * @child (schema.table)
     */
    public static String getAllInheritedTables(String tableSchema, String tableName) {
    	StringBuilder requete = new StringBuilder();
    	requete.append("\n SELECT cn.nspname||'.'||c.relname AS child ");
    	requete.append("\n FROM pg_inherits  ");
    	requete.append("\n JOIN pg_class AS c ON (inhrelid=c.oid) ");
    	requete.append("\n JOIN pg_class as p ON (inhparent=p.oid) ");
    	requete.append("\n JOIN pg_namespace pn ON pn.oid = p.relnamespace ");
    	requete.append("\n JOIN pg_namespace cn ON cn.oid = c.relnamespace ");
    	requete.append("\n WHERE p.relname = '"+tableName+"' and pn.nspname = '"+tableSchema+"' ");
    	return requete.toString();
    }
    
    /**
     * Change l'héritage d'une table
     */
    public static String deleteTableHeritage(String table, String ancienneTableHeritage) {
    	StringBuilder requete = new StringBuilder();
    	requete.append("\n ALTER TABLE "+table);
    	requete.append("\n NO INHERIT "+ancienneTableHeritage);
    	requete.append("\n ; ");
    	return requete.toString();
    }
    
    /**
     * Change l'héritage d'une table
     */
    public static String addTableHeritage(String table, String nouvelleTableHeritage) {
    	StringBuilder requete = new StringBuilder();
    	requete.append("\n ALTER TABLE "+table);
    	requete.append("\n INHERIT "+nouvelleTableHeritage);
    	requete.append("\n ; ");
    	return requete.toString();
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
	outputed.append(newline + theQuery);

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
	    outputed.append(toCommentary(String.join(newline, someHeaders)));	    
	    outputed.append(newline);
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
