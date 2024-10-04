package org.opendcs.testing.gradle;

import org.apache.commons.io.FileUtils;

import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.CleanupMode;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Paths

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

@Disabled
class PluginToKiwiTest 
{
    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    File testProjectDir;
    private File settingsFile;
    private File buildFile;
    
    @BeforeEach
    void setup() throws Exception
    {
        settingsFile = new File(testProjectDir, "settings.gradle");
        buildFile = new File(testProjectDir, "build.gradle");
        buildFile << """
            plugins {
                id 'org.opendcs.testing.tcms.gherkin-kiwi'
            }
        """
        def resources = new File("src/test/resources")
        FileUtils.copyDirectory(resources, new File(testProjectDir, "src/test/resources"))
        
    }

    @Test
    void test_push() throws Exception
    {
        settingsFile << """
            rootProject.name = 'kiwi-push-test'
        """
        buildFile << """
            version = "0.1"
            kiwi {
                product = "test"
                
                plans {
                    Plan1 {
                        planName = "A Simple Plan"
                        type = "Acceptance"
                    }

                    Plan2 {
                        planName = "A plan with space in the ID"
                        type = "Acceptance"
                    }

                    Plan3 {
                        planName = "A third plan"
                        type = "Acceptance"
                    }
                }

                outputs {
                    hec {
                        product = "OpenDCS"
                        type = "kiwi"
                        url = project.getProperty("kiwi.url")
                        version = project.version
                        username = project.getProperty("kiwi.user")
                        password = project.getProperty("kiwi.password")
                    }
                }
            }
        """

        def result = GradleRunner.create()
                                 .withProjectDir(testProjectDir)
                                 .withArguments("outputTestCases",
                                                "-Pkiwi.user=" + System.getProperty("kiwi.user"),
                                                "-Pkiwi.password=" + System.getProperty("kiwi.password"),
                                                "-Pkiwi.url=" + System.getProperty("kiwi.url"),
                                                "--stacktrace")
                                 .withPluginClasspath()
                                 .build()

        
    }
}