package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        JSONRPC2Request rpcReq = new JSONRPC2Request("TestCase.create",1);
        Map<String,Object> params = new HashMap<>();
        params.put("summary", tc.getSummary());
        params.put("text", tc.getSteps());
        params.put("case_status", 2);//tc.getStatus());
        params.put("category", 1);//tc.getCategory());
        params.put("priority", 3); //tc.getPriority();
        rpcReq.setPositionalParams(Arrays.asList(params));
        JSONRPC2Response response = client.rpcRequest(rpcReq);
        String jsonString = response.getResult().toString();
        JsonNode node = jsonMapper.readTree(jsonString);
        return node.get("id").asLong();
    }

    public List<TestCase> filter(Map<String,String> query) throws IOException
    {
        List<TestCase> testCases = new ArrayList<>();
        JSONRPC2Request rpcReq = new JSONRPC2Request("TestCase.filter", 2);
        rpcReq.setPositionalParams(Arrays.asList(query));
        JSONRPC2Response response = client.rpcRequest(rpcReq);
        String jsonString = response.getResult().toString();
        JsonNode node = jsonMapper.readTree(jsonString);
        node.forEach(e ->
        {
            TestCase tc = new TestCase.Builder("-todo-")
                            .withSteps(e.get("text").asText())
                            .withPriority(e.get("priority__value").asText())
                            .withSummary(e.get("summary").asText())
                            .withCategory(e.get("category__name").asText())
                            .withStatus(e.get("case_status__name").asText())
                            .withId(e.get("id").asLong())
                            .build();
            testCases.add(tc);
        });
        return testCases;
    }
}
