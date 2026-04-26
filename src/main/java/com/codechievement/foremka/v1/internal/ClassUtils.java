package com.codechievement.foremka.v1.internal;

public class ClassUtils {

    public static <T> Class<? extends T> forName(String className) throws ClassNotFoundException {
        //noinspection unchecked
        return (Class<? extends T>) Class.forName(className);
    }
}
