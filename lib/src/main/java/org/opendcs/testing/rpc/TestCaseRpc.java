package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.opendcs.testing.kiwi.TestCase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public final class TestCaseRpc
{
    private final KiwiClient client;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public TestCaseRpc(KiwiClient client)
    {
        this.client = client;
    }

    /**
     * Save test case to database
     * @param tc
     * @return
     */
    public long create(TestCase tc) throws IOException
    {
        return client.create("TestCase.create", 
                             t -> Arrays.asList(testCaseElementsToMap(t)), 
                             null,
                             (node) -> node.get("id").asLong(),
                             tc);
    }

    public List<TestCase> filter(Map<String,String> query) throws IOException
    {
        return client.filter("TestCase.filter", node -> fillTestCase(node), query);
    }

    public TestCase update(long id, TestCase tc) throws IOException
    {
        return client.update("TestCase.update",
                             t -> Arrays.asList(testCaseElementsToMap(t)),
                             null,
                             TestCaseRpc::fillTestCase,
                             id, 
                             tc);
    }

    public List<TestCase.TestCaseProperty> properties(Map<String,String> query) throws IOException
    {
        List<TestCase.TestCaseProperty> properties = new ArrayList<>();
        JSONRPC2Request rpcReq = client.createRequest("TestCase.properties",Arrays.asList(query));
        JSONRPC2Response response = client.rpcRequest(rpcReq);
        JsonNode node = jsonMapper.readTree(response.getResult().toString());
        node.forEach(e ->
        {
            properties.add(
                new TestCase.TestCaseProperty(
                    e.get("id").asLong(),
                    e.get("case").asLong(),
                    e.get("name").asText(),
                    e.get("value").asText())
            );
        });
        return properties;
    }

    public TestCase.TestCaseProperty add_property(long id, String name, String value) throws IOException
    {
        JSONRPC2Request rpcReq = client.createRequest("TestCase.add_property",Arrays.asList(id, name, value));
        JSONRPC2Response response = client.rpcRequest(rpcReq);
        JsonNode node = jsonMapper.readTree(response.getResult().toString());
        return new TestCase.TestCaseProperty(node.get("id").asLong(),
                                    node.get("case").asLong(),
                                    node.get("name").asText(),
                                    node.get("value").asText());
    }

    public void remove_property(Map<String,String> query) throws IOException
    {
        JSONRPC2Request rpcReq = client.createRequest("TestCase.remove_property",Arrays.asList(query));
        client.rpcRequest(rpcReq);
    }

    private static Map<String,Object> testCaseElementsToMap(TestCase tc)
    {
        Map<String,Object> params = new HashMap<>();
        params.put("summary", tc.getSummary());
        params.put("text", tc.getSteps());
        params.put("case_status", 2);//tc.getStatus());
        params.put("category", 1);//tc.getCategory());
        params.put("priority", 3); //tc.getPriority();
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
