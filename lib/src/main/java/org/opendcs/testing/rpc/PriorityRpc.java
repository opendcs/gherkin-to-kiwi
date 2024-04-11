package org.opendcs.testing.rpc;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendcs.testing.kiwi.Priority;

import com.fasterxml.jackson.databind.JsonNode;

public class PriorityRpc {
    private final KiwiClient client;

    public PriorityRpc(KiwiClient client) {
        this.client = client;
    }

    public List<Priority> filter(Map<String,String> query) throws IOException {
        return client.filter("Priority.filter", PriorityRpc::mapPriority, query);
    }

    public Priority byName(String name) throws IOException {
        Map<String,String> priorityQuery = new HashMap<>();
        priorityQuery.put("value",name);
        return filter(priorityQuery)
                     .stream()
                     .findFirst()
                     .orElseThrow(() -> new IOException("No priority named: " + name));
        
    }

    private static Priority mapPriority(JsonNode node) throws IOException {
        return Priority.of(node.get("id").asLong(), node.get("value").asText());
    }
}
