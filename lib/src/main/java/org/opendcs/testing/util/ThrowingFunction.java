package org.opendcs.testing.util;

import java.io.IOException;

/**
 * A Function interface for functions that may need to throw an IOException
 */
@FunctionalInterface
public interface ThrowingFunction<A,R> {
    R apply(A a) throws IOException;
}
