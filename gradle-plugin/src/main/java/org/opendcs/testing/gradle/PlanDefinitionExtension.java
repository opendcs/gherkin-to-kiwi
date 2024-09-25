package org.opendcs.testing.gradle;

import org.gradle.api.provider.Property;

public abstract class PlanDefinitionExtension
{
    private final String id;

    @javax.inject.Inject
    public PlanDefinitionExtension(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }
    public abstract Property<String> getName();
    public abstract Property<String> getType();    
}
