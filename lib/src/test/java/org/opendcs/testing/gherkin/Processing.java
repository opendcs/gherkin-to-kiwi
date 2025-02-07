package org.opendcs.testing.gherkin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.opendcs.testing.kiwi.TestCase;

/**
 * NOTE: tests will be created. Placeholder for ci/cd setup at the moment.
 */
public class Processing {
    @Test
    public void test_feature_processing() throws Exception {
        Path path = Paths.get("src/test/resources/feature-files/PlatformListSorting.feature");
        System.out.println(path.toFile().exists());
        TestCaseGenerator generator = new TestCaseGenerator("OpenDCS");
        

        List<TestCase> cases = generator.generateCases(path)
                .peek(ftc -> ftc.handleError(ex ->
                {
                    System.out.println(ex.getMessage());
                }))
                .filter(ftc -> ftc.isSuccess())
                .map(ftc -> ftc.getSuccess())
                .map(TestCase.Builder::build)
                .peek(System.out::println)
                .peek(tc -> System.out.println(tc.getSteps()))
                .collect(Collectors.toList());
        assertEquals(10, cases.size(), "Not all expected test cases were created.");
    }
}
