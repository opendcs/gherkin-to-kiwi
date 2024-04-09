package org.opendcs.testing.kiwi;

public class Component
{
    public final Product product;
    public final String name;
    public final String initialOwner;
    public final String initialQaContact;
    public final String description;

    public Component(Product product, String name)
    {
        this.product = product;
        this.name = name;
        this.initialOwner = null;
        this.initialQaContact = null;
        this.description = null;
    }
}
