package fr.insee.arc.utils.textUtils;

import java.util.Map;

/**
 *
 * Utility class for concatenating {@link Map<K, V>} entries into a {@link String}.
 *
 * @param <K>
 * @param <V>
 */
public interface MapUntokenizer<K, V> {

    /**
     *
     * @param map The Map to concatenate
     * @return the resulting string
     */
    String untokenize(Map<K, V> map);
}
