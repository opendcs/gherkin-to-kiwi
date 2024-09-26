package org.opendcs.testing.gradle;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public abstract class OutputConfig
{
    private final String name;

    @javax.inject.Inject
    public OutputConfig(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public abstract Property<String> getType();
    public abstract Property<String> getUrl();
    public abstract Property<String> getUsername();
    public abstract Property<String> getPassword();
    public abstract Property<String> getVersion();
    public abstract Property<String> getProduct();
    public abstract ListProperty<String> getSelectedPlans();
}
