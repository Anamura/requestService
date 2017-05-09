package com.murava.test.requestservice;

import com.codahale.metrics.MetricRegistry;
import com.murava.test.requestservice.requestdata.RequestExecutor;
import com.murava.test.requestservice.interfaces.RequestService;
import com.murava.test.requestservice.service.OCCRequestResolver;
import com.murava.test.requestservice.service.RequestPoolService;
import com.murava.test.requestservice.utils.MetricsCollector;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import java.util.concurrent.TimeoutException;

/**
 * Main class that executes the sample load on the request service.
 * Represents pipeline of processing request, wrapping original {@link RequestExecutor}.
 * It delegates on {@link RequestPoolService}, {@link OCCRequestResolver} .
 */
public class Main {
    public static void main(String[] args) throws TimeoutException, InterruptedException {
        //Configure logger
        BasicConfigurator.configure();
        PropertyConfigurator.configure("log4j.properties");
        ServiceRunner serviceRunner = new ServiceRunner();
        serviceRunner.run(Main::createRequestExecutorService);
    }

    private static RequestService createRequestExecutorService(MetricRegistry metricRegistry) {
        MetricsCollector metrics =
                new MetricsCollector(metricRegistry);
        RequestExecutor requestExecutor =
                new RequestExecutor(metrics);
        RequestPoolService requestPool =
                new RequestPoolService(requestExecutor, metrics);
        return new OCCRequestResolver(requestPool, metrics);
    }
}