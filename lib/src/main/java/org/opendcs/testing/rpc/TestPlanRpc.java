package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendcs.testing.kiwi.Category;
import org.opendcs.testing.kiwi.TestCase;
import org.opendcs.testing.kiwi.TestPlan;

import com.fasterxml.jackson.databind.JsonNode;

public class TestPlanRpc
{
    private final KiwiClient client;

    public TestPlanRpc(KiwiClient client)
    {
        this.client = client;
    }

/*{
    'product': 61,
    'product_version': 93,
    'name': 'Testplan foobar',
    'type': 1,
    'parent': 150,
    'text':'Testing TCMS',
}*/

    public List<TestPlan> filter(Map<String,String> query) throws IOException 
    {
        return client.filter("TestPlan.filter", node -> fillTestPlan(node, client), query);
    }


    public TestPlan create(TestPlan plan) throws IOException
    {
        long id = client.create("TestPlan.create", 
                                null, 
                                (node) -> node.get("id").asLong(),
                                testPlanElementsToMap(plan, client));
        Map<String,String> query = new HashMap<>();
        query.put("id", ""+id);
        return filter(query).stream()
            .findFirst()
            .orElseThrow(() -> new IOException("TestPlan could not be read back."));
    }

    public TestPlan update(long id, TestPlan plan) throws IOException
    {
        TestPlan tpOut = client.update("TestPlan.update",
                                    null,
                                    n -> fillTestPlan(n, client),
                                    id,
                                    testPlanElementsToMap(plan, client));
        // TODO: remove/refill test cases
        return tpOut;
    }

    public void add_test_case(Long planId, Long caseId) throws IOException
    {
        client.create("TestPlan.add_case",
                     null,
                     n -> null,
                     planId, caseId);
    }

    public void remove_test_case(Long planId, Long caseId) throws IOException
    {
        client.remove("TestPlan.remove_case", planId, caseId);
    }

    public static Map<String,Object> testPlanElementsToMap(TestPlan plan, KiwiClient client) throws IOException
    {
        Map<String,Object> planMap = new HashMap<>();
        planMap.put("name", plan.getName());
        planMap.put("type",1); //TODO: type Rpc
        long productId = plan.getProduct().id;
        if (productId == -1L) {
            Map<String,String> query = new HashMap<>();
            query.put("name",plan.getProduct().name);
            productId = client.product()
                              .filter(query)
                              .stream()
                              .map(e -> e.id)
                              .findFirst()
                              .orElseThrow(() -> new IOException("Unable to find product with name '" + plan.getProduct().name + "'"));
        }
        
        planMap.put("product", productId);
        planMap.put("product_version", 1);
        return planMap;
    }

    public static TestPlan fillTestPlan(JsonNode node, KiwiClient client ) throws IOException
    {
        TestPlan.Builder tpb = new TestPlan.Builder();
        tpb.withId(node.get("id").asLong())
           .withName(node.get("name").asText())
           .withType(node.get("type").asText())
           .withProduct(client.product().byId(node.get("product").asLong()))
            ;
        return tpb.build();
    }
}
