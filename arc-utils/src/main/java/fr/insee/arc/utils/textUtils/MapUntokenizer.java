package fr.insee.arc.utils.textUtils;

import java.util.Map;

/**
 * Classe utilitaire capable de concaténer des entrées d'une {@link Map<K, V>} et de stocker le résultat de cette
 * concaténation dans un {@link String}.
 *
 *
 * @param <K>
 * @param <V>
 */
public interface MapUntokenizer<K, V>
{

    /**
     *
     * @param map
     *            la table associative des entrées à concaténer
     * @return une chaîne de caractères qui concatène les représentations des objets sous forme de {@link String}
     */
    String untokenize(Map<K, V> map);
}
