package fr.insee.arc_essnet.utils.sqlengine;

import fr.insee.arc_essnet.utils.sqlengine.model.IToken;

/**
 *
 * Encapsule une {@link String} chaîne de caractères et fournit les opérations {@link #hashCode()} et {@code #equals(Object)}. Permet
 * d'implémenter le pattern poids-mouche facilement.
 *
 */
public class StringToken implements IToken {

    private String name;

    public StringToken(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((name() == null) ? 0 : name().hashCode());
        return defaultHashCode();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
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
        if (this.name == null) {
            if (other.name() != null) {
                return false;
            }
        } else if (!this.name.equals(other.name())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name();
    }
    
}
