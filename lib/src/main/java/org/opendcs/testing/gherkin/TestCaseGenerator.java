package org.opendcs.testing.gherkin;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;

import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.PickleStepType;
import io.cucumber.messages.types.PickleTag;

public class TestCaseGenerator 
{
    public static void main (String args[])
    {
        GherkinParser.Builder builder = GherkinParser.builder();
        builder.includeGherkinDocument(true);
        builder.includePickles(true);
        builder.includeSource(true);
        GherkinParser parser = builder.build();
        Path path = Paths.get("src/test/resources/feature-files/PlatformListSorting.feature");
        try
        {
            final Stream<Envelope> cases = parser.parse(path);
            final AtomicReference<String> currentFeature = new AtomicReference<>();
            cases.forEach(e ->
            {
                e.getGherkinDocument().ifPresent(gd ->
                {
                    System.out.println(gd.getUri().orElse("no uri"));
                    gd.getFeature().ifPresent(f ->
                    {
                        currentFeature.set(f.getName());
                    });
                });
                if (!e.getGherkinDocument().isPresent())
                {
                    System.out.println("No doc");
                }
                else if (!e.getGherkinDocument().get().getFeature().isPresent())
                {
                    System.out.println("No feature");
                }
                Optional<Pickle> pickle = e.getPickle();
                pickle.ifPresent(p ->
                {
                    System.out.println(String.format("%s/TestCase: %s", currentFeature.get(), p.getName()));
                    List<PickleTag> tags = p.getTags();
                    System.out.println("\tTags -> " + tags.stream().map(t -> t.getName()).collect(Collectors.joining(",")));
                    for (PickleStep s: p.getSteps())
                    {
                        s.getType().ifPresent(type ->
                        {
                            String typeName = type.equals(PickleStepType.ACTION) ? "When" 
                                            : type.equals(PickleStepType.OUTCOME) ? "Then"
                                            : type.equals(PickleStepType.CONTEXT) ? "Given"
                                            : "UNKNOWN";
                            System.out.println(String.format("\t%s: %s", typeName, s.getText()));
                        });
                        s.getArgument().ifPresent(arg -> 
                        {
                            System.out.println("Arg: " + arg.toString());
                        });
                        if (!s.getType().isPresent())
                        {
                            System.out.println("\tUnknown Type: " + s.getText());
                        }
                    }
                });
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
