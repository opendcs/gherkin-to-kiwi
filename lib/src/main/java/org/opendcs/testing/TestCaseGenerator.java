package org.opendcs.testing;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.PickleStepType;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.TableRow;

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
            final Map<String,Scenario> scenarios = new HashMap<>();
            final Map<String,TableRow> examples = new HashMap<>();
            final Map<String,Feature> features = new HashMap<>();
            final List<Envelope> cases = parser.parse(path).collect(Collectors.toList());
            cases.forEach(e ->
            {
                e.getGherkinDocument().ifPresent(gd -> {
                    System.out.println("Got Document");
                    
                    gd.getFeature().ifPresent(f ->
                    {
                        
                        System.out.println("Got feature." + f.getName());
                        for(FeatureChild child: f.getChildren())
                        {
                            child.getScenario().ifPresent(s ->
                            {
                                scenarios.put(s.getId(),s);
                                for(Examples ex: s.getExamples())
                                {
                                    ex.getTableBody().forEach(row ->
                                    {
                                        examples.put(row.getId(), row);
                                    });
                                }
                            });
                            
                        }
                    });
                });
            });
            cases.forEach(e ->
            {
                Optional<Pickle> pickle = e.getPickle();
                e.getTestCase().ifPresent(tc -> 
                {
                    System.out.println("***" + tc.getId());
                });
                pickle.ifPresent(p ->
                {
                    final List<String> astNodes = p.getAstNodeIds();
                    
                    String variants = "";
                    if (astNodes.size() > 1)
                    {
                        TableRow row = examples.get(astNodes.get(1));
                        if (row != null)
                        {
                            variants = String.format("[%s]",row.getCells()
                                                                         .stream()
                                                                         .map(cell -> cell.getValue())
                                                                         .collect(Collectors.joining(",")
                                                                         ));
                        }
                    }
                    System.out.println(String.format("TestCase: %s %s", p.getName(), variants));
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
