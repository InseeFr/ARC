package fr.insee.arc.utils.textUtils;

import java.util.Collection;

/**
 *
 * Functional interface describing the concatenation of {@link Collection} items into a {@link String}.
 *
 * @param <T> the collection type
 */

@FunctionalInterface
public interface Untokenizer<T>
{

    public static final String EMPTY_STRING = "";

    /**
     *
     * @param objects
     *            la collection des objets à concaténer
     * @return une chaîne de caractères résultant de la concaténation des objets
     */
    String untokenize(Collection<? extends T> objects);
}
