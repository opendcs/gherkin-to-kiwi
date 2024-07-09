package org.opendcs.testing.ant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.opendcs.testing.PlanDefinition;
import org.opendcs.testing.gherkin.ProcessingError;
import org.opendcs.testing.kiwi.TestUtils;
import org.opendcs.testing.rpc.KiwiClient;


public class GherkinKiwiTask extends Task
{

    /**
     * Defaults to Project name if attribute not set by the user.
     */
    private String productName = null;
    private String url = null;
    private String username = null;
    private String password = null;
    private FileSet files = null;
    private Project proj = null;
    private String version = null;
    private Path planDefinitionsFile = null;
    private Map<String, PlanDefinition> planDefinitions = null;

    public GherkinKiwiTask()
    {

    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public void setPlanDefinitions(String planDefinitionsFile)
    {
        this.proj.log(this, "Plan Def File = " + planDefinitionsFile, Project.MSG_INFO);
        this.planDefinitionsFile = Paths.get(planDefinitionsFile);
    }

    public void addConfiguredFileSet(FileSet files)
    {
        this.files = files;
    }

    @Override
    public void init() throws BuildException
    {
        this.proj = getProject();
    }

    public void validate_settings() throws BuildException
    {
        if (this.productName == null)
        {
            this.productName = proj.getName();
        }

        if (url == null)
        {
            throw new BuildException("'url' attribute must be set", getLocation());
        }

        if (username == null)
        {
            throw new BuildException("'username' attribute must be set", getLocation());
        }

        if (password == null)
        {
            throw new BuildException("'password' attribute must be set", getLocation());
        }

        if (files == null)
        {
            throw new BuildException("A nested FileSet must be provided.", getLocation());
        }

        if (planDefinitionsFile == null)
        {
            throw new BuildException("A 'planDefinitions' attribute must be set to a file"
                    + " containing a json list of test plan defitions.", getLocation());
        }

        try
        {
            this.planDefinitions = PlanDefinition.from(this.planDefinitionsFile);
        }
        catch (IOException ex)
        {
            throw new BuildException("Unable to load test plan definitions", ex);
        }
    }

    @Override
    public void execute() throws BuildException
    {
        validate_settings();
        try
        {
            final KiwiClient client = new KiwiClient(url, username, password);
            proj.log(this, "Creating test cases for '" + productName + "' at url '" + url + "'", Project.MSG_INFO);
            Consumer<ProcessingError> onError = err ->
            {
                if (err.getMessage() != null && err.getCause() != null)
                {
                    throw new BuildException(err.getMessage(), err.getCause());
                }
                else if (err.getCause() != null)
                {
                    throw new BuildException("Unable to process feature file", err.getCause());
                }
                else
                {
                    throw new BuildException(err.getMessage());
                }
            };
            proj.log(this, "Processing and Saving Test Sets.", Project.MSG_VERBOSE);
            TestUtils.processAndSaveData(client,
                    productName,
                    version,
                    files.stream().map(r -> new File(r.getName()).toPath()),
                    planDefinitions,
                    obj -> proj.log(GherkinKiwiTask.this, obj.toString(), Project.MSG_VERBOSE),
                    onError);
        }
        catch (IOException ex)
        {
            throw new BuildException("Unable to connect to Kiwi Client: " + ex.getLocalizedMessage(), ex, getLocation());
        }
    }
}
