package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendcs.testing.kiwi.Product;
import org.opendcs.testing.kiwi.Version;

import com.fasterxml.jackson.databind.JsonNode;

public class VersionRpc
{
    private final KiwiClient client;

    public VersionRpc(KiwiClient client)
    {
        this.client = client;
    }

    public List<Version> filter(Map<String,String> query) throws IOException
    {
        return client.filter("Version.filter", VersionRpc::mapVersion, query);
    }

    public Version create(Version version) throws IOException
    {
        long id = client.create("Version.create",
                                null,
                                (node) -> mapVersion(node).id,
                                versionElementsToMap(version, client)
                                );
        return byId(id);
    }

    public Version byId(long id) throws IOException
    {
        Map<String,String> query = new HashMap<>();
        query.put("id", ""+id);
        return filter(query).stream()
                            .findFirst()
                            .orElseThrow(() -> new IOException("Version with id '" + id + "' could not be found."));
    }

    public static Map<String,Object> versionElementsToMap(Version version, KiwiClient client) throws IOException
    {
        Map<String,Object> versionElements = new HashMap<>();
        versionElements.put("value", version.name);
        Map<String,String> productQuery = new HashMap<>();
        productQuery.put("name", version.product.name);        
        long productId = client.product()
                               .filter(productQuery)
                               .stream()
                               .findFirst()
                               .orElseThrow(() -> new IOException("No Product named '" + version.product.name + "' exists in the target Kiwi Instance."))
                               .id;
        versionElements.put("product", productId);

        return versionElements;
        
    }

    public static Version mapVersion(JsonNode node) throws IOException
    {
        Product product = Product.of(node.get("product__name").asText());
        return Version.of(
                    node.get("id").asLong(),
                    node.get("value").asText(),
                    product
                );
    }
}
