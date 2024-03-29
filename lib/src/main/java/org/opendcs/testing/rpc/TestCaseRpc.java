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

public class TestCaseRpc
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
        JSONRPC2Request rpcReq = new JSONRPC2Request("TestCase.create",UUID.randomUUID().toString());
        Map<String,Object> params = testCaseElementsToMap(tc);
        rpcReq.setPositionalParams(Arrays.asList(params));
        JSONRPC2Response response = client.rpcRequest(rpcReq);
        String jsonString = response.getResult().toString();
        JsonNode node = jsonMapper.readTree(jsonString);
        return node.get("id").asLong();
    }

    public List<TestCase> filter(Map<String,String> query) throws IOException
    {
        List<TestCase> testCases = new ArrayList<>();
        JSONRPC2Request rpcReq = new JSONRPC2Request("TestCase.filter", UUID.randomUUID().toString());
        rpcReq.setPositionalParams(Arrays.asList(query));
        System.out.println(rpcReq.toJSONString());
        JSONRPC2Response response = client.rpcRequest(rpcReq);
        String jsonString = response.getResult().toString();
        JsonNode node = jsonMapper.readTree(jsonString);
        System.out.println(node.toPrettyString());
        node.forEach(e ->
        {
            TestCase tc = fillTestCase(e);
            testCases.add(tc);
        });
        return testCases;
    }

    public TestCase update(long id, TestCase tc) throws IOException
    {
        if (id <= 0)
        {
            throw new IOException("Cannot update TestCase without ID.");
        }
        JSONRPC2Request rpcReq = new JSONRPC2Request("TestCase.update", UUID.randomUUID().toString());
        Map<String,Object> params = testCaseElementsToMap(tc);
        rpcReq.setPositionalParams(Arrays.asList(id, params ));
        JSONRPC2Response response = client.rpcRequest(rpcReq);
        JsonNode node = jsonMapper.readTree(response.getResult().toString());
        return fillTestCase(node);
        
    }

    private Map<String,Object> testCaseElementsToMap(TestCase tc)
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

    private TestCase fillTestCase(JsonNode node)
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
