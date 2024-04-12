package org.opendcs.testing.gherkin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.opendcs.testing.kiwi.TestCase;
import org.opendcs.testing.util.FailableResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.ParseError;
import io.cucumber.messages.types.Pickle;
import io.cucumber.messages.types.PickleStep;
import io.cucumber.messages.types.PickleStepType;
import io.cucumber.messages.types.SourceReference;

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

    public Stream<FailableResult<TestCase,ProcessingError>> generateCases(Path path)
    {
        final AtomicReference<String> currentFeature = new AtomicReference<>();
        Predicate<FailableResult<TestCase,ParseError>> excludeNull = r -> r != null;
        try {
            return parser.parse(path)
                         .map(e -> createOrSetError(e, currentFeature, product))
                         .filter(excludeNull)
                         .map(fr -> {
                            if (fr.isSuccess()) {
                                return FailableResult.success(fr.getSuccess());
                            } else {
                                return FailableResult.failure(new ProcessingError(fr.getFailure().getMessage()));
                            }
                         });
        } catch (IOException ex) {
            return Stream.of(FailableResult.failure(new ProcessingError("Unable to process "+ path.toUri().toString(), ex)));
        }
    }

    public Stream<FailableResult<TestCase,ProcessingError>> generateCases(List<Path> paths) throws IOException {
        return paths.stream()
             .flatMap(p -> generateCases(p));
    }

    private static FailableResult<TestCase,ParseError> createOrSetError(Envelope e, AtomicReference<String> currentFeature, String product) {
        if (e.getParseError().isPresent()) {
            return FailableResult.failure(e.getParseError().get());
        }
        e.getGherkinDocument().ifPresent(gd -> {
            if (log.isTraceEnabled()) {
                log.trace(gd.getUri().orElse("no uri"));
            }
            gd.getFeature().ifPresent(f -> {
                currentFeature.set(f.getName());
            });
        });

        Optional<Pickle> pickle = e.getPickle();
        if (pickle.isPresent()) {
            return FailableResult.success(fromPickle(pickle.get(),currentFeature, product));
        }
        return null;
    }


    private static TestCase fromPickle(Pickle p, AtomicReference<String> currentFeature, String product) {
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
        return testBuilder.build();
    }


    public static void main (String args[])
    {
        Path path = Paths.get("src/test/resources/feature-files/PlatformListSorting.feature");
        System.out.println(path.toFile().exists());
        TestCaseGenerator generator = new TestCaseGenerator("OpenDCS");
        final String url = args[0];
        final String user = args[1];
        final String password = args[2];
        //KiwiClient client = new KiwiClient(url, user, password);

        generator.generateCases(path)
                 .peek(ftc -> ftc.handleError(ex -> {
                    System.out.println(ex.getMessage());
                  }))
                 .filter(ftc -> ftc.isSuccess())
                 .map(ftc -> ftc.getSuccess())
                .forEach(System.out::println);
        //TestUtils.saveTestCases(cases, client);
    }
}
