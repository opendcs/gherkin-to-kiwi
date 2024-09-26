package org.opendcs.testing.gradle;
import javax.xml.ws.spi.Provider;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.opendcs.testing.PlanDefinition;

public abstract class GherkinKiwiPlugin implements Plugin<Project> 
{
    @Override
    public void apply(Project project) 
    {
        GherkinKiwiExtension kiwi = project.getExtensions().create("kiwi", GherkinKiwiExtension.class);
        kiwi.getProduct().convention(project.getName());
        kiwi.getFeatureFiles().convention(project.getLayout().getProjectDirectory().dir("src/test/resources/features"));
        final ListProperty<PlanDefinition> plans = project.getObjects().listProperty(PlanDefinition.class);
        final TaskContainer tasks = project.getTasks();
        final Task outputTask = tasks.create("outputTestCases");

        kiwi.getPlans().whenObjectAdded(pd ->
        {
            System.out.println("Adding " + pd.getName());
            plans.add(
                project.provider(
                    () -> new PlanDefinition(pd.getName(),
                                             pd.getPlanName().get(),
                                             pd.getType().get())));
        });

        kiwi.getOutputs().all(output ->
        {
            output.getSelectedPlans().convention(project.getObjects().listProperty(String.class));
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
                kiwiPush.plans = plans;
                kiwiPush.selectedPlans = output.getSelectedPlans();
            });
            outputTask.dependsOn(pushTask);
        });
    }
}
