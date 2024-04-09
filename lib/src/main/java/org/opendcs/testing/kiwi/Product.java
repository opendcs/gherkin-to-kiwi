package org.opendcs.testing.kiwi;

public class Product {
    public final long id;
    public final String name;

    public Product(String name) {
        this.name = name;
        this.id = -1;
    }

    public Product(long id, String name) {
        this.name = name;
        this.id = id;
    }
}
