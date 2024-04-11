package org.opendcs.testing.ant;

import java.io.File;
import java.net.URL;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import org.junit.jupiter.api.Test;

public class GherkinKiwiTaskTest {
    @Test
    public void test_kiwi_build() throws Exception {
        Project proj = new Project();
        URL url = this.getClass().getClassLoader().getResource("ant-builds/simple-build.xml");
        File buildFile = new File(url.toURI());
        System.out.println(buildFile.getAbsolutePath());
        proj.setUserProperty("ant.file", buildFile.getAbsolutePath());
        proj.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        proj.addReference("ant.projectHelper", helper);
        helper.parse(proj, buildFile);
        proj.executeTarget(proj.getDefaultTarget());
    }
}
