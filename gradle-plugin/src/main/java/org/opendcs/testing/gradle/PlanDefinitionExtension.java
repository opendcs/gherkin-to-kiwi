package org.opendcs.testing.gradle;

import org.gradle.api.provider.Property;

public interface PlanDefinitionExtension
{
    String getName();
    Property<String> getPlanName();
    Property<String> getType();
}
