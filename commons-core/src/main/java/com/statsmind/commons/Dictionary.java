package com.statsmind.commons;

import java.util.HashMap;

public class Dictionary<T> extends HashMap<T, Integer> {
    public int add(T t) {
        if (containsKey(t)) {
            return get(t);
        } else {
            int size = size();
            put(t, size);
            return size;
        }
    }

    public T getKeyByValue(int v) {
        for (T key : keySet()) {
            if (get(key) == v) {
                return key;
            }
        }

        return null;
    }
}