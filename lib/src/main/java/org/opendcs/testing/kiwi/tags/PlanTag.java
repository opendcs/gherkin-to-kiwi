package org.opendcs.testing.kiwi.tags;

import java.util.Objects;

/**
 * Indicates which Plan(s) a TestCase belongs to.
 */
public class PlanTag implements KiwiTag
{
    public final String planName;

    PlanTag(String args[])
    {
        planName = Objects.requireNonNull(args, "args cannot be null or empty.")[0];
    }
}
