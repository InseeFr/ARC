package fr.insee.arc.utils.sqlengine.model;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.insee.arc.utils.sqlengine.ContextName;
import fr.insee.arc.utils.sqlengine.ITable;
import fr.insee.arc.utils.sqlengine.Naming;
import fr.insee.arc.utils.sqlengine.ParallelizationSuffix;
import fr.insee.arc.utils.sqlengine.Tokens;
import fr.insee.arc.utils.textUtils.ICharacterConstant;
import fr.insee.arc.utils.textUtils.MapUntokenizer;

/**
 *
 * A fully qualified table
 *
 */
public abstract class SQLTable<T extends IAttribute> implements ITable<T>
{
    
    /**
     * A minimalist MapUntokenizer with only schema and name
     */
    public static final MapUntokenizer<IToken, IToken> SCHEMA_PLUS_NOM = (map) -> //
    (map.get(Tokens.TOK_SCHEMA)==null)?//
            map.get(Tokens.TOK_TABLE_NAME).name()://
                map.get(Tokens.TOK_SCHEMA).name() + "." + map.get(Tokens.TOK_TABLE_NAME).name();
    
    
    /**
     * The Thésée way of building names.
     *
     * &lt;schemaName&gt;.&lt;phaseName&gt;_&lt;campaignIdentifier&gt;_&lt;filterIdentifier&gt;_&lt;name&gt;
     *
     */
    public static final MapUntokenizer<IToken, IToken> SQL_QUALIFIED_TABLENAME_ASSEMBLER_THESEE = (objects) -> {
        StringBuilder returned = new StringBuilder();
        if (objects.containsKey(Tokens.TOK_SCHEMA) && (objects.get(Tokens.TOK_SCHEMA) != null)
                && (objects.get(Tokens.TOK_SCHEMA).name() != null))
        {
            returned.append(objects.get(Tokens.TOK_SCHEMA).name());
            returned.append(ICharacterConstant.DOT);
        }
        List<IToken> liste = Arrays.asList(//
                objects.get(Tokens.TOK_NOM_PHASE), //
                objects.get(Tokens.TOK_CAMPAGNE), //
                objects.get(Tokens.TOK_FILTRE), //
                objects.get(Tokens.TOK_TABLE_NAME),//
                objects.get(Tokens.TOK_SUFFIX_TEMP));
        returned.append(liste.stream().filter((t) -> t != null).map(IToken::name).collect(Collectors.joining("_")));
        return returned.toString();
    };

