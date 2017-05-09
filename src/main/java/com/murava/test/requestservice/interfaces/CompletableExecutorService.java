
package com.murava.test.requestservice.interfaces;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * CompletableExecutorService interface returns {@code CompletableFuture}
 * instead of {@code Future}, allows execution of code upon request completion.
 */
public interface CompletableExecutorService {
    /**
     * Submits the Runnable service for execution.
     * Executes the given service and returns a completable future that will be done once the service.
     *
     * @param request The request to execute.
     * @return The completable future of the submitted service.
     */
    CompletableFuture<?> submit(Runnable request);

    /**
     * Submits the Callable for execution.
     * Request process only after the previous submitted request of the same client completed.
     *
     * @param <T>     Type of return value of the service.
     * @param key     The id identifying the client that the submitted request belongs to.
     * @param request The request to execute.
     * @return The completable future of the submitted request.
     */
    <T> CompletableFuture<T> submit(Integer key, Callable<T> request);

    /**
     * Submits the Callable service for execution.
     * Executes the given service and returns a completable future that will be done once the service.
     *
     * @param request The request to execute.
     * @return The completable future of the submitted service.
     */
    <T> CompletableFuture<T> submit(Callable<T> request);
}
