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

public final class TestCaseRpc {
    private final KiwiClient client;

    public TestCaseRpc(KiwiClient client) {
        this.client = client;
    }

    /**
     * Save test case to database
     * @param tc
     * @return
     */
    public TestCase create(TestCase tc) throws IOException {
        long id = client.create("TestCase.create",
                             null,
                             (node) -> node.get("id").asLong(),
                             testCaseElementsToMap(tc, client));
        Map<String,String> query = new HashMap<>();
        query.put("id", ""+id);
        return filter(query).stream()
                            .findFirst()
                            .orElseThrow(() -> new IOException("TestCase could not be read back."));
    }

    public List<TestCase> filter(Map<String,String> query) throws IOException
    {
        return client.filter("TestCase.filter", node -> fillTestCase(node, client), query);
    }

    public TestCase update(long id, TestCase tc) throws IOException
    {
        TestCase tcOut = client.update("TestCase.update",
                                    null,
                                    n -> fillTestCase(n, client),
                                    id,
                                    testCaseElementsToMap(tc, client));
        for (Component c: tcOut.getComponents()) {
            remove_component(id, c);
        }
        for (Component c: tc.getComponents()) {
            add_component(id, c);
        }
        return tcOut;
    }

    public List<TestCase.TestCaseProperty> properties(Map<String,String> query) throws IOException {
        return client.filter("TestCase.properties",
                             e -> new TestCase.TestCaseProperty(
                                    e.get("id").asLong(),
                                    e.get("case").asLong(),
                                    e.get("name").asText(),
                                    e.get("value").asText()),
                             query);
    }

    public TestCase.TestCaseProperty add_property(long id, String name, String value) throws IOException {
        return client.create("TestCase.add_property",
                             null,
                             node -> new TestCase.TestCaseProperty(node.get("id").asLong(),
                             node.get("case").asLong(),
                             node.get("name").asText(),
                             node.get("value").asText()),
                             id, name, value
        );
    }

    public void remove_property(Map<String,String> query) throws IOException {
        client.remove("TestCase.remove_property", query);
    }

    public Component add_component(long caseId, Component component) throws IOException {
        return add_component(caseId, component, false);
    }

    public void remove_component(long caseId, Component component) throws IOException {
        if (component.id == -1) {
            throw new IOException("Component provided does not have an ID listed.");
        }
        Map<String,String> query = new HashMap<>();
        query.put("id", "" + caseId);
        query.put("component_id", "" + component.id);
        client.remove("TestCase.remove_component", query);
    }

    public List<Component> components(long caseId) throws IOException {
        Map<String,String> query = new HashMap<>();
        query.put("id", "" + caseId);
        return client.filter("TestCase.components", n -> ComponentRpc.jsonToComponent(n, client), query);
    }

    public Component add_component(long caseId, Component component, boolean create) throws IOException {
        Map<String,String> query = new HashMap<>();
        query.put("name", component.name);
        query.put("product__name", component.product.name);
        List<Component> components = client.component().filter(query);
        if (components.isEmpty() && create) {
            System.out.println("Creating component: " + component);
            client.component().create(component);
        }
        return client.create("TestCase.add_component",
                             null,
                             n -> ComponentRpc.jsonToComponent(n, client), caseId, component.name);
    }

    private static Map<String,Object> testCaseElementsToMap(TestCase tc, KiwiClient client) throws IOException {
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

    private static TestCase fillTestCase(JsonNode node, KiwiClient client) throws IOException {
        Map<String,String> query = new HashMap<>();
        query.put("id", node.get("category").asText());
        Category category = client.category().filter(query).stream().findFirst().get();
        TestCase.Builder builder = new TestCase.Builder(category.product.name)
            .withSteps(node.get("text").asText())
            .withPriority(node.get("priority__value").asText())
            .withSummary(node.get("summary").asText())
            .withCategory(category.name, category.product.name)
            .withStatus(node.get("case_status__name").asText())
            .withId(node.get("id").asLong())
            .withNotes(node.get("notes").asText());
        /* NOTE there doesn't appear to be possible in this structure
        List<Component> components = client.testcase().components(builder.getId());
        builder.withComponents(components);
         */
        return builder.build();
    }
}
