package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendcs.testing.kiwi.Product;

import com.fasterxml.jackson.databind.JsonNode;

public final class ProductRpc {
    private final KiwiClient client;

    public ProductRpc(KiwiClient client)
    {
        this.client = client;
    }
 
    public Product create(Product product) throws IOException {
        return client.create("Product.create",
                             p -> Arrays.asList(productElementsToMap(p)),
                             null,
                             ProductRpc::jsonToProduct,
                             product);
    }

    public List<Product> filter(Map<String, String> query) throws IOException {
        return client.filter("Product.filter", ProductRpc::jsonToProduct, query);
    }

    private static Map<String, Object> productElementsToMap(Product product) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", product.name);
        return map;
    }

    private static Product jsonToProduct(JsonNode node) {
        return new Product(node.get("id").asLong(), node.get("name").asText());
    }
}
