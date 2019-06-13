package fr.insee.arc_essnet.utils.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 *
 * Classe utilitaire capable de faire des traitements et des tests sur les
 * containers (collections et tables associatives).
 *
 */
public class Containers
{
    private Containers()
    {
    };

    public static final <T> boolean isItATotallyOrderedCollection(Collection<T> collection,
            BiPredicate<T, T> areTheyComparable)
    {
        return !collection.stream()
                .filter(
                        /*
                         * Les éléments dont
                         */
                        (t) -> collection.stream()
                                .filter(
                                        /*
                                         * les copains ne sont pas comparables
                                         * avec lui
                                         */
                                        (u) -> !areTheyComparable.test(t, u))
                                .findFirst().isPresent())
                /*
                 * existent-t-ils ?
                 */
                .findFirst().isPresent();
    }

    public static final <T> T greatest(Collection<T> someTs, Comparator<T> comparator)
    {
        T returned = null;
        for (T aT : someTs)
        {
            if ((returned == null) || comparator.compare(aT, returned) > 0)
            {
                returned = aT;
            }
        }
        return returned;
    }

    /**
     * 
     * @param returned
     * @param clazz
     * @return
     */
    public static final <T, U extends Collection<T>> BinaryOperator<U> union(U returned, Class<T> clazz)
    {
        return new BinaryOperator<U>()
        {
            @Override
            public U apply(U u, U v)
            {
                if (returned.isEmpty())
                {
                    returned.addAll(u);
                }
                returned.addAll(v);
                return returned;
            }
        };
    }

    /**
     * 
     * @param returned
     * @param clazz
     * @return
     */
    public static final <T, U extends Collection<T>> BinaryOperator<U> intersect(U returned, Class<T> clazz)
    {
        return new BinaryOperator<U>()
        {
            @Override
            public U apply(U u, U v)
            {
                if (returned.isEmpty())
                {
                    returned.addAll(u);
                }
                returned.retainAll(v);
                return returned;
            }
        };
    }

    /**
     * 
     * @param returned
     * @param clazz
     * @param equiv
     * @return
     */
    public static final <T, U extends Collection<T>> BinaryOperator<U> intersect(U returned, Class<T> clazz,
            BiPredicate<T, T> equiv)
    {
        return new BinaryOperator<U>()
        {
            @Override
            public U apply(U u, U v)
            {
                if (returned.isEmpty())
                {
                    returned.addAll(u);
                }
                returned.retainAll(returned.stream()
                        .filter((eltU) -> v.stream().filter((eltV) -> equiv.test(eltU, eltV)).count() > 0)
                        .collect(Collectors.toList()));
                return returned;
            }
        };
    }

    /**
     * 
     * @param returned
     * @param clazz
     * @return
     */
    public static final <T, U extends Collection<T>> BinaryOperator<U> except(U returned, Class<T> clazz)
    {
        return new BinaryOperator<U>()
        {
            @Override
            public U apply(U u, U v)
            {
                if (returned.isEmpty())
                {
                    returned.addAll(u);
                }
                returned.removeAll(v);
                return returned;
            }
        };
    }

    public static class Maps
    {
        private Maps()
        {
        }

        public static final <K, V, U extends Map<K, V>> BinaryOperator<U> union(U returned)
        {
            return new BinaryOperator<U>()
            {
                @Override
                public U apply(U u, U v)
                {
                    returned.putAll(u);
                    returned.putAll(v);
                    return returned;
                }
            };
        }
    }

    /**
     * TODO refactor<br/>
     * Opérations sur les ensembles
     *
     */
    public static class Sets
    {
        private Sets()
        {
        }

        /**
         * TODO refactor
         * 
         * @param clazz
         * @return
         */
        public static final <T> BinaryOperator<Set<T>> union(Class<T> clazz)
        {
            return new BinaryOperator<Set<T>>()
            {
                @Override
                public Set<T> apply(Set<T> u, Set<T> v)
                {
                    Set<T> returned = new HashSet<>(u);
                    returned.addAll(v);
                    return returned;
                }
            };
        }

        /**
         *
         * @param u
         * @param v
         * @return l'ensemble des éléments qui sont dans {@code u} ou dans
         *         {@code v}
         */
        public static final <T> Set<T> union(Set<T> u, Set<T> v)
        {
            Set<T> returned = new HashSet<>(u);
            returned.addAll(v);
            return returned;
        }

