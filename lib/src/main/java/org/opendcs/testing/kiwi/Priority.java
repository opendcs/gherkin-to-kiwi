package org.opendcs.testing.kiwi;

import java.util.HashMap;
import java.util.Map;

public class Priority
{
    private static Map<Long,Priority> priorities = new HashMap<>();;
    
    public final long id;
    public final String name;

    private Priority(long id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public static Priority of(long id, String priorityName)
    {
        return priorities.computeIfAbsent(id, key -> {
            return new Priority(id, priorityName);
        });        
    }

    public static Priority of(final String priorityName) {
        return priorities.values()
                  .stream()
                  .filter(p -> p.name.equals(priorityName))
                  .findFirst()
                  .orElseGet(() -> new Priority(-1, priorityName));
    }
}
