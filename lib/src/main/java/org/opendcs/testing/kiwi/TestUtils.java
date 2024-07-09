package org.opendcs.testing.kiwi;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.opendcs.testing.PlanDefinition;
import org.opendcs.testing.gherkin.ProcessingError;
import org.opendcs.testing.gherkin.TestCaseGenerator;
import org.opendcs.testing.gherkin.TestPlanGenerator;
import org.opendcs.testing.rpc.KiwiClient;
import org.opendcs.testing.rpc.TestCaseRpc;
import org.opendcs.testing.rpc.TestPlanRpc;
import org.opendcs.testing.util.FailableResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tools to help with generating cases, plans, etc
 */
public class TestUtils
{
    private final static Logger log = LoggerFactory.getLogger(TestUtils.class);

    private TestUtils()
    {
    }

    /**
     * Given a List of test cases save them to the Kiwi client.
     *
     * @param cases Set of TestCases to save.
     * @param client KiwiClient instance to use.
     * @return Processed test cases that now include their ID field for processing plans.
     *         NOTE: Stream will <b>NOT</b> have been processed.
     */
    public static Stream<FailableResult<TestCase, ProcessingError>> saveTestCases(Stream<TestCase> cases,
            KiwiClient client)
    {
        return cases.map(tc -> saveCase(tc, client));
    }

    /**
     * Save a specific test case returning a result.
     * @param tc Test case to save.
     * @param client KiwiClient instance to use
     * @return Stream of (TestCase, ProcessingError) for further operations.
     */
    private static FailableResult<TestCase, ProcessingError> saveCase(TestCase tc, KiwiClient client)
    {
        try
        {
            String marker = tc.getProperty("marker");

            TestCaseRpc rpc = client.testcase();

            Map<String, String> query = new HashMap<>();
            query.put("name", "marker");
            query.put("value", marker);
            long id = rpc.properties(query)
                    .stream()
                    .map(e -> e.caseId)
                    .findFirst()
                    .orElse(-1L);
            if (id == -1)
            {
                TestCase tcOut = client.testcase().create(tc);
                long idOut = tcOut.getId();
                client.testcase().add_property(idOut, "marker", marker);
                for (Component c : tc.getComponents())
                {
                    client.testcase().add_component(idOut, c, true);
                }
                log.debug("Created test, new id =" + idOut);
                id = idOut;
            }
            else
            {
                log.debug("Updating test case. id = " + id);
                client.testcase().update(id, tc);
            }
            return FailableResult.success(tc.newBuilder().withId(id).build());
        }
        catch (IOException ex)
        {
            return FailableResult.failure(new ProcessingError("Unable to save test case.", ex));
        }
    }

    /**
     * Save a test of TestPlans to the instance of Kiwi.
     * @param plans Stream of plans to save.
     * @param client Kiwi Client to use.
     * @return Stream of TestPlans that can be further processed.
     *         NOTE: Test Cases *WILL* have been processed and saved, The TestPlan Stream will not have
     *         executed
     */
    public static Stream<FailableResult<TestPlan, ProcessingError>> saveTestPlans(Stream<TestPlan> plans,
            KiwiClient client)
    {
        return plans.map(tp -> saveTestPlan(tp, client));
    }

    /**
     * Save an individual plan to the database returning a result.
     * @param plan
     * @param client
     * @return
     */
    private static FailableResult<TestPlan, ProcessingError> saveTestPlan(TestPlan plan, KiwiClient client)
    {
        try
        {
            TestPlanRpc rpc = client.testplan();
            Map<String, String> query = new HashMap<>();
            query.put("name", plan.getName());
            long id = rpc.filter(query).stream().map(e -> e.getId()).findFirst().orElse(-1L);
            if (id == -1L)
            {
                TestPlan tpOut = rpc.create(plan);
                long idOut = tpOut.getId();
                id = idOut;
            }
            else
            {
                rpc.update(id, plan);
                rpc.delete_test_cases(id);
            }
            for (TestCase tc : plan.getCases())
            {
                rpc.add_test_case(id, tc.getId());
            }
            return FailableResult.success(plan.newBuilder().withId(id).build());
        }
        catch (IOException ex)
        {
            return FailableResult.failure(new ProcessingError("Unable to save test plan", ex));
        }

    }

    /**
     * Given an appropriate set of data and a valid Kiwi Client actually save the data to Kiwi.
     * The provide version will be created if not already present.
     *
     * @param client
     *            KiwiClient
     * @param productName
     *            Product name to use
     * @param version
     *            version to use
     * @param files
     *            Stream of Paths to read test cases from
     * @param planDefinitions
     *            Definition of test plan definitions that test cases can be assigned to.
     * @param logger
     *            Consumer to which every element will be passed. Intended usage is simple logging by Object::toString
     *            The types can vary depending on where in the stream this gets called.
     * @param onError
     *            Error handler. Your handler should throw a runtime exception to bail on
     *            further processing. Otherwise function carries on with successful results.
     * @throws IOException
     *             Errors during simple Kiwi Operations.
     */
    public static void processAndSaveData(KiwiClient client, String productName, String version,
            Stream<Path> files, Map<String, PlanDefinition> planDefinitions,
            Consumer<Object> logger, Consumer<ProcessingError> onError)
            throws IOException
    {
        final TestCaseGenerator tcg = new TestCaseGenerator(productName);
        createVersion(client, version, productName);

        Function<Path, Stream<TestCase>> mapCases = path ->
        {
            return tcg.generateCases(path)
                    .peek(fr -> fr.handleError(onError))
                    .filter(fr -> fr.isSuccess())
                    .map(fr -> fr.getSuccess());
        };
        Stream<TestCase> cases = files
                .flatMap(mapCases)
                .peek(logger);
        logger.accept("Setting up Case Stream.");
        Stream<TestCase> casesWithId = TestUtils.saveTestCases(cases, client)
                .peek(fr -> fr.handleError(onError))
                .filter(fr -> fr.isSuccess())
                .map(r -> r.getSuccess())
                .peek(logger);

        logger.accept("Setting up Plan Stream.");
        Stream<TestPlan> plans = TestPlanGenerator.generateTestPlans(casesWithId, planDefinitions, version);
        logger.accept("Saving Plans.");
        TestUtils.saveTestPlans(plans, client)
                .peek(fr -> fr.handleError(onError))
                .filter(fr -> fr.isSuccess())
                .map(r -> r.getSuccess())
                .forEach(logger);
    }

    /**
     * Create a new version for this product in the KiwiInstance; if it doesn't already exist
     * @param client client connection to use.
     * @param version version name to use
     * @param productName Product to which this version is attached.
     * @throws IOException If there is an issue with the Kiwi Client HTTP connection
     */
    public static void createVersion(KiwiClient client, String version, String productName) throws IOException
    {
        Map<String, String> query = new HashMap<>();
        query.put("value", version);
        query.put("product__name", productName);
        if (!client.version().filter(query).stream().findFirst().isPresent())
        {
            Version v = Version.of(version, Product.of(productName));
            client.version().create(v);
        }
    }
}
