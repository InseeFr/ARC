package fr.insee.arc_essnet.utils.textUtils;

import java.util.List;

/**
 *
 * Concatène les objets de deux listes de même taille et renvoie le résultat de cette concaténation sous forme de
 * {@code String}
 *
 * @param <T>
 * @param <U>
 */
@FunctionalInterface
public interface BiUntokenizer<T, U>
{

    public static final String EMPTY_STRING = "";

    /**
     *
     * @param lefty
     *            la première liste des éléments à concaténer
     * @param righty
     *            la seconde liste des éléments à concaténer
     * @return la concaténation des élements de la liste, soit pris de façon séquentielle, soit parallèle
     */
    String untokenize(List<? extends T> lefty, List<? extends U> righty);
}
