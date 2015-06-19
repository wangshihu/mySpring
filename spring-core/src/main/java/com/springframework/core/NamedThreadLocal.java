package com.springframework.core;

/**
 * Created by hadoop on 2015/5/8 0008.
 */
public class NamedThreadLocal<T> extends ThreadLocal<T> {
    private final String name;

    public NamedThreadLocal(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
