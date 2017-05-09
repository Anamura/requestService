
package com.murava.test.requestservice;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.murava.test.requestservice.interfaces.RequestService;
import com.murava.test.requestservice.requestdata.Request;
import com.google.common.util.concurrent.MoreExecutors;
import scala.concurrent.Await;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;


/**
 * A class used to run a simulate the usage of a given {@link RequestService}.
 *
 * This class is implemented using Akka Streams, but it should not be necessary to know how it works
 * as it should simply work for you.
 */
public class ServiceRunner {

    private static final Random RANDOM = new Random(24385102);
    private static final FiniteDuration MAX_TIME = FiniteDuration.create(1, TimeUnit.MINUTES);
    private static final int REQ_PER_SEC = 500;
    private static final Request REPEATED_REQUEST = new Request(RANDOM.nextInt(10_000));

    /**
     * The method used to simulate the usage of a {@link RequestService}.
     *
     * @param requestServiceSupplier a supplier that, given a {@link MetricRegistry}, returns the
     *                               {@link RequestService} that will be used.
     * @throws TimeoutException     thrown if there is a timeout when the execution is terminated.
     * @throws InterruptedException thrown if this thread is interrupted before the execution is
     *                              terminated.
     */
    public void run(Function<MetricRegistry, RequestService> requestServiceSupplier)
            throws TimeoutException, InterruptedException {
        MetricRegistry metricRegistry = new MetricRegistry();
        ScheduledReporter reporter = createReporter(metricRegistry);

        ActorSystem actorSystem = ActorSystem.create("service-executor-test");
        try (RequestService requestService = requestServiceSupplier.apply(metricRegistry)) {
            Materializer materializer = ActorMaterializer.create(actorSystem);
            simulateClients(materializer, requestService);
        } finally {
            Await.ready(
                    actorSystem.terminate(),
                    scala.concurrent.duration.Duration.create(10, TimeUnit.MINUTES)
            );
            reporter.stop();
        }
    }

    private void simulateClients(Materializer materializer, RequestService requestService) {
        createSource()
                .takeWithin(MAX_TIME)
                .map(this::createRequest)
                .buffer(10_000, OverflowStrategy.fail())
                .async()
                .map(req -> executeRequest(requestService, req))
                .runWith(Sink.ignore(), materializer)
                .toCompletableFuture()
                .join();
    }

    private ScheduledReporter createReporter(MetricRegistry registry) {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(registry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.SECONDS);
        return reporter;
    }

    private Source<NotUsed, ?> createSource() {
        int listSize = REQ_PER_SEC / 10;
        List<NotUsed> perSecondList = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {
            perSecondList.add(NotUsed.getInstance());
        }
        return Source.tick(
                FiniteDuration.Zero(),
                FiniteDuration.create(100, TimeUnit.MILLISECONDS),
                perSecondList)
                .mapConcat(l -> l);
    }

    private Request createRequest(NotUsed notUsed) {
        int nextInt = RANDOM.nextInt(1000000);
        if ((nextInt % 1000) == 0) {
            return REPEATED_REQUEST.duplicate();
        }
        return new Request(nextInt);
    }

    private CompletableFuture<Request> executeRequest(RequestService requestService, Request req) {
        return requestService.execute(req)
                .thenApplyAsync(v -> req, MoreExecutors.directExecutor());
    }
}
