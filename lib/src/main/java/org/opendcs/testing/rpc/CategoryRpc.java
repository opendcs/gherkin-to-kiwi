package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendcs.testing.kiwi.Category;
import org.opendcs.testing.kiwi.Product;

import com.fasterxml.jackson.databind.JsonNode;

public class CategoryRpc {
    private final KiwiClient client;

    public CategoryRpc(KiwiClient client)
    {
        this.client = client;
    }


    public Category create(Category category) throws IOException {
        return client.create("Category.create",
                             c -> Arrays.asList(toMap(c,client)),
                             null,
                             n -> fromJson(n,client),
                             category);
    }

    public List<Category> filter(Map<String,String> query) throws IOException {
        return client.filter("Category.filter", n -> fromJson(n, client), query);
    }

    public Category byNameAndProduct(String name, Product p) throws IOException {
        Map<String,String> query = new HashMap<>();
        query.put("name",name);
        query.put("product__name", p.name);
        return filter(query).stream()
                            .findFirst()
                            .orElseThrow(
                                () -> new IOException("No category for name " + name + " and product " + p.name));
    }

    static Map<String,Object> toMap(Category category, KiwiClient client) throws IOException {
        Map<String,Object> map = new HashMap<>();
        map.put("name", category.name);
        long id = category.product.id;
        if (id == -1) {
            id = client.product().byId(id).id;
        }
        map.put("product", id);
        return map;
    }

    static Category fromJson(JsonNode node, KiwiClient client) throws IOException {
        return Category.of(node.get("id").asLong(),
                           client.product().byId(node.get("product").asLong()),
                           node.get("name").asText());
    }
}
