package com.virjar.thanos.util;

import com.google.common.base.Defaults;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TypeUtils {
    /**
     * Maps names of primitives to their corresponding primitive {@code Class}es.
     */
    private static final Map<String, Class<?>> namePrimitiveMap = new HashMap<>();

    static {
        namePrimitiveMap.put("boolean", Boolean.TYPE);
        namePrimitiveMap.put("byte", Byte.TYPE);
        namePrimitiveMap.put("char", Character.TYPE);
        namePrimitiveMap.put("short", Short.TYPE);
        namePrimitiveMap.put("int", Integer.TYPE);
        namePrimitiveMap.put("long", Long.TYPE);
        namePrimitiveMap.put("double", Double.TYPE);
        namePrimitiveMap.put("float", Float.TYPE);
        namePrimitiveMap.put("void", Void.TYPE);
    }

    /**
     * Maps primitive {@code Class}es to their corresponding wrapper {@code Class}.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();

    static {
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }

    /**
     * Maps a primitive class name to its corresponding abbreviation used in array class names.
     */
    private static final Map<String, String> abbreviationMap;

    /**
     * Maps an abbreviation used in array class names to corresponding primitive class name.
     */
    private static final Map<String, String> reverseAbbreviationMap;

    /**
     * Feed abbreviation maps
     */
    static {
        final Map<String, String> m = new HashMap<>();
        m.put("int", "I");
        m.put("boolean", "Z");
        m.put("float", "F");
        m.put("long", "J");
        m.put("short", "S");
        m.put("byte", "B");
        m.put("double", "D");
        m.put("char", "C");
        final Map<String, String> r = new HashMap<>();
        for (final Map.Entry<String, String> e : m.entrySet()) {
            r.put(e.getValue(), e.getKey());
        }
        abbreviationMap = Collections.unmodifiableMap(m);
        reverseAbbreviationMap = Collections.unmodifiableMap(r);
    }

    /**
     * Maps wrapper {@code Class}es to their corresponding primitive types.
     */
    private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<>();

    static {
        for (final Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperMap.entrySet()) {
            final Class<?> primitiveClass = entry.getKey();
            final Class<?> wrapperClass = entry.getValue();
            if (!primitiveClass.equals(wrapperClass)) {
                wrapperPrimitiveMap.put(wrapperClass, primitiveClass);
            }
        }
    }

    public static Class<?> wrapperToPrimitive(final Class<?> cls) {
        if (cls.isPrimitive()) {
            return cls;
        }
        return wrapperPrimitiveMap.get(cls);
    }

    @SuppressWarnings("unchecked")
    public static <T> T primitiveCast(String value, Class<T> type) {

        if (value == null) {
            if (type.isPrimitive()) {
                return Defaults.defaultValue(type);
            }
            if (wrapperPrimitiveMap.containsKey(type)) {
                return (T) Defaults.defaultValue(wrapperToPrimitive(type));
            }
            return null;
        }

        if (type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        if (type.isAssignableFrom(String.class)) {
            return (T) value;
        }

        Class<?> primitiveType = wrapperToPrimitive(type);
        if (primitiveType == null) {
            return null;
        }
        if (type == boolean.class) {
            return (T) Boolean.valueOf(value);
        } else if (type == char.class) {
            return (T) Character.valueOf(value.charAt(0));
        } else if (type == byte.class) {
            return (T) Byte.valueOf(value);
        } else if (type == short.class) {
            return (T) Short.valueOf(value);
        } else if (type == int.class) {
            return (T) Integer.valueOf(value);
        } else if (type == long.class) {
            return (T) Long.valueOf(value);
        } else if (type == float.class) {
            return (T) Float.valueOf(value);
        } else if (type == double.class) {
            return (T) Double.valueOf(value);
        } else {
            return null;
        }
    }
}
