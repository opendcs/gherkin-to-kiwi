package org.opendcs.testing.kiwi.tags;

import java.util.Objects;

public class PlanTag implements KiwiTag {
    public final String planName;

    PlanTag(String args[]) {
        planName = Objects.requireNonNull(args, "args cannot be null or empty.")[0];
    }
}
