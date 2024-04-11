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
import org.opendcs.util.ThrowingFunction;
import org.opendcs.util.FailableResult;

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
        String tmp = getProject().getUserProperty(url);
        this.url = tmp != null ? tmp : url;
        
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
        if (this.projectName == null) {
            this.projectName = proj.getName();
        }
    }

    @Override
    public void execute() throws BuildException {
        final TestCaseGenerator tcg = new TestCaseGenerator(projectName);
        proj.log("Hello, creating test cases for '" + projectName + "' at url '" + url + "'");
        ThrowingFunction<Resource,Stream<TestCase>> mapCases = r -> {
            List<TestCase> cases = tcg.generateCases(new File(r.getName()).toPath());
            if (cases.isEmpty()) {
                throw new IOException("No test cases in feature file: " + r.getName());
            }
            return cases.stream();
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
        cases.forEach(System.out::println);
    }
}
