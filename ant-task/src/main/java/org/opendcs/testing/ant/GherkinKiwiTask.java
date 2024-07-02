package org.opendcs.testing.ant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.opendcs.testing.PlanDefinition;
import org.opendcs.testing.gherkin.TestCaseGenerator;
import org.opendcs.testing.gherkin.TestPlanGenerator;
import org.opendcs.testing.kiwi.Product;
import org.opendcs.testing.kiwi.TestCase;
import org.opendcs.testing.kiwi.TestPlan;
import org.opendcs.testing.kiwi.TestUtils;
import org.opendcs.testing.kiwi.Version;
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
    private String version = null;
    private Path planDefinitionsFile = null;
    private Map<String,PlanDefinition> planDefinitions = null;

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

    public void setVersion(String version) {
        this.version = version;
    }

    public void setPlanDefinitions(String planDefinitionsFile) {
        this.proj.log(this, "Plan Def File = " + planDefinitionsFile, Project.MSG_INFO);
        this.planDefinitionsFile = Paths.get(planDefinitionsFile);
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

        if (planDefinitionsFile == null) {
            throw new BuildException("A 'planDefinitions' attribute must be set to a file"
                                    +" containing a json list of test plan defitions.", getLocation());
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
    public void execute() throws BuildException {
        validate_settings();
        final TestCaseGenerator tcg = new TestCaseGenerator(projectName);
        System.out.println("Creating test cases for '" + projectName + "' at url '" + url + "'");
        ThrowingFunction<Resource,Stream<TestCase>> mapCases = r -> {
            return tcg.generateCases(new File(r.getName()).toPath())
                      .peek(fr -> fr.handleError(err -> {
                            if (err.getMessage() != null && err.getCause() != null) {
                                throw new BuildException(err.getMessage(), err.getCause());
                            } else if (err.getCause() != null) {
                                throw new BuildException("Unable to process feature file", err.getCause());
                            } else {
                                throw new BuildException(err.getMessage());
                            }
                      }))
                      .map(fr -> fr.getSuccess());
        };
        List<TestCase> cases = files.stream()
             .map(ThrowingFunction.wrap(mapCases))
             .peek(r -> r.handleError(ex -> {
                    throw new BuildException("Unable to process feature file.", ex);
                })
                )
             // any failure will abruptly end the processing.
             .flatMap(r -> r.getSuccess())
             .peek(System.out::println)
             .collect(Collectors.toList());
        cases.forEach(tc -> proj.log(this, tc.toString(), Project.MSG_INFO));

        
        try {
            final KiwiClient client = new KiwiClient(url, username, password);

            create_version(client);

            Stream<TestCase> casesWithId = TestUtils.saveTestCases(cases, client)
                                                    .peek(fr -> fr.handleError(ex ->
                                                                    { 
                                                                        throw new BuildException("Unable to process save case.", ex);
                                                                    })
                                                    )
                                                    .map(r -> r.getSuccess())
                                                    .peek(System.out::println);

            Stream<TestPlan> plans = TestPlanGenerator.generateTestPlans(casesWithId, planDefinitions, version);

            Stream<TestPlan> plansWithId = TestUtils.saveTestPlans(plans, client)
                                                    .peek(fr -> fr.handleError(ex ->
                                                          {
                                                            throw new BuildException("Unable to process saving a test plan.", ex);
                                                          }))
                                                    .map(r -> r.getSuccess())
                                                    .peek(System.out::println);
                                           ;
            plansWithId.forEach(tp -> proj.log(this, tp.toString(), Project.MSG_INFO));
        } catch (IOException ex) {
            throw new BuildException("Problem communicating with KiwiTCMS instance", ex, getLocation());
        }

    }

    private void create_version(KiwiClient client) throws IOException {
        Map<String,String> query = new HashMap<>();
        query.put("value", version);
        query.put("product__name", projectName);
        if (!client.version().filter(query).stream().findFirst().isPresent()) {
            Version v = Version.of(version, Product.of(projectName));
            client.version().create(v);
        }
    }
}
