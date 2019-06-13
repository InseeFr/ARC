package fr.insee.arc_essnet.utils.sqlengine;

import fr.insee.arc_essnet.utils.sqlengine.model.IToken;
import fr.insee.arc_essnet.utils.textUtils.MapUntokenizer;

/**
 * Implémentation simple de l'interface {@link ContextName}.</br>
 * Se veut un nom composé simple.
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
     * Evalue le nom textuel, si besoin avant de le renvoyer.
     */
    @Override
    public final String name() {
        if (this.name == null) {
            evaluate();
        }
        return this.name;
    }

    /**
     * Combine le {@link Namings} avec le {@link MapUntokenizer}
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

    public CompoundName deepClone() {
        return new CompoundName(getNaming().deepClone(), getUntokenizer());
    }

    /**
     * Attention, il faut absolument utiliser {@link #name()} qui évalue le nom avant de le renvoyer. 
     */
    @Override
    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((name() == null) ? 0 : name().hashCode());
        return defaultHashCode();
    }

    /**
     * Attention, il faut absolument utiliser {@link #name()} qui évalue le nom avant de le renvoyer. 
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
