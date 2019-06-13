package fr.insee.arc_essnet.utils.textUtils;

import java.util.Collection;

/**
 *
 * Classe utilitaire capable de concaténer des objets et de stocker le résultat de cette concaténation dans un
 * {@link String}.
 *
 * @param <T>
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
