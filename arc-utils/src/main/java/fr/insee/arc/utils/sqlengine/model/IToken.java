package fr.insee.arc.utils.sqlengine.model;

/**
 *
 * Un nom atomique
 *
 */
public interface IToken {

    String name();

    String toString();
    
    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    default int defaultHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name() == null) ? 0 : name().hashCode());
        return result;
    }
    
    int hashCode();
    
    boolean equals(Object o);

}
