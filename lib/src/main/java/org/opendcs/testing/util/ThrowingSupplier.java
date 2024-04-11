package org.opendcs.testing.util;

import java.io.IOException;

/**
 * A supplier interface for functions that may need to throw an IOException.
 */
@FunctionalInterface
public interface ThrowingSupplier<R> {
    R get() throws IOException;
}
