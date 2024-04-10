package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendcs.testing.kiwi.Category;
import org.opendcs.testing.kiwi.Component;
import org.opendcs.testing.kiwi.Priority;
import org.opendcs.testing.kiwi.TestCase;

import com.fasterxml.jackson.databind.JsonNode;

public final class TestCaseRpc
{
    private final KiwiClient client;

    public TestCaseRpc(KiwiClient client)
    {
        this.client = client;
    }

    /**
     * Save test case to database
     * @param tc
     * @return
     */
    public TestCase create(TestCase tc) throws IOException
    {
        long id = client.create("TestCase.create",
                             t -> Arrays.asList(testCaseElementsToMap(t, client)),
                             null,
                             (node) -> node.get("id").asLong(),
                             tc);
        Map<String,String> query = new HashMap<>();
        query.put("id", ""+id);
        return filter(query).stream()
                            .findFirst()
                            .orElseThrow(() -> new IOException("TestCase could not be read back."));
    }

    public List<TestCase> filter(Map<String,String> query) throws IOException
    {
        return client.filter("TestCase.filter", node -> fillTestCase(node), query);
    }

    public TestCase update(long id, TestCase tc) throws IOException
    {
        return client.update("TestCase.update",
                             t -> Arrays.asList(testCaseElementsToMap(t, client)),
                             null,
                             TestCaseRpc::fillTestCase,
                             id,
                             tc);
    }

    public List<TestCase.TestCaseProperty> properties(Map<String,String> query) throws IOException
    {
        return client.filter("TestCase.properties",
                             e -> new TestCase.TestCaseProperty(
                                    e.get("id").asLong(),
                                    e.get("case").asLong(),
                                    e.get("name").asText(),
                                    e.get("value").asText()),
                             query);
    }

    public TestCase.TestCaseProperty add_property(long id, String name, String value) throws IOException
    {
        return client.create("TestCase.add_property",
                             l -> new ArrayList<>(l), null,
                             node -> new TestCase.TestCaseProperty(node.get("id").asLong(),
                             node.get("case").asLong(),
                             node.get("name").asText(),
                             node.get("value").asText()),
                             Arrays.asList(id, name, value)
        );
    }

    public void remove_property(Map<String,String> query) throws IOException
    {
        client.remove("TestCase.remove_property", query);
    }

    public Component add_component(long caseId, String componentName) throws IOException {
        return client.create("TestCase.add_component",
                             (s) -> Arrays.asList(caseId,s),
                             null,
                             n -> ComponentRpc.jsonToComponent(n, client), componentName);
    }

    private static Map<String,Object> testCaseElementsToMap(TestCase tc, KiwiClient client) throws IOException
    {
        Map<String,Object> params = new HashMap<>();
        params.put("summary", tc.getSummary());
        params.put("text", tc.getSteps());
        params.put("case_status", 2);//tc.getStatus());
        final Category category = tc.getCategory();
        long categoryId = category.id;
        if (categoryId == -1 ) {
            categoryId = client.category().byNameAndProduct(category.name, category.product).id;
        }
        params.put("category", categoryId);
        final Priority p = tc.getPriority();
        params.put("priority", p.id);
        if (p.id == -1 ) {
            Priority p2 = client.priority().byName(p.name);
            params.put("priority", p2.id);
        }
        
        params.put("notes", tc.getNotes());
        params.put("extra_link", tc.getReferenceLink());
        return params;
    }

    private static TestCase fillTestCase(JsonNode node)
    {
        return new TestCase.Builder("-todo-")
            .withSteps(node.get("text").asText())
            .withPriority(node.get("priority__value").asText())
            .withSummary(node.get("summary").asText())
            .withCategory(node.get("category__name").asText())
            .withStatus(node.get("case_status__name").asText())
            .withId(node.get("id").asLong())
            .withNotes(node.get("notes").asText())
            .build();
    }


}
