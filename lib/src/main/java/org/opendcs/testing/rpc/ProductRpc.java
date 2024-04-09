package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendcs.testing.kiwi.Product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

public final class ProductRpc {
    private final KiwiClient client;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public ProductRpc(KiwiClient client)
    {
        this.client = client;
    }
 
    public Product create(Product product) throws IOException {
        Map<String,Object> params = productElementsToMap(product);
        JSONRPC2Request rpcReq = client.createRequest("Product.create",Arrays.asList(params));
        JSONRPC2Response response = client.rpcRequest(rpcReq);
        String jsonString = response.getResult().toString();
        JsonNode node = jsonMapper.readTree(jsonString);
        return jsonToProduct(node);
    }

    public List<Product> filter(Map<String, String> query) throws IOException {
        final List<Product> products = new ArrayList<>();
        JSONRPC2Request rpcReq = client.createRequest("product.filter", Arrays.asList(query));
        JSONRPC2Response response = client.rpcRequest(rpcReq);
        String jsonString = response.getResult().toString();
        JsonNode node = jsonMapper.readTree(jsonString);
        node.forEach(e -> {
            products.add(jsonToProduct(e));
        });
        return products;
    }

    private Map<String, Object> productElementsToMap(Product product) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", product.name);
        return map;
    }

    private Product jsonToProduct(JsonNode node) {
        return new Product(node.get("id").asLong(), node.get("name").asText());
    }
}