        /**
         *
         * @param u
         * @param v
         * @return l'ensemble des éléments qui sont dans {@code u} et {@code v}
         */
        public static final <T> Set<T> inter(Set<T> u, Set<T> v)
        {
            Set<T> returned = new HashSet<>(u);
            returned.retainAll(v);
            return returned;
        }

        /**
         *
         * @param u
         * @param v
         * @return l'ensemble des éléments qui sont dans {@code u} mais pas dans
         *         {@code v}
         */
        public static final <T> Set<T> minus(Set<T> u, Set<T> v)
        {
            Set<T> returned = new HashSet<>(u);
            returned.removeAll(v);
            return returned;
        }
    }

    public static <T> List<T> fillArray(List<List<T>> lists)
    {
        List<T> returned = new ArrayList<T>();
        for (int i = 0; i < lists.size(); i++)
        {
            returned.addAll(lists.get(i));
        }
        return returned;
    }
    
    /**
     * Rempli une map à partir d'une liste.
     * Petit test pour s'assurer que la liste a bien un nombre pair d'élément.
     * @param returned
     * @param asList
     * @return
     */
    public static final <T> Map<T, T> fillMap(Map<T, T> returned, List<T> asList)
    {
        if(asList.size()%2==1){
            throw new IllegalStateException("La liste n'a pas un nombre pair d'élément");
        }
        for (int i=1;i<asList.size();i=i+2)
        {
                returned.put(asList.get(i-1), asList.get(i));
        }
        return returned;
    }

    /**
     * Remplit la {@link Map} {@code returned} passée en paramètre avec les
     * paires {@code K, V} de la liste {@code asList} passée en paramètre.
     *
     * @param asList
     *            une liste de paires {@code K, V}
     * @param returned
     *            la {@link Map} à remplir avec les paires de {@code asList}
     * @return la {@link Map} remplie avec les paires de {@code asList}
     */
    public static final <K, V> Map<K, V> fillMap(List<Pair<K, V>> asList, Map<K, V> returned)
    {
        for (Pair<K, V> pair : asList)
        {
            returned.put(pair.getFirst(), pair.getSecond());
        }
        return returned;
    }

    /**
     * Remplit la {@link Map} {@code returned} passée en paramètre avec les
     * clefs de {@code keys} et les valeurs de {@code returned}, de sorte que si
     * <code>keys.get(i) = a</code>, alors
     * <code>returned.get(a) = values.get(i)</code>
     *
     * @param keys
     *            la liste des clefs {@code K}
     * @param values
     *            la liste des valeurs {@code V}
     * @param returned
     *            la {@link Map} à remplir
     * @return la {@link Map} remplie avec les clefs de {@code keys} et les
     *         valeurs de {@code returned}, de sorte que si
     *         <code>keys.get(i) = a</code>, alors
     *         <code>returned.get(a) = values.get(i)</code>
     */
    public static final <K, V> Map<K, V> fillMap(List<K> keys, List<V> values, Map<K, V> returned)
    {
        checkRanges(keys, values);
        for (int i = 0; i < keys.size(); i++)
        {
            returned.put(keys.get(i), values.get(i));
        }
        return returned;
    }

    /**
     * Vérifie que les collections passées en paramètre ont toutes la même
     * taille
     *
     * @param collections
     * @throws IllegalStateException
     *             si une collection au moins n'a pas la même taille que les
     *             autres
     */
    @SuppressWarnings("rawtypes")
    public static final void checkRanges(Collection... collections)
    {
        if (collections == null || collections.length == 0) { return; }
        int size = collections[0].size();
        checkRanges(size, collections);
    }

    /**
     * Vérifie que les collections passées en paramètre sont toutes de taille
     * {@code length}
     *
     * @param length
     * @param collections
     * @throws IllegalStateException
     *             si une collection au moins n'a pas la même taille que les
     *             autres
     */
    @SuppressWarnings("rawtypes")
    public static final void checkRanges(int size, Collection... collections)
    {
        for (Collection collection : collections)
        {
            if (collection.size() != size) { throw new IllegalStateException(
                    "Les collections ne sont pas toutes de taille " + size + "."); }
        }
    }

    public static <K, V> Map<K, V> newHashMap(List<K> keys, List<V> values)
    {
        checkRanges(keys, values);
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < keys.size(); i++)
        {
            map.put(keys.get(i), values.get(i));
        }
        return map;
    }
}
