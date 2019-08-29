package fr.insee.arc.utils.textUtils;

import java.util.List;

/**
 *
 * Concatenates list items from two equally sized lists, returns a {@code String} in result.
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
     *            the first list
     * @param righty
     *            the second list
     * @return list elements concatenated, either sequencially or in parallel
     */
    String untokenize(List<? extends T> lefty, List<? extends U> righty);
}
