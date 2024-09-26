package org.opendcs.testing.gradle;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Nested;

public interface GherkinKiwiExtension
{ 
    @Input
    Property<String> getProduct();
    @InputDirectory
    DirectoryProperty getFeatureFiles();
    @Input
    @Nested
    NamedDomainObjectContainer<PlanDefinitionExtension> getPlans();
    @Input
    @Nested
    NamedDomainObjectContainer<OutputConfig>  getOutputs();


    //void plans(Action<? super NamedDomainObjectContainer<? super PlanDefinitionExtension>> action);
}
