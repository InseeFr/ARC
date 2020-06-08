package fr.insee.arc.utils.sqlengine;

import fr.insee.arc.utils.sqlengine.model.IToken;
import fr.insee.arc.utils.textUtils.MapUntokenizer;

/**
 * Simple implementation of the {@link ContextName} interface.</br>
 * It's a simple compound name.
 */
public class CompoundName implements ContextName {
    
    private MapUntokenizer<IToken, IToken> untokenizer;
    protected Naming naming;
    private String name;

    public CompoundName(Naming naming, MapUntokenizer<IToken, IToken> untokenizer) {
        this.naming = naming;
        this.untokenizer = untokenizer;
    }

    /**
     * If necessary, evaluate the name before returning it.
     */
    @Override
    public final String name() {
        if (this.name == null) {
            evaluate();
        }
        return this.name;
    }

    /**
     * Combination of {@link Namings} with {@link MapUntokenizer}
     */
    private void evaluate() {
        this.name = this.untokenizer.untokenize(this.naming.tokens());
    }

    @Override
    public void setUntokenizer(MapUntokenizer<IToken, IToken> untokenizer) {
        this.untokenizer = untokenizer;
        this.name = null;
    }

    @Override
    public MapUntokenizer<IToken, IToken> getUntokenizer() {
        return this.untokenizer;
    }

    @Override
    public void setNaming(Naming naming) {
        this.naming = naming;
        this.name = null;
    }

    @Override
    public Naming getNaming() {
        return this.naming;
    }

    public CompoundName clone() {
        return new CompoundName(getNaming().clone(), getUntokenizer());
    }

    /**
     * Warning : one must use {@link #name()} in order to evaluate a name before returning it.
     */
    @Override
    public int hashCode() {
        return defaultHashCode();
    }

    /**
     * Warning : one must use {@link #name()} in order to evaluate a name before returning it.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IToken)) {
            return false;
        }
        IToken other = (IToken) obj;
        if (name() == null) {
            if (other.name() != null) {
                return false;
            }
        } else if (!name().equals(other.name())) {
            return false;
        }
        return true;
    }
    
    public String toString() {
        return name();
    }
}
