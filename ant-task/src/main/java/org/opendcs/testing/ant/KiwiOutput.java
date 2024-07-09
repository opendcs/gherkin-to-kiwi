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

public class KiwiOutput extends TestOutput
{
    private Project project;
    /**
     * Defaults to Project name if attribute not set by the user.
     */
    private String productName = null;
    private String url = null;
    private String username = null;
    private String password = null;
    private String version = null;
    private Path planDefinitionsFile = null;
    private Map<String, PlanDefinition> planDefinitions = null;

    public KiwiOutput(Project project)
    {
        this.project = project;
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
        this.project.log(this, "Plan Def File = " + planDefinitionsFile, Project.MSG_INFO);
        this.planDefinitionsFile = Paths.get(planDefinitionsFile);
    }

    @Override
    public void run(FileSet files) throws BuildException
    {
        try
        {
            final KiwiClient client = new KiwiClient(url, username, password);
            project.log(this, "Creating test cases for '" + productName + "' at url '" + url + "'", Project.MSG_INFO);
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
            project.log(this, "Processing and Saving Test Sets.", Project.MSG_VERBOSE);
            TestUtils.processAndSaveData(client,
                    productName,
                    version,
                    files.stream().map(r -> new File(r.getName()).toPath()),
                    planDefinitions,
                    obj -> project.log(this, obj.toString(), Project.MSG_VERBOSE),
                    onError);
        }
        catch (IOException ex)
        {
            throw new BuildException("Unable to connect to Kiwi Client: " + ex.getLocalizedMessage(), ex, this.getLocation());
        }
    }

    @Override
    public void validate_settings() throws BuildException
    {
        if (this.productName == null)
        {
            this.productName = project.getName();
        }

        if (url == null)
        {
            throw new BuildException("'url' attribute must be set", this.getLocation());
        }

        if (username == null)
        {
            throw new BuildException("'username' attribute must be set", this.getLocation());
        }

        if (password == null)
        {
            throw new BuildException("'password' attribute must be set", this.getLocation());
        }

        if (planDefinitionsFile == null)
        {
            throw new BuildException("A 'planDefinitions' attribute must be set to a file"
                    + " containing a json list of test plan defitions.", this.getLocation());
        }

        try
        {
            this.planDefinitions = PlanDefinition.from(this.planDefinitionsFile);
        }
        catch (IOException ex)
        {
            throw new BuildException("Unable to load test plan definitions", ex, this.getLocation());
        }
    }
    
}
