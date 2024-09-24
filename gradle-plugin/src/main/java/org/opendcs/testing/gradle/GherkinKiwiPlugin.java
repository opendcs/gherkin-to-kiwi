package org.opendcs.testing.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GherkinKiwiPlugin implements Plugin<Project> 
{
    @Override
    public void apply(Project project) 
    {
        project.task("hello")
               .doLast(task -> System.out.println("Hello Gradle!"));
    }
}
