package org.opendcs.testing.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;

public interface GherkinKiwiExtension
{ 
    @Input
    Property<String> getProduct();
    @InputDirectory
    DirectoryProperty getFeatureFiles();
}
