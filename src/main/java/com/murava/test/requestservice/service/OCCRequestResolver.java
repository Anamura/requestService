package com.murava.test.requestservice.service;

import com.murava.test.requestservice.requestdata.Request;
import com.murava.test.requestservice.interfaces.RequestService;
import com.murava.test.requestservice.utils.MetricsCollector;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.*;

/**
 * The class that resolves Concurrency control issue when duplicate request of the same client deducted.
 * Implementation of {@link RequestService}.
 * <p>
 * If @link Request#getRequestId() UUID is repeated), then the completable future
 * associated with the original request is returned.
 */
public class OCCRequestResolver implements RequestService {
    private static final Logger LOGGER = Logger.getLogger(OCCRequestResolver.class);

    private final RequestService executor;
    private final Cache<UUID, UUID> requestIds;
    private final MetricsCollector metrics;

    public OCCRequestResolver(RequestService executor, MetricsCollector metrics) {
        this.executor = executor;
        this.metrics = metrics;
        requestIds = CacheBuilder.newBuilder()
                //requestIds relevant only 20 seconds
                .expireAfterWrite(20, TimeUnit.SECONDS)
                .build();
        metrics.register("cacheSize", requestIds::size);
    }

    /**
     * Deduct duplicate requests and process the correct requests(first one).
     * Duplicate request is sending Error Message.
     *
     * @param request The request to call.
     * @return If request is duplicated, then the future associated with original request returned.
     */
    @Override
    public <T> CompletableFuture<T> execute(Request request) {
        CompletableFuture<T> cf = new CompletableFuture<>();
        final UUID uuid = request.getRequestId();

        if (requestIds.asMap().putIfAbsent(uuid, uuid) == null) {
            return executor.execute(request);
        } else {
            metrics.duplicate();
            cf.completeExceptionally(new OCCException());
            LOGGER.warn("Concurrency control issue: "
                    + "Deducted duplicate request id of the client " + request.getClientId());
            return cf;
        }
    }

    @Override
    public void close() throws TimeoutException {
        requestIds.invalidateAll();
    }
}