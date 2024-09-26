package org.opendcs.testing.gradle;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.BuildException;
import org.opendcs.testing.PlanDefinition;
import org.opendcs.testing.gherkin.ProcessingError;
import org.opendcs.testing.kiwi.TestUtils;
import org.opendcs.testing.rpc.KiwiClient;

public abstract class KiwiOutputTask extends DefaultTask
{  
    @InputDirectory
    DirectoryProperty featureFiles;

    @Input
    Property<String> product;
    @Input
    Property<String> type;
    @Input
    Property<String> url;
    @Input
    Property<String> username;
    @Input
    Property<String> password;
    @Input
    Property<String> version;

    @Input
    ListProperty<String> selectedPlans;

    @Input
    ListProperty<PlanDefinition> plans;

    @TaskAction
    public void storeData()
    {
        
        Consumer<ProcessingError> onError = err ->
        {
            if (err.getMessage() != null && err.getCause() != null)
            {
                throw new GradleException(err.getMessage(), err.getCause());
            }
            else if (err.getCause() != null)
            {
                throw new GradleException("Unable to process feature file", err.getCause());
            }
            else
            {
                throw new GradleException(err.getMessage());
            }
        };
        System.err.println("Processing Features");
        Stream<Path> files = featureFiles.getAsFileTree()
            .getFiles().stream()
            .map(File::toPath);
        
        if (plans.get().isEmpty())
        {
            throw new RuntimeException("No plans have been defined.");
        }
        
        try
        {
            final Map<String,PlanDefinition> planDefs = new HashMap<>();
            System.out.println("Plan size" + plans.get().size());
            plans.get().forEach(pd ->
            {
                System.out.println("Creating/Updating plan " + pd.id);
                planDefs.put(pd.id, pd);
            });
            if (!url.isPresent())
            {
                //getProject().getLogger().error("Kiwi url is not set.");
                throw new InvalidUserDataException("Kiwi url is not set.");
            }
            KiwiClient client = new KiwiClient(url.get(), username.get(), password.get());
            TestUtils.processAndSaveData(client,
                                         product.get(),
                                         version.get(),
                                         files,
                                         planDefs,
                                         obj -> getProject().getLogger().debug(obj.toString()),
                                         onError);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Operations failed.", ex);
        }
    }

    public DirectoryProperty getFeatureFiles()
    {
        return featureFiles;
    }

    public Property<String> getProduct()
    {
        return product;
    }

    public Property<String> getType()
    {
        return type;
    }

    public Property<String> getUrl()
    {
        return url;
    }

    public Property<String> getUsername()
    {
        return username;
    }

    public Property<String> getPassword()
    {
        return password;
    }

    public Property<String> getVersion()
    {
        return version;
    }

    public ListProperty<String> getSelectedPlans()
    {
        return selectedPlans;
    }

    public ListProperty<PlanDefinition> getPlans()
    {
        return plans;
    }
}
