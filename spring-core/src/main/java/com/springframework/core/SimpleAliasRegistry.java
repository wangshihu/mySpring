package com.springframework.core;

import com.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hadoop on 2015/5/8 0008.
 */
public class SimpleAliasRegistry implements AliasRegistry {

    /** Map from alias to canonical name */
    private final Map<String, String> aliasMap = new ConcurrentHashMap<String, String>(16);


    @Override
    public void registerAlias(String name, String alias) {
        Assert.hasText(name, "'name' must not be empty");
        Assert.hasText(alias, "'alias' must not be empty");
        if (alias.equals(name)) {
            this.aliasMap.remove(alias);
        }
        else {
            if (!allowAliasOverriding()) {
                String registeredName = this.aliasMap.get(alias);
                if (registeredName != null && !registeredName.equals(name)) {
                    throw new IllegalStateException("Cannot register alias '" + alias + "' for name '" +
                            name + "': It is already registered for name '" + registeredName + "'.");
                }
            }
            checkForAliasCircle(name, alias);
            this.aliasMap.put(alias, name);
        }
    }

    /**
     * Return whether alias overriding is allowed.
     * Default is {@code true}.
     */
    protected boolean allowAliasOverriding() {
        return true;
    }


    @Override
    public void removeAlias(String alias) {

    }

    @Override
    public boolean isAlias(String beanName) {
        return false;
    }

    @Override
    public String[] getAliases(String name) {
        return new String[0];
    }

    /**
     * Determine the raw name, resolving aliases to canonical names.
     * @param name the user-specified name
     * @return the transformed name
     */
    public String canonicalName(String name) {
        String canonicalName = name;
        // Handle aliasing...
        String resolvedName;
        do {
            resolvedName = this.aliasMap.get(canonicalName);
            if (resolvedName != null) {
                canonicalName = resolvedName;
            }
        }
        while (resolvedName != null);
        return canonicalName;
    }

    /**
     * Check whether the given name points back to given alias as an alias
     * in the other direction, catching a circular reference upfront and
     * throwing a corresponding IllegalStateException.
     * @param name the candidate name
     * @param alias the candidate alias
     * @see #registerAlias
     */
    protected void checkForAliasCircle(String name, String alias) {
        if (alias.equals(canonicalName(name))) {
            throw new IllegalStateException("Cannot register alias '" + alias +
                    "' for name '" + name + "': Circular reference - '" +
                    name + "' is a direct or indirect alias for '" + alias + "' already");
        }
    }
}
