package fr.insee.arc.utils.sqlengine;

import fr.insee.arc.utils.sqlengine.model.IToken;

/**
 * Wrap a {@link String} and provide {@link #hashCode()} et {@code #equals(Object)}.
 *
 * This way it implements a GOF lightweight pattern.
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
