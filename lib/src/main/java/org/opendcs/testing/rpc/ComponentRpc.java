package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendcs.testing.kiwi.Component;
import org.opendcs.testing.kiwi.Product;

import com.fasterxml.jackson.databind.JsonNode;

public final class ComponentRpc {

    private final KiwiClient client;

    public ComponentRpc(KiwiClient client)
    {
        this.client = client;
    }

    public Component create(Component component) throws IOException {
        return client.create("Component.create",
                             c -> Arrays.asList(componentElementsToMap(c,client)),
                             null,
                             n -> {
                                return jsonToComponent(n, client);
                             },
                             component);
    }

    

    public Component update(long componentId, Component newComponent) {
        return null;
    }

    public List<Component> filter(Map<String,String> query) throws IOException {
        return client.filter("Component.filter", n -> jsonToComponent(n, client), query);
    }

    private static Component jsonToComponent(JsonNode node, KiwiClient client) throws IOException {
        Map<String,String> productQuery = new HashMap<>();
        productQuery.put("id", node.get("product_id").asText());
        Product p = client.product()
                          .filter(productQuery)
                          .stream()
                          .findFirst()
                          .orElseThrow(() -> new IOException("No Product for given ID"));
        return Component.of(node.get("id").asLong(), p, node.get("name").asText());
    }

    private static Map<String, Object> componentElementsToMap(Component component, KiwiClient client) throws IOException {
        Map<String,Object> map = new HashMap<>();
        map.put("name", component.name);
        if (component.product.id > 0) {
            map.put("product", component.product.id);
        } else {
            Map<String,String> productQuery = new HashMap<>();
            productQuery.put("name",component.product.name);
            Product p = client.product()
                              .filter(productQuery)
                              .stream()
                              .findFirst()
                              .orElseThrow(() -> new IOException("Can't find product with given name."));
            map.put("product",p.id);
        }
        return map;
    }
}
