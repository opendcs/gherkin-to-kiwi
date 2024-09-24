package org.opendcs.testing.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;

public abstract class GherkinKiwiPlugin implements Plugin<Project> 
{
    @Override
    public void apply(Project project) 
    {
        project.getPluginManager().apply(BasePlugin.class);

        GherkinKiwiExtension kiwi = project.getExtensions().create("kiwi", GherkinKiwiExtension.class);
        kiwi.getProduct().convention(project.getName());
        kiwi.getFeatureFiles().convention(project.getLayout().getProjectDirectory().dir("src/test/resources/features"));
        project.getTasks().register("kiwiPush", KiwiOutputTask.class, kiwiPush ->
        {
            kiwiPush.featureFiles = kiwi.getFeatureFiles();
            kiwiPush.product = kiwi.getProduct();
        });
        
    }
}
