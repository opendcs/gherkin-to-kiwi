package org.opendcs.testing.gherkin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opendcs.testing.kiwi.TestCase;
import org.opendcs.testing.rpc.KiwiClient;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;

import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.PickleStepType;
import io.cucumber.messages.types.PickleTag;

public class TestCaseGenerator 
{

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
                    System.out.println(gd.getUri().orElse("no uri"));
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
                    kiwiCases.add(testBuilder.build());
                });
            });
            return kiwiCases;
    }


    public static void main (String args[])
    {
        
        Path path = Paths.get("src/test/resources/feature-files/PlatformListSorting.feature");
        TestCaseGenerator generator = new TestCaseGenerator("Test");
        try
        {
            KiwiClient client = new KiwiClient("http://localhost", "test", "testpass");

            generator.generateCases(path)
                     .forEach(tc ->
                     {  
                        System.out.println(tc.getProduct().name);
                        System.out.println("\tComponents: "+tc.getComponents().stream().map(c->c.name).collect(Collectors.joining(",")));
                        System.out.println("\tSummary:  "+tc.getSummary());
                        System.out.println("\tSteps:"+System.lineSeparator()+tc.getSteps());
                        try
                        {
                            client.writeTestCase(tc);
                        }
                        catch (IOException ex)
                        {
                            System.out.println("Unable to push case to kiwi.");
                            ex.printStackTrace();
                        }
                     });

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
