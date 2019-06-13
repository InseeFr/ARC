package fr.insee.arc_essnet.utils.sqlengine.model;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.insee.arc_essnet.utils.sqlengine.ContextName;
import fr.insee.arc_essnet.utils.sqlengine.ITable;
import fr.insee.arc_essnet.utils.sqlengine.Naming;
import fr.insee.arc_essnet.utils.sqlengine.ParallelizationSuffix;
import fr.insee.arc_essnet.utils.sqlengine.Tokens;
import fr.insee.arc_essnet.utils.textUtils.IConstanteCaractere;
import fr.insee.arc_essnet.utils.textUtils.MapUntokenizer;

/**
 *
 * Une table dont le nom est pleinement qualifié
 *
 */
public abstract class SQLTable<T extends IAttribute> implements ITable<T>
{
    
    /**
     * Un MapUntokenizer minimal, le plus simple avec uniquement un schema et un nom
     */
    public static final MapUntokenizer<IToken, IToken> SCHEMA_PLUS_NOM = (map) -> //
    (map.get(Tokens.TOK_SCHEMA)==null)?//
            map.get(Tokens.TOK_TABLE_NAME).name()://
                map.get(Tokens.TOK_SCHEMA).name() + "." + map.get(Tokens.TOK_TABLE_NAME).name();
    
    
    /**
     * Façon de construire les noms dans thésée.<br/>
     * &lt;nomSchema&gt;.&lt;nomPhase&gt;_&lt;identifiantCampagne&gt;_&lt;identifiantFiltre&gt;_&lt;nom&gt;
     * 
     */
    public static final MapUntokenizer<IToken, IToken> SQL_QUALIFIED_TABLENAME_ASSEMBLER_THESEE = (objects) -> {
        StringBuilder returned = new StringBuilder();
        if (objects.containsKey(Tokens.TOK_SCHEMA) && (objects.get(Tokens.TOK_SCHEMA) != null)
                && (objects.get(Tokens.TOK_SCHEMA).name() != null))
        {
            returned.append(objects.get(Tokens.TOK_SCHEMA).name());
            returned.append(IConstanteCaractere.DOT);
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
     * Façon de construire les noms dans thésée.<br/>
     * &lt;nomSchema&gt;.&lt;nomPhase&gt;_&lt;identifiantCampagne&gt;_&lt;identifiantFiltre&gt;_&lt;nom&gt;
     * 
     */
    public static final MapUntokenizer<IToken, IToken> SQL_QUALIFIED_TABLENAME_ASSEMBLER_THESEE_SOURCE = (objects) -> {
        StringBuilder returned = new StringBuilder();
        if (objects.containsKey(Tokens.TOK_SCHEMA) && (objects.get(Tokens.TOK_SCHEMA) != null)
                && (objects.get(Tokens.TOK_SCHEMA).name() != null))
        {
            returned.append(objects.get(Tokens.TOK_SCHEMA).name());
            returned.append(IConstanteCaractere.DOT);
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
     * Façon de construire les noms dans diane.<br/>
     * &lt;nomSchema&gt;.&lt;identifiantCampagne&gt;_&lt;identifiantSample&gt;_&lt;nomPhase&gt;_&lt;nom&gt;
     * 
     */
    public static final MapUntokenizer<IToken, IToken> SQL_QUALIFIED_TABLENAME_ASSEMBLER = (objects) -> {
        StringBuilder returned = new StringBuilder();
        if (objects.containsKey(Tokens.TOK_SCHEMA) && (objects.get(Tokens.TOK_SCHEMA) != null)
                && (objects.get(Tokens.TOK_SCHEMA).name() != null))
        {
            returned.append(objects.get(Tokens.TOK_SCHEMA).name());
            returned.append(IConstanteCaractere.DOT);
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
     * Prend un MapUntokenizer en entrée pour fournir en sortie un MapUntokenizer (interface fonctionnelle avec une seule méthode).
     * Protection contre le fait que le contenu du {@link Tokens#TOK_SUFFIX_TEMP} est null.
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
     * Actuellement, fabrique le nom de la table avec le suffixe de parallelisation.
     * @return Le nom qualifié de la table
     */
    // todo : a modifier
    public String qualifiedName()
    {
        return name().name() + ParallelizationSuffix.get();
    }

    /**
     *
     * @return Le nom qualifié de la table
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
