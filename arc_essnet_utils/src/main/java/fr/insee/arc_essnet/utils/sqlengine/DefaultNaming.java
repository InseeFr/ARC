package fr.insee.arc_essnet.utils.sqlengine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.naming.CompoundName;

import org.apache.tools.ant.types.resources.Tokens;

import fr.insee.arc_essnet.utils.sqlengine.model.IToken;
import fr.insee.arc_essnet.utils.textUtils.MapUntokenizer;
import fr.insee.arc_essnet.utils.utils.Containers;

/**
 * Implémentation simple de {@link Naming}. </br>
 * Pour la construction complète d'un nom de table, </br>
 * cette classe est combinée avec un {@link MapUntokenizer} dans la classe concrète {@link CompoundName}. 
 * 
 *
 */
public class DefaultNaming implements Naming {

    private Map<IToken, IToken> tokens;

    public DefaultNaming() {
        this.tokens = new HashMap<>();
    }

    /**
     * TODO à réfléchir sur le fait de vérifier que les clefs de la map sont existent bien dans {@link Tokens}.
     * @param tokens
     */
    public DefaultNaming(Map<IToken, IToken> tokens) {
        this();
        this.tokens.putAll(tokens);
    }

    /**
     * Attention, il faut que le nombre de paramètre soit pair </br>
     * et que l'ordre soit le suivant : </br>
     *  un {@link Tokens} suivi d'une valeur. </br>
     * @param tokens
     */
    public DefaultNaming(IToken... tokens){
        this( Containers.fillMap(new HashMap<>(), Arrays.asList(tokens)));
    }
    
    
    @Override
    public Map<IToken, IToken> tokens() {
        return this.tokens;
    }

    @Override
    public IToken get(IToken token) {
        return tokens().get(token);
    }

    @Override
    public void set(IToken key, IToken value) {
        if (tokens().containsKey(key)) {
            tokens().put(key, value);
        }
    }

    @Override
    public Integer size() {
        return tokens().size();
    }

    @Override
    public Naming deepClone() {
        return new DefaultNaming(this.tokens);
    }
    
    public String toString() {
        return this.tokens.toString();
    }

}
