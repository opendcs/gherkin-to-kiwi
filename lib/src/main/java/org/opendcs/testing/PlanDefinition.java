package org.opendcs.testing;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class PlanDefinition
{
    public final String id;
    public final String name;
    public final String type;
    private static ObjectMapper jsonMapper = new ObjectMapper();
    static
    {
        jsonMapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
    }
    
    @JsonCreator
    public PlanDefinition(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("type") String type)
    {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    /**
     * Read available plan definitions from the given file.
     * @param jsonData
     * @return map of id -> plan def;
     */
    public static Map<String,PlanDefinition> from(Path jsonData) throws IOException {
        final Map<String,PlanDefinition> plans = new HashMap<>();
        List<PlanDefinition> definitions = jsonMapper.readValue(jsonData.toUri().toURL(), new TypeReference<List<PlanDefinition>>(){});
        definitions.forEach(planDefinition ->
        {
            plans.put(planDefinition.id, planDefinition);
        });
        return plans;
    }
    
}
