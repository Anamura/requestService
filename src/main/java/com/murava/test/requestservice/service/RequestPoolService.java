package com.murava.test.requestservice.service;

import com.murava.test.requestservice.interfaces.CompletableExecutorService;
import com.murava.test.requestservice.requestdata.Request;
import com.murava.test.requestservice.requestdata.RequestExecutor;
import com.murava.test.requestservice.interfaces.RequestService;
import com.murava.test.requestservice.utils.MetricsCollector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import static java.util.concurrent.Executors.callable;

/**
 * The class represent {@link RequestService}, {@link CompletableExecutorService} implementation,
 * executes all service on the caller thread. Wraps executor service to return covariant type
 * {@code CompletableFuture}. Requests associated with the same client executed sequentially.
 */

public class RequestPoolService implements CompletableExecutorService, RequestService {
    private static final Logger LOGGER = Logger.getLogger(RequestPoolService.class);

    protected RequestExecutor delegate;
    private ExecutorService executor;
    private boolean isShutdown;
    private static final int POOL_SIZE = 4;
    private static final int MAX_POOL_SIZE = 4;
    private static final long KEEP_ALIVE = 0L;

    /*
    * Maps identify client id to future of last request submitted by that client.
    */
    private final Map<Integer, CompletableFuture<?>> futurePerClient = new ConcurrentHashMap<>();

    public RequestPoolService(RequestExecutor delegate, MetricsCollector metrics) {
        if (delegate == null) {
            throw new IllegalArgumentException();
        }
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        metrics.register("queue", queue::size);
        this.executor = new ThreadPoolExecutor(
                POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE,
                TimeUnit.MILLISECONDS,
                queue
        );
        this.delegate = delegate;
    }

    public void shutdown() {
        isShutdown = true;
    }

    @Override
    public CompletableFuture<?> submit(Runnable request) {
        return submit(callable(request));
    }

    @Override
    public <T> CompletableFuture<T> submit(Integer clientId, Callable<T> request) {
        if (isShutdown) {
            throw new RejectedExecutionException();
        }
        CompletableFuture<?> lastFuture = futurePerClient.computeIfAbsent(clientId,
                CompletableFuture::completedFuture);
        CompletableFuture<T> nextFuture = lastFuture.thenApplyAsync(callback(request), executor);
        futurePerClient.put(clientId, nextFuture);

        nextFuture.whenComplete((result, error) -> {
            if (isShutdown && futurePerClient.values().parallelStream().allMatch(Future::isDone))
                executor.shutdown();
        }).exceptionally(error -> {
            if (error instanceof CancellationException) {
                LOGGER.error("Execution of the Request was cancelled " + error.getMessage());
            }
            return null;
        });

        return nextFuture;
    }

    /**
     * Returns the function to call the specified request.
     *
     * @param <T>     Type of return value of the service.
     * @param request The request to call.
     * @return The function to call the specified request, returns the result of the call.
     * @throws CompletionException waiting for completion requests fail with the same exceptions.
     */
    private <T> Function<Object, T> callback(Callable<T> request) {
        return previousResult -> {
            try {
                return request.call();
            } catch (Exception ex) {
                throw new CompletionException(ex);
            }
        };
    }

    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public <T> CompletableFuture<T> submit(Callable<T> request) {
        final CompletableFuture<T> cf = new CompletableFuture<>();
        executor.submit(() -> {
            try {
                cf.complete(request.call());
            } catch (CancellationException e) {
                cf.cancel(true);
            } catch (Exception e) {
                cf.completeExceptionally(e);
            }
        });
        return cf;
    }

    /**
     * Executes the given service and returns the completable future that will be done once the service
     * is executed.
     *
     * @param request The request to call.
     * @return The completable future that will be done once the service is executed.
     */
    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<Integer> execute(Request request) {
        if (isShutdown) {
            throw new RejectedExecutionException();
        }
        return submit(request.getClientId(),
                () -> {
                    delegate.execute(request);
                    return request.getClientId();
                });
    }

    @Override
    public void close() throws TimeoutException {
    }
}