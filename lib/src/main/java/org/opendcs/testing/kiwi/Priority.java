package org.opendcs.testing.kiwi;

import java.util.ArrayList;
import java.util.List;

public class Priority
{
    private static List<Priority> priorities = new ArrayList<>();;
    
    public final String name;

    public Priority(String name)
    {
        this.name = name;
    }

    public Priority fromString(String priorityName)
    {
        return null;
    }
}
