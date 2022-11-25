package com.statsmind.commons;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapUtil {
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, Order order) {
        return sortByValue(map, new Comparator<V>() {
            @Override
            public int compare(V o1, V o2) {
                return order == Order.ASC ? o1.compareTo(o2) : o2.compareTo(o1);
            }
        });
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return sortByValue(map, Order.ASC);
    }

    public static <K, V> Map<K, V> sortByValue(Map<K, V> map, Comparator<V> comparator) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue(comparator));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(Map<K, V> map, Order order) {
        return sortByKey(map, new Comparator<K>() {
            @Override
            public int compare(K o1, K o2) {
                return order == Order.ASC ? o1.compareTo(o2) : o2.compareTo(o1);
            }
        });
    }

    public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(Map<K, V> map) {
        return sortByKey(map, Order.ASC);
    }

    public static <K, V> Map<K, V> sortByKey(Map<K, V> map, Comparator<K> comparator) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByKey(comparator));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }


    public static <T, U extends Comparable<? super U>> Map<U, T> toMap(Stream<T> items, Function<? super T, ? extends U> keyExtractor) {
        /**
         * public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
         *             Function<? super T, ? extends U> keyExtractor)
         */
        return items.collect(Collectors.toMap(keyExtractor, m -> m));
    }

    public static <T, U extends Comparable<? super U>> Map<U, T> toMap(Collection<T> items, Function<? super T, ? extends U> keyExtractor) {
        /**
         * public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
         *             Function<? super T, ? extends U> keyExtractor)
         */
        return items.stream().collect(Collectors.toMap(keyExtractor, m -> m));
    }

    public static <T, V, U extends Comparable<? super U>> Map<U, V> toMap(Collection<T> items, Function<? super T, ? extends U> keyExtractor, Function<? super T, ? extends V> valueExtractor) {
        /**
         * public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
         *             Function<? super T, ? extends U> keyExtractor)
         */
        return items.stream().collect(Collectors.toMap(keyExtractor, valueExtractor));
    }

    public static <T, V, U extends Comparable<? super U>> Map<U, V> toMap(Stream<T> items, Function<? super T, ? extends U> keyExtractor, Function<? super T, ? extends V> valueExtractor) {
        /**
         * public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
         *             Function<? super T, ? extends U> keyExtractor)
         */
        return items.collect(Collectors.toMap(keyExtractor, valueExtractor));
    }

    public static <T, V, U extends Comparable<? super U>> Map<U, List<V>> toStackedMap(Collection<T> items, Function<? super T, ? extends U> keyExtractor, Function<? super T, ? extends V> valueExtractor) {
        /**
         * public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
         *             Function<? super T, ? extends U> keyExtractor)
         */
        Map<U, List<V>> stackedMap = new LinkedHashMap<U, List<V>>(items.size());
        for (T item : items) {
            U key = keyExtractor.apply(item);
            if (!stackedMap.containsKey(key)) {
                stackedMap.put(key, new ArrayList<>());
            }

            List<V> vs = stackedMap.get(key);

            V val = valueExtractor.apply(item);
            if (!vs.contains(val)) {
                vs.add(val);
            }
        }

        return stackedMap;
    }

    public static Map of(Object... kvPairs) {
        Map map = new LinkedHashMap();
        for (int i = 0; i < (kvPairs.length + 1) / 2; ++i) {
            map.put(kvPairs[2 * i], kvPairs[2 * i + 1]);
        }

        return map;
    }

    public enum Order {
        ASC, DESC
    }
}
