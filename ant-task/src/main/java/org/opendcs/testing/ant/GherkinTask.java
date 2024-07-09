package org.opendcs.testing.ant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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


public class GherkinTask extends Task
{
    private FileSet files = null;
    private Project proj = null;
    private List<TestOutput> outputs = new ArrayList<>();
    
    public GherkinTask()
    {

    }

    public void addConfiguredKiwi(KiwiOutput output)
    {
        outputs.add(output);
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
        if (files == null)
        {
            throw new BuildException("A nested FileSet must be provided.", getLocation());
        }

        proj.log(this, "Generating and saving Manual test cases.", Project.MSG_INFO);
        for (TestOutput output: outputs)
        {
            output.validate_settings();
        }
    }

    @Override
    public void execute() throws BuildException
    {
        validate_settings();
        for (TestOutput output: outputs)
        {
            output.run(files);
        }
    }
}
