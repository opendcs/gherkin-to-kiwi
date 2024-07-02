package org.opendcs.testing.kiwi;

import java.util.HashMap;
import java.util.Map;

public class Version
{
    private static final Map<Long, Version> versions = new HashMap<>();

    public final long id;
    public final String name;
    public final Product product;
    
    private Version(String name, Product product)
    {
        this(-1, name, product);
    }

    private Version(long id, String name, Product product)
    {
        this.id = id;
        this.name = name;
        this.product = product;
    }

    public static Version of(long id, String name, Product product)
    {
        return versions.computeIfAbsent(id, key ->
        {
            return new Version(id, name, product);
        });
    }    

    public static Version of(String name, Product p)
    {
        return versions.values()
                       .stream()
                       .filter(v -> v.name.equals(name) && v.product.name.equals(p.name))
                       .findFirst()
                       .orElseGet(() -> new Version(name, p)); 

    }

}
