package org.opendcs.testing.util;

import java.util.function.Function;

public interface ThrowingFunction<ArgType,ResultType> {
    ResultType apply(ArgType a) throws Throwable;

    public static <ResultType,ArgType> Function<ArgType,FailableResult<ResultType,Throwable>> wrap(ThrowingFunction<ArgType,ResultType> func) {
        return arg -> {
            try {
                return FailableResult.success(func.apply(arg));
            } catch (Throwable t) {
                return FailableResult.failure(t);
            }
        };
    }
} 
