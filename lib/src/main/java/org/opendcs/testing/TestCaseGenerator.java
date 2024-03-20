package org.opendcs.testing;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;

public class TestCaseGenerator 
{
 
    public static void main (String args[])
    {
        GherkinParser.Builder builder = GherkinParser.builder();
        GherkinParser parser = builder.build();
        Path path = Paths.get("lib/src/test/resources/feature-files/PlatformListSorting.feature");
        try
        {
            Stream<Envelope> cases = parser.parse(path);
            cases.forEach(e ->
            {
                Optional<Pickle> pickle = e.getPickle();
                pickle.ifPresent(p ->
                {
                    System.out.println("Pickle: " + p.getName());
                    for (PickleStep s: p.getSteps())
                    {
                        System.out.println("\tStep:" + s.getText());
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
