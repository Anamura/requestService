package com.murava.test.requestservice.utils;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * The class used to collect metric instances.
 *
 */
public class MetricsCollector {

    private final MetricRegistry metricRegistry;
    private final Timer latencyTimer;
    private final Meter duplicates;

    public MetricsCollector(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        this.latencyTimer = metricRegistry.timer("service-latency");
        this.duplicates = metricRegistry.meter("duplicates");
    }

    public void timer(Duration latency) {
        latencyTimer.update(latency.toMillis(), TimeUnit.MILLISECONDS);
    }
    public void duplicate() {
        duplicates.mark();
    }

    public void register(String name, Gauge<? extends Number> size) {
        metricRegistry.register(name, size);
    }
}