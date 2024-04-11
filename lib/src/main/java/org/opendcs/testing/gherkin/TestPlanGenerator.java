package org.opendcs.testing.gherkin;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.opendcs.testing.kiwi.TestCase;
import org.opendcs.testing.kiwi.TestPlan;
import org.opendcs.testing.kiwi.tags.KiwiTag;
import org.opendcs.testing.kiwi.tags.PlanTag;

public class TestPlanGenerator {
    
    /**
     * Generates test plans from the given cases. Uses Tags that were put on each test in the feature file
     * @param cases
     * @return TestPlan objects ready to be saved to kiwi
     */
    public static Collection<TestPlan.Builder> generateTestPlans(List<TestCase> cases) {
        final Map<String,TestPlan.Builder> plans = new HashMap<>();
        
        for (TestCase tc: cases) {
            Stream<PlanTag> kiwiTags = processTags(tc.getTags()).filter(t -> t instanceof PlanTag).map(t -> (PlanTag)t);
            kiwiTags.forEach(pt -> {
                TestPlan.Builder builder = plans.computeIfAbsent(pt.planName, key -> {
                    return new TestPlan.Builder()
                                       .withProduct(tc.getProduct());
                });
                builder.withTest(tc);
            });
        }
        return plans.values(); 
    }


    public static Stream<KiwiTag> processTags(List<String> tags) {
        return tags.stream()
                   .map(t -> KiwiTag.of(t))
                   .filter(kt -> kt != null);
    }
}
