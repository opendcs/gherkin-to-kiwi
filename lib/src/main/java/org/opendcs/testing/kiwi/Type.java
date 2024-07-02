package org.opendcs.testing.kiwi;

import java.util.HashMap;
import java.util.Map;

public class Type
{
    private static Map<Long,Type> typeMap = new HashMap<>();

    public final long id;
    public final String name;

    private Type(String name)
    {
        this(-1L, name);
    }

    private Type(long id, String name)
    {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString()
    {
        return String.format("Type{id=%d,name=%s}", id, name);
    }


    public static Type of(long id, String name)
    {
        return typeMap.computeIfAbsent(id, key ->
        {
            return new Type(id,name);
        });
    }

    public static Type of(final String name)
    {
        return typeMap.values()
                      .stream()
                      .filter(p -> p.name.equals(name))
                      .findFirst()
                      .orElseGet(() -> new Type(name));
    }
}
