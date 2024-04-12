package org.opendcs.testing.ant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.opendcs.testing.gherkin.TestCaseGenerator;
import org.opendcs.testing.kiwi.TestCase;
import org.opendcs.testing.kiwi.TestUtils;
import org.opendcs.testing.rpc.KiwiClient;
import org.opendcs.testing.util.FailableResult;
import org.opendcs.testing.util.ThrowingFunction;

public class GherkinKiwiTask extends Task {

    private String projectName = null;
    private String url = null;
    private String username = null;
    private String password = null;
    private FileSet files = null;
    private Project proj = null;

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

    public void addConfiguredFileSet(FileSet files) {
        this.files = files;
    }

    @Override
    public void init() throws BuildException {
        this.proj = getProject();
        
    }

    public void validate_settings() throws BuildException {
        if (this.projectName == null) {
            this.projectName = proj.getName();
        }

        if (url == null) {
            throw new BuildException("'url' attribute must be set", getLocation());
        }

        if (username == null) {
            throw new BuildException("'username' attribute must be set", getLocation());
        }

        if (password == null) {
            throw new BuildException("'password' attribute must be set", getLocation());
        }

        if (files == null) { 
            throw new BuildException("A nested FileSet must be provided.", getLocation());
        }
    }

    @Override
    public void execute() throws BuildException {
        validate_settings();
        final TestCaseGenerator tcg = new TestCaseGenerator(projectName);
        System.out.println("Creating test cases for '" + projectName + "' at url '" + url + "'");
        ThrowingFunction<Resource,Stream<TestCase>> mapCases = r -> {
            return tcg.generateCases(new File(r.getName()).toPath())
                      .peek(fr -> fr.handleError(ex -> {
                        throw new BuildException("Unable to process feature file", ex);
                      }))
                      .map(fr -> fr.getSuccess());
        };
        List<TestCase> cases = files.stream()
             .map(ThrowingFunction.wrap(mapCases))
             .peek(r -> r.handleError(ex -> {
                    throw new BuildException("Unable to process feature file", ex);
                })
                )
             // any failure will abruptly end the processing.
             .flatMap(r -> r.getSuccess())
             .collect(Collectors.toList());
        cases.forEach(tc -> proj.log(this,tc.toString(), Project.MSG_INFO));

        try {
            final KiwiClient client = new KiwiClient(url, username, password);
            TestUtils.saveTestCases(cases, client);
        } catch (IOException ex) {
            throw new BuildException("Problem communicating with KiwiTCMS instance", ex, getLocation());
        }

    }
}
