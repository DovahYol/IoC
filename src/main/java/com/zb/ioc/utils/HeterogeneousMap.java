package com.zb.ioc.utils;

import java.util.HashMap;
import java.util.Map;

public class HeterogeneousMap extends HashMap implements Map {
    public <T> T get(Class<T> key) {
        @SuppressWarnings("unchecked")
        T result = (T)super.get(key);
        return result;
    }

    public <T> T put(Class<T> key, T value) {
        @SuppressWarnings("unchecked")
        T result = (T)super.put(key, value);
        return result;
    }
}
