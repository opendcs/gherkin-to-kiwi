package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.opendcs.testing.kiwi.TestCase;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TestCaseRpc
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
    public long create(TestCase tc) throws IOException
    {
        JSONRPC2Request rpcReq = new JSONRPC2Request("TestCase.create",1);
        Map<String,Object> params = new HashMap<>();
        params.put("summary", tc.getSummary());
        params.put("text", tc.getSteps());
        params.put("case_status", tc.getStatus());
        params.put("category", tc.getCategory());
        rpcReq.setPositionalParams(Arrays.asList(params));
        JSONRPC2Response response = client.rpcRequest(rpcReq);
        System.out.println(response.getID());
        System.out.println(response.toString());
        return -1;
    }
}
