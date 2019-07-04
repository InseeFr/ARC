package fr.insee.arc.utils.sqlengine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.naming.CompoundName;

import org.apache.tools.ant.types.resources.Tokens;

import fr.insee.arc.utils.sqlengine.model.IToken;
import fr.insee.arc.utils.textUtils.MapUntokenizer;
import fr.insee.arc.utils.utils.Containers;

/**
 * Simple implemenation of {@link Naming}. </br>
 * <p>
 * In order to build a complete table name, this class is combined with {@link MapUntokenizer} in the implementation class {@link CompoundName}.
 */
public class DefaultNaming implements Naming {

    private Map<IToken, IToken> tokens;

    public DefaultNaming() {
        this.tokens = new HashMap<>();
    }

    /**
     * TODO Checking that map keys exist in  {@link Tokens}
     *
     * @param tokens
     */
    public DefaultNaming(Map<IToken, IToken> tokens) {
        this();
        this.tokens.putAll(tokens);
    }

    /**
     * Warning : the number of parameter must be even and the order must be for each pair :
     * a {@link Tokens} followed by a value;
     * <p>
     *
     * @param tokens
     */
    public DefaultNaming(IToken... tokens) {
        this(Containers.fillMap(new HashMap<>(), Arrays.asList(tokens)));
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
