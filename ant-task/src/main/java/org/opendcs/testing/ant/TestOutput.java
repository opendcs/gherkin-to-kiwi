package org.opendcs.testing.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

public abstract class TestOutput extends Task
{
    public abstract void run(FileSet files) throws BuildException;
    public abstract void validate_settings() throws BuildException;
}
