package org.opendcs.testing.gherkin;

public class ProcessingError {
    private final Throwable cause;
    private final String msg;

    public ProcessingError(String msg) {
        this(msg,null);
    }

    public ProcessingError(Throwable cause) {
        this(null,cause);
    }

    public ProcessingError(String msg, Throwable cause) {
        this.msg = msg;
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }

    public String getMessage() {
        return msg;
    }
}
