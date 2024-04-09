package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.opendcs.testing.kiwi.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public final class ComponentRpc {

    private final KiwiClient client;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public ComponentRpc(KiwiClient client)
    {
        this.client = client;
    }

    public Component create(Component component) throws IOException {
        Map<String,Object> params = componentElementsToMap(component);
        JSONRPC2Request rpcReq = client.createRequest("Component.create",Arrays.asList(params));
        JSONRPC2Response response = client.rpcRequest(rpcReq);
        String jsonString = response.getResult().toString();
        JsonNode node = jsonMapper.readTree(jsonString);
        return jsonToComponent(node);
    }

    

    public Component update(long componentId, Component newComponent) {
        return null;
    }

    public List<Component> filter(Map<String,String> query) {
        List<Component> components = new ArrayList<>();

        return components;
    }

    private Component jsonToComponent(JsonNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'jsonToComponent'");
    }

    private Map<String, Object> componentElementsToMap(Component component) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'componentElementsToMap'");
    }
}
