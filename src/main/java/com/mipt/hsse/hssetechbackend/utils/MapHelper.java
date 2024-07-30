package com.mipt.hsse.hssetechbackend.utils;

import java.util.HashMap;
import java.util.Map;

public final class MapHelper {
    private MapHelper() {}

    public static <K, V> Map<K, V> copyOf(Map<K, V> original) {
        return new HashMap<>(original);
    }
}
