package com.murava.test;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.murava.test.requestservice.requestdata.RequestExecutor;
import com.murava.test.requestservice.requestdata.Request;
import com.murava.test.requestservice.service.OCCRequestResolver;
import com.murava.test.requestservice.service.RequestPoolService;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.murava.test.requestservice.utils.MetricsCollector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RequestPoolServiceTest {

    private static final UUID REQUEST_ID = UUID.fromString("0000000-0000-0000-0000-000000000000");
    private static final Request REPEATED_REQUEST = new Request(6666, REQUEST_ID);

    private static MetricsCollector metrics;
    private static RequestExecutor requestExecutor;
    private static RequestPoolService requestPoolService;
    private static OCCRequestResolver oCCRequestResolver;


    @Before
    public void setUp() throws Exception {
        MetricRegistry metricRegistry = mock(MetricRegistry.class);
        Meter meter = mock(Meter.class);
        Timer timer = mock(Timer.class);
        when(metricRegistry.timer("service-latency")).thenReturn(timer);
        when(metricRegistry.meter(anyString())).thenReturn(meter);
        metrics = new MetricsCollector(metricRegistry);

        requestExecutor = new RequestExecutor(metrics);
        requestPoolService = new RequestPoolService(requestExecutor, metrics);
        oCCRequestResolver = new OCCRequestResolver(requestPoolService, metrics);
        verify(metricRegistry, times(1)).timer("service-latency");
    }

    @Test
    public void testExecuteRequestWithSameClientIdSequentially() throws Exception {
        Map<Request, CompletableFuture<?>> futurePerRequest = new LinkedHashMap<>();
        CompletableFuture<?> future = new CompletableFuture<>();
        try {
            for (int clientId : new int[]{6624, 6625, 6626}) {
                for (int i = 1; i < 10; i++) {
                    Request request = new Request(Instant.now(), clientId,
                            UUID.fromString(REQUEST_ID.toString() + i));
                    future = requestPoolService.execute(request);
                    futurePerRequest.put(request, future);
                }
            }
        } finally {
            requestPoolService.shutdown();
        }
        getFutureResults(futurePerRequest);
        assertThat(future.isDone(), is(true));
        assertEquals(future.get(), 6626);
    }

    @Test
    public void testDuplicateRequestIdOfTheSameClientDeducted() throws ExecutionException {
        Map<Request, CompletableFuture<?>> results = new LinkedHashMap<>();
        CompletableFuture<?> future = new CompletableFuture<>();
        for (int i = 1; i < 10; i++) {
            future = oCCRequestResolver.execute(REPEATED_REQUEST);
            results.put(REPEATED_REQUEST, future);
        }
        getFutureResults(results);
        assertTrue(future.isDone());
        assertEquals(results.size(), 1);
    }

    private static void getFutureResults(Map<Request, CompletableFuture<?>> results) {
        results.forEach((request, result) -> {
            try {
                result.get();
                System.out.println("Completed Request clientId:" + request.getClientId()
                        + " requestId: " + request.getRequestId());
            } catch (InterruptedException | ExecutionException ex) {
                System.out.println("Failed to execute request " + request + " Reason " + ex.getMessage());
				// Thread.interrupt();
            }
        });
    }
}