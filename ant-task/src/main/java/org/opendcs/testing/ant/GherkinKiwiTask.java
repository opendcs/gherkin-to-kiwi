package org.opendcs.testing.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class GherkinKiwiTask extends Task {

    private String projectName = null;
    private String url = null;
    private String username = null;
    private String password = null;

    public GherkinKiwiTask() {
        
    }
    
    public void setProject(String projectName) {
        this.projectName = projectName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void execute() throws BuildException {
        if (this.projectName == null) {
            this.projectName = getProject().getName();
        }
        System.out.println("Hello, creating test cases for " + projectName + " at url " + url);
    }
}
