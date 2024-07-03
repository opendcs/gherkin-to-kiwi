package org.opendcs.testing.kiwi;

import java.util.HashMap;
import java.util.Map;

/**
 * Test category for a given test case
 */
public class Category
{
    public final long id;
    public final Product product;
    public final String name;

    private static final Map<Long, Category> categories = new HashMap<>();

    private Category(Product product, String name)
    {
        this(-1, product, name);
    }

    private Category(long id, Product product, String name)
    {
        this.product = product;
        this.name = name;
        this.id = id;
    }

    public static Category of(long id, Product product, String name)
    {
        return categories.computeIfAbsent(id, key -> new Category(id, product, name));
    }

    public static Category of(Product product, String name)
    {
        return categories.values()
                .stream()
                .filter(c -> c.name.equals(name) && c.product.name.equals(product.name))
                .findFirst()
                .orElseGet(() -> new Category(product, name));
    }
}
