package org.opendcs.testing.kiwi;

import java.util.HashMap;
import java.util.Map;

/**
 * Component covered by a given test case
 */
public class Component
{
    public final long id;
    public final Product product;
    public final String name;
    public final String initialOwner;
    public final String initialQaContact;
    public final String description;

    private final static Map<Long, Component> components = new HashMap<>();

    private Component(Product product, String name)
    {
        this(-1, product, name);
    }

    private Component(long id, Product product, String name)
    {
        this.id = id;
        this.product = product;
        this.name = name;
        this.initialOwner = null;
        this.initialQaContact = null;
        this.description = null;
    }

    @Override
    public String toString()
    {
        return "Component{id=" + id + ",name=" + name + ",Product=" + product.name + "}";
    }

    public static Component of(Long id, Product product, String name)
    {
        return components.computeIfAbsent(id,
                key -> new Component(id, product, name));
    }

    public static Component of(Product product, String name)
    {
        return components.values()
                .stream()
                .filter(c -> c.name.equals(name) && c.product.name.equals(product.name))
                .findFirst()
                .orElseGet(() -> new Component(product, name));
    }
}
