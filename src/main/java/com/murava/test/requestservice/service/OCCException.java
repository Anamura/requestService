package com.murava.test.requestservice.service;

import java.util.concurrent.ExecutionException;

/**
 * Concurrency control Exception occurs when duplicate request id of the client is deducted.
 */
public class OCCException extends ExecutionException {

    private static final long serialVersionUID = 1L;

    public OCCException() {
        super();
    }

    /**
     * Constructor.
     *
     * @param message Return the error message.
     */
    public OCCException(String message) {
        super(message);
    }

    public OCCException(String message, Throwable cause) {
        super(message, cause);
    }

    public OCCException(Throwable cause) {
        super(cause);
    }
}