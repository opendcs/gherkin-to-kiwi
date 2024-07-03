package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opendcs.testing.kiwi.Product;

import com.fasterxml.jackson.databind.JsonNode;

public final class ProductRpc
{
    private final KiwiClient client;

    public ProductRpc(KiwiClient client)
    {
        this.client = client;
    }

    public Product create(Product product) throws IOException
    {
        return client.create("Product.create",
                null,
                ProductRpc::jsonToProduct,
                productElementsToMap(product));
    }

    public List<Product> filter(Map<String, String> query) throws IOException
    {
        return client.filter("Product.filter", ProductRpc::jsonToProduct, query);
    }

    public Product byId(long id) throws IOException
    {
        Optional<Product> p = Product.existingOfId(id);
        if (p.isPresent())
        {
            return p.get();
        }
        else
        {
            Map<String, String> query = new HashMap<>();
            query.put("id", "" + id);
            return filter(query).stream()
                    .findFirst()
                    .orElseThrow(() -> new IOException("No product with id = " + id));
        }
    }

    private static Map<String, Object> productElementsToMap(Product product)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("name", product.name);
        return map;
    }

    private static Product jsonToProduct(JsonNode node)
    {
        return Product.of(node.get("id").asLong(), node.get("name").asText());
    }
}
