package org.opendcs.testing.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

public abstract class GherkinKiwiPlugin implements Plugin<Project> 
{
    @Override
    public void apply(Project project) 
    {
        GherkinKiwiExtension kiwi = project.getExtensions().create("kiwi", GherkinKiwiExtension.class);
        kiwi.getProduct().convention(project.getName());
        kiwi.getFeatureFiles().convention(project.getLayout().getProjectDirectory().dir("src/test/resources/features"));
        final TaskContainer tasks = project.getTasks();
        System.out.println("Have " + kiwi.getOutputs().size() + " outputs.");
        final Task outputTask = tasks.create("outputTestCases");
        kiwi.getOutputs().all(output ->
        {
            output.getProduct().convention(kiwi.getProduct());

            final String name = output.getName();
            project.getLogger().info("Processing output {} of type", name);
            TaskProvider<KiwiOutputTask> pushTask = tasks.register("testOutput"+name, KiwiOutputTask.class, kiwiPush ->
            {
                kiwiPush.featureFiles = kiwi.getFeatureFiles();
                kiwiPush.product = output.getProduct();
                kiwiPush.type = output.getType();
                kiwiPush.url = output.getUrl();
                kiwiPush.username = output.getUsername();
                kiwiPush.password = output.getPassword();
                kiwiPush.version = output.getVersion();
            });
            outputTask.dependsOn(pushTask);
        });
    }
}
