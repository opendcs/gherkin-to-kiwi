package org.opendcs.testing.util;

import java.io.IOException;

public interface ThrowingFunction<A,R> {
    R apply(A a) throws IOException;
}
