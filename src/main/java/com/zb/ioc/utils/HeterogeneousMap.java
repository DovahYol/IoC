package com.zb.ioc.utils;

import java.util.HashMap;
import java.util.Map;

public class HeterogeneousMap extends HashMap implements Map {
    public <T> T getObject(T key) {
        @SuppressWarnings("unchecked")
        T result = (T)super.get(key);
        return result;
    }
}
