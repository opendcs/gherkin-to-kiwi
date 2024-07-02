package org.opendcs.testing.gherkin;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.opendcs.testing.PlanDefinition;
import org.opendcs.testing.kiwi.TestCase;
import org.opendcs.testing.kiwi.TestPlan;
import org.opendcs.testing.kiwi.tags.KiwiTag;
import org.opendcs.testing.kiwi.tags.PlanTag;

public class TestPlanGenerator {
    
    public static Stream<TestPlan> generateTestPlans(List<TestCase> cases, Map<String,PlanDefinition> planDefinitions, String version) {
        return generateTestPlans(cases.stream(), planDefinitions, version);
    }

    /**
     * Generates test plans from the given cases. Uses Tags that were put on each test in the feature file
     * @param cases
     * @return TestPlan objects ready to be saved to kiwi
     */
    public static Stream<TestPlan> generateTestPlans(Stream<TestCase> cases, Map<String,PlanDefinition> planDefinitions, String version) {
        final Map<String,TestPlan.Builder> plans = new HashMap<>();
        
        cases.forEach(tc ->
        {
            Stream<PlanTag> kiwiTags = processTags(tc.getTags()).filter(t -> t instanceof PlanTag).map(t -> (PlanTag)t);
            kiwiTags.forEach(pt -> {
                TestPlan.Builder builder = plans.computeIfAbsent(pt.planName, key -> {
                    return new TestPlan.Builder()
                                       .withProduct(tc.getProduct());
                });
                builder.withTest(tc);
                PlanDefinition pd = planDefinitions.get(pt.planName);
                builder.withType(pd.type);
                builder.withName(pd.name);
                builder.withVersion(version);
            });
        });
        return plans.values().stream().map(TestPlan.Builder::build);
    }


    public static Stream<KiwiTag> processTags(List<String> tags) {
        return tags.stream()
                   .map(t -> KiwiTag.of(t))
                   .filter(kt -> kt != null);
    }
}
