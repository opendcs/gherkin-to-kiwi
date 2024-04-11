package org.opendcs.util;

import java.util.function.Consumer;

public class FailableResult<SuccessType,FailType> {
    private final SuccessType successResult;
    private final FailType failResult;

    private FailableResult(SuccessType successResult, FailType failResult) {
        this.successResult = successResult;
        this.failResult = failResult;
    }

    public boolean isSuccess() {
        return failResult == null;
    }

    public boolean isFailure() {
        return failResult != null;
    }

    public SuccessType getSuccess() {
        if (isFailure()) {
            throw new IllegalStateException("Attempt to retrieve 'success' result of a failure result.");
        }
        return successResult;
    }

    public FailType getFailure() {
        if (!isFailure()) {
            throw new IllegalStateException("Attempt to retrieve 'failure' result of a succesfull result.");
        }
        return failResult;
    }

    public void handleError(Consumer<FailType> consumer) {
        if (isFailure()) {
            consumer.accept(failResult);
        }
    }

    public static <SuccessType, FailType> FailableResult<SuccessType,FailType> success(SuccessType successResult) {
        return new FailableResult<>(successResult, null);
    }

    public static <SuccessType, FailType> FailableResult<SuccessType,FailType> failure(FailType failResult) {
        return new FailableResult<>(null, failResult);
    }
}
