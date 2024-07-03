package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendcs.testing.kiwi.Type;

import com.fasterxml.jackson.databind.JsonNode;

public class TypeRpc
{
    private final KiwiClient client;

    public TypeRpc(KiwiClient client)
    {
        this.client = client;
    }

    public List<Type> filter(Map<String, String> query) throws IOException
    {
        return client.filter("PlanType.filter", TypeRpc::mapType, query);
    }

    public Type byName(String name) throws IOException
    {
        Map<String, String> query = new HashMap<>();
        query.put("name", name);
        return filter(query).stream()
                .findFirst()
                .orElseThrow(() -> new IOException("No type named: " + name));

    }

    public static Type mapType(JsonNode node) throws IOException
    {
        return Type.of(node.get("id").asLong(), node.get("name").asText());
    }
}
