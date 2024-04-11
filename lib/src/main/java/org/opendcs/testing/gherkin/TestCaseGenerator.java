package org.opendcs.testing.gherkin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.opendcs.testing.kiwi.Component;
import org.opendcs.testing.kiwi.TestCase;
import org.opendcs.testing.kiwi.TestUtils;
import org.opendcs.testing.rpc.KiwiClient;
import org.opendcs.testing.rpc.TestCaseRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cucumber.gherkin.GherkinParser;

import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.PickleStepType;

public class TestCaseGenerator 
{
    private final static Logger log = LoggerFactory.getLogger(TestCaseGenerator.class);
    GherkinParser parser;
    final String product;

    public TestCaseGenerator(String product)
    {
        this.product = product;
        parser = GherkinParser.builder()
                              .includeGherkinDocument(true)
                              .includePickles(true)
                              .includeSource(true)
                              .build();
    }

    public List<TestCase> generateCases(Path path) throws IOException
    {
        List<TestCase> kiwiCases = new ArrayList<>();
        final AtomicReference<String> currentFeature = new AtomicReference<>();

        parser.parse(path)
              .forEach(e ->
            {
                e.getGherkinDocument().ifPresent(gd ->
                {
                    if (log.isTraceEnabled()) {
                        log.trace(gd.getUri().orElse("no uri"));
                    }
                    gd.getFeature().ifPresent(f ->
                    {
                        currentFeature.set(f.getName());
                    });
                });
                Optional<Pickle> pickle = e.getPickle();
                pickle.ifPresent(p ->
                {
                    final TestCase.Builder testBuilder = new TestCase.Builder(product);
                    testBuilder.withSummary(p.getName());
                    testBuilder.withComponent(currentFeature.get());
                    testBuilder.withPriority("P3"); // TODO: allow a config file/tag/etc to establish default
                    p.getTags().forEach(t -> testBuilder.withTag(t.getName()));
                    StringWriter sw = new StringWriter();
                    final PrintWriter pw = new PrintWriter(sw);
                    for (PickleStep s: p.getSteps())
                    {
                        s.getType().ifPresent(type ->
                        {
                            String typeName = type.equals(PickleStepType.ACTION) ? "When" 
                                            : type.equals(PickleStepType.OUTCOME) ? "Then"
                                            : type.equals(PickleStepType.CONTEXT) ? "Given"
                                            : "UNKNOWN";
                            pw.println(String.format("%s: %s", typeName, s.getText()));
                        });
                    }

                    testBuilder.withSteps(sw.toString());
                    // TODO: probably need to be able to tweak the URI to something sensible like the project URL vs the local
                    // FileSystem. Or just Strip to not include the host. 
                    testBuilder.withProperty("marker", String.format("%s-%s", p.getUri(), p.getName()));
                    kiwiCases.add(testBuilder.build());
                });
            });
            return kiwiCases;
    }


    public static void main (String args[])
    {
        Path path = Paths.get("src/test/resources/feature-files/PlatformListSorting.feature");
        TestCaseGenerator generator = new TestCaseGenerator("OpenDCS");
        try
        {
            final String url = args[0];
            final String user = args[1];
            final String password = args[2];
            KiwiClient client = new KiwiClient(url, user, password);

            List<TestCase> cases = generator.generateCases(path);
            TestUtils.saveTestCases(cases, client);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
