package org.opendcs.testing.kiwi.tags;

import java.util.Objects;

/**
 * Indicates which Plan(s) a TestCase belongs to.
 */
public class PriorityTag implements KiwiTag
{
    public final String priorityName;

    PriorityTag(String[] args)
    {
        priorityName = Objects.requireNonNull(args, "args cannot be null or empty.")[0];
    }
}
