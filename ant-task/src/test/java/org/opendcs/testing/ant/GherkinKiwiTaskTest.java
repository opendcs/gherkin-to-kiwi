package org.opendcs.testing.ant;

import java.io.File;
import java.net.URL;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Task;
import org.junit.jupiter.api.Test;


public class GherkinKiwiTaskTest {
    @Test
    public void test_kiwi_build() throws Exception {
        Project proj = new Project();
        URL url = this.getClass().getClassLoader().getResource("ant-builds/simple-build.xml");
        File buildFile = new File(url.toURI());
        proj.setUserProperty("ant.file", buildFile.getAbsolutePath());
        proj.init();
        proj.setBaseDir(new File("."));
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        proj.addReference("ant.projectHelper", helper);
        proj.setUserProperty("kiwi.url", System.getProperty("kiwi.url"));
        proj.setUserProperty("kiwi.username", System.getProperty("kiwi.user"));
        proj.setUserProperty("kiwi.password", System.getProperty("kiwi.password"));
        helper.parse(proj, buildFile);
        proj.executeTarget(proj.getDefaultTarget());
    }
}
