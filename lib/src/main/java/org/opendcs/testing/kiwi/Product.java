package org.opendcs.testing.kiwi;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Product {
    public final long id;
    public final String name;

    private static final Map<Long,Product> products = new HashMap<>();

    private Product(String name) {
        this(-1, name);
    }

    private Product(long id, String name) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String toString() {
        return "Product{id="+id+",name="+name +"}";
    }

    public static Product of(long id, String name) {
        return products.computeIfAbsent(id, key -> new Product(id, name));
    }

    public static Product of(String name) {
        return products.values()
                .stream()
                .filter(p -> p.name.equals(name))
                .findFirst()
                .orElseGet(() -> new Product(name));
    }

    public static Optional<Product> existingOfId(long id)
    {
        return Optional.ofNullable(products.getOrDefault(id, null));
    }
}
