package org.opendcs.testing.ant;

import java.io.File;
import java.net.URL;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Disabled;

@Disabled
public class GherkinKiwiTaskTest
{    

    @ParameterizedTest
    @ValueSource(strings = {"ant-builds/simple-build.xml","ant-builds/name-override-build.xml"})
    public void test_kiwi_build(String buildFileFile) throws Exception
    {
        Project proj = new Project();
        URL url = this.getClass().getClassLoader().getResource(buildFileFile);
        File buildFile = new File(url.toURI());
        proj.setUserProperty("ant.file", buildFile.getAbsolutePath());
        proj.init();
        proj.setBaseDir(new File("."));
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        proj.addReference("ant.projectHelper", helper);
        proj.setUserProperty("kiwi.url", System.getProperty("kiwi.url", "https://localhost:8443"));
        proj.setUserProperty("kiwi.username", System.getProperty("kiwi.user", "test-upload"));
        proj.setUserProperty("kiwi.password", System.getProperty("kiwi.password", "test-password"));
        helper.parse(proj, buildFile);
        DefaultLogger logger = new DefaultLogger();
        logger.setOutputPrintStream(System.out);
        logger.setErrorPrintStream(System.err);
        logger.setMessageOutputLevel(Project.MSG_DEBUG);
        proj.addBuildListener(logger);
        proj.executeTarget(proj.getDefaultTarget());
    }
}
