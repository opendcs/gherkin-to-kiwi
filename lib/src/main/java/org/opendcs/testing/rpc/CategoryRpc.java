package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendcs.testing.kiwi.Category;
import org.opendcs.testing.kiwi.Product;

import com.fasterxml.jackson.databind.JsonNode;

public class CategoryRpc
{
    private final KiwiClient client;

    public CategoryRpc(KiwiClient client)
    {
        this.client = client;
    }

    /**
     * Create a new category.
     * @param category The basic definition of the category to save.
     * @return the Create category with ID and other fields and objects set.
     * @throws IOException any issues with the HTTP connection.
     */
    public Category create(Category category) throws IOException
    {
        return client.create("Category.create",
                null,
                n -> fromJson(n, client),
                toMap(category, client));
    }

    /**
     * Search for categories.
     * See the Kiwi TCMS Python API docs for additional details
     * @param query Search terms to use.
     * @return List of categories matching the query. Or an empty list.
     * @throws IOException any issues with the HTTP connection.
     */
    public List<Category> filter(Map<String, String> query) throws IOException
    {
        return client.filter("Category.filter", n -> fromJson(n, client), query);
    }

    public Category byNameAndProduct(String name, Product p) throws IOException
    {
        Map<String, String> query = new HashMap<>();
        query.put("name", name);
        query.put("product__name", p.name);
        return filter(query).stream()
                .findFirst()
                .orElseThrow(
                        () -> new IOException("No category for name " + name + " and product " + p.name));
    }

    /**
     * Map the Category elements to appropriate fields for the JSON RPC calls
     * @param category
     * @param client
     * @return
     * @throws IOException
     */
    static Map<String, Object> toMap(Category category, KiwiClient client) throws IOException
    {
        Map<String, Object> map = new HashMap<>();
        map.put("name", category.name);
        long id = category.product.id;
        if (id == -1)
        {
            id = client.product().byId(id).id;
        }
        map.put("product", id);
        return map;
    }

    static Category fromJson(JsonNode node, KiwiClient client) throws IOException
    {
        return Category.of(node.get("id").asLong(),
                client.product().byId(node.get("product").asLong()),
                node.get("name").asText());
    }
}
