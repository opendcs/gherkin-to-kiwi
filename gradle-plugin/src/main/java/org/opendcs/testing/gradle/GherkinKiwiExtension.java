package org.opendcs.testing.gradle;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Nested;

public interface GherkinKiwiExtension
{ 
    /**
     * Kiwi Product Name. Defaults to Gradle Project Name
     * @return
     */
    @Input
    Property<String> getProduct();

    /**
     * Gherkin Feature files to process into manual test cases
     * Default is `$projectDir/src/test/features/*.feature`
     * @return
     */
    @InputDirectory
    DirectoryProperty getFeatureFiles();

    /**
     * Test Plans That may be used in the provided feature files.
     * @return
     */
    @Input
    @Nested
    NamedDomainObjectContainer<PlanDefinitionExtension> getPlans();

    /**
     * Defined places to push processed tests.
     * @return
     */
    @Input
    @Nested
    NamedDomainObjectContainer<OutputConfig>  getOutputs();
}
