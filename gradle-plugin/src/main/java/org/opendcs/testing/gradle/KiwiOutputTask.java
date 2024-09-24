package org.opendcs.testing.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

public abstract class KiwiOutputTask extends DefaultTask
{  
    @InputDirectory
    DirectoryProperty featureFiles;

    @Input
    Property<String> product;

    @TaskAction
    public void storeData()
    {
        System.err.println("Processing Features");
        featureFiles.getAsFileTree()
            .getFiles().stream()
            .filter(f -> f.getName().endsWith(".feature"))
            .map(f -> f.getAbsolutePath())
            .forEach(System.out::println);
    }

    public DirectoryProperty getFeatureFiles()
    {
        return featureFiles;
    }

    public Property<String> getProduct()
    {
        return product;
    }
}