    /**
     * The Thésée way of building names.
     *
     * &lt;schemaName&gt;.&lt;phaseName&gt;_&lt;campaignIdentifier&gt;_&lt;filterIdentifier&gt;_&lt;name&gt;
     *
     */
    public static final MapUntokenizer<IToken, IToken> SQL_QUALIFIED_TABLENAME_ASSEMBLER_THESEE_SOURCE = (objects) -> {
        StringBuilder returned = new StringBuilder();
        if (objects.containsKey(Tokens.TOK_SCHEMA) && (objects.get(Tokens.TOK_SCHEMA) != null)
                && (objects.get(Tokens.TOK_SCHEMA).name() != null))
        {
            returned.append(objects.get(Tokens.TOK_SCHEMA).name());
            returned.append(ICharacterConstant.DOT);
        }
        List<IToken> liste = Arrays.asList(//
                objects.get(Tokens.TOK_ENV), //
                objects.get(Tokens.TOK_TABLE_NAME), //
                objects.get(Tokens.TOK_NUMERO), //
                objects.get(Tokens.TOK_METIER));
        returned.append(liste.stream().filter((t) -> t != null).map(IToken::name).collect(Collectors.joining("_")));
        return returned.toString();
    };
    
    
    
    
    /**
     * The Diane way of building names.
     *
     * &lt;schemaName&gt;.&lt;campaignIdentifier&gt;_&lt;sampleIdentifier&gt;_&lt;phaseName&gt;_&lt;name&gt;
     *
     */
    public static final MapUntokenizer<IToken, IToken> SQL_QUALIFIED_TABLENAME_ASSEMBLER = (objects) -> {
        StringBuilder returned = new StringBuilder();
        if (objects.containsKey(Tokens.TOK_SCHEMA) && (objects.get(Tokens.TOK_SCHEMA) != null)
                && (objects.get(Tokens.TOK_SCHEMA).name() != null))
        {
            returned.append(objects.get(Tokens.TOK_SCHEMA).name());
            returned.append(ICharacterConstant.DOT);
        }
        List<IToken> liste = Arrays.asList(//
                objects.get(Tokens.TOK_CAMPAGNE), //
                objects.get(Tokens.TOK_SAMPLE), //
                objects.get(Tokens.TOK_NOM_PHASE), //
                objects.get(Tokens.TOK_TABLE_NAME));
        returned.append(liste.stream().filter((t) -> t != null).map(IToken::name).collect(Collectors.joining("_")));
        return returned.toString();
    };
    public static final MapUntokenizer<IToken, IToken> SQL_UNQUALIFIED_TABLENAME_ASSEMBLER = (objects) -> {
        StringBuilder returned = new StringBuilder();
        List<IToken> liste = Arrays.asList(//
                objects.get(Tokens.TOK_CAMPAGNE), //
                objects.get(Tokens.TOK_SAMPLE), //
                objects.get(Tokens.TOK_NOM_PHASE), //
                objects.get(Tokens.TOK_TABLE_NAME));
        returned.append(liste.stream().filter((t) -> t != null).map(IToken::name).collect(Collectors.joining("_")));
        return returned.toString();
    };
    public static final MapUntokenizer<IToken, IToken> SQL_TECHNICALID_ASSEMBLER = (objects) -> {
        StringBuilder returned = new StringBuilder();
        returned.append(objects.get(Tokens.TOK_PREFIX).name());
        returned.append(objects.get(Tokens.TOK_SUFFIX_TEMP).name());
        return returned.toString();
    };
    
    /**
     * This function change the suffix order for {@code Tokens#TOK_SUFFIX_TEMP}.
     *
     * It also handle the case where TOK_SUFFIX_TEMP is null.
     *
     */
    public static final Function<MapUntokenizer<IToken, IToken>, MapUntokenizer<IToken, IToken>> TO_TEMPORARY = (
            untok) -> (objects) -> {
            	
            	if(objects.get(Tokens.TOK_SUFFIX_TEMP)!=null) {
            		String suffix = objects.get(Tokens.TOK_SUFFIX_TEMP).name();
            		return (untok.untokenize(objects)).replace("_" + suffix, "") + "_" + suffix;
            	}
            	return (untok.untokenize(objects));
             };

    private ContextName name;

    public SQLTable(ContextName name)
    {
        this.name = name;
    }

    @Override
    public ContextName name()
    {
        return this.name;
    }

    public String name(MapUntokenizer<IToken, IToken> untokenizer)
    {
        return untokenizer.untokenize(this.name.getNaming().tokens());
    }

    /**
     * @return the naming
     */
    public final Naming getNaming()
    {
        return name().getNaming();
    }

    /**
     * @return the untokenizer
     */
    public final MapUntokenizer<IToken, IToken> getUntokenizer()
    {
        return name().getUntokenizer();
    }

    /**
     * Currently builds a table name with the parallelization suffix.
     *
     * @return the fully qualified table name
     *
     */
    // todo : to be modified
    public String qualifiedName()
    {
        return name().name() + ParallelizationSuffix.get();
    }

    /**
     *
     * @return the fully qualified table name
     */
    public String notQualifiedName()
    {
        return name().name().replaceFirst("^.*\\.", "") + ParallelizationSuffix.get();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof SQLTable)) { return false; }
        SQLTable other = (SQLTable) obj;
        if (this.name == null)
        {
            if (other.name != null) { return false; }
        }
        else if (!this.name.equals(other.name)) { return false; }
        return true;
    }

    /**
     * @param name
     *            the name to set
     */
    public final void setName(ContextName name)
    {
        this.name = name;
    }
}
