
package com.murava.test.requestservice.requestdata;

import com.murava.test.requestservice.utils.MetricsCollector;
import org.apache.log4j.Logger;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;


/**
 * The class that executes the service.
 */
@ThreadSafe
public class RequestExecutor {
  private static final Logger LOGGER = Logger.getLogger(RequestExecutor.class);

  private static final long REQUEST_COST_MS = 5;
  private final MetricsCollector metrics;

  public RequestExecutor(MetricsCollector metrics) { this.metrics = metrics; }

  /**
   * The method that executes the service.
   *
   * The execution is simulated by sleeping the current thread for a fixed number
   * of milliseconds. It also registers the latency for each service.
   * @param request The request to call.
   */
  public void execute(Request request) {
    try {
      LOGGER.debug("Executing " + request);
      Duration latency = Duration.between(request.getCreated(), Instant.now());
      metrics.timer(latency);
      TimeUnit.MILLISECONDS.sleep(REQUEST_COST_MS);
      LOGGER.debug("Execution complete " + request);
    } catch (InterruptedException ex) {
      Thread.interrupted();
      throw new RuntimeException(ex);
    }
  }
}