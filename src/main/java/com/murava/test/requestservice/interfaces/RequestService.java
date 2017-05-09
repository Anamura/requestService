
package com.murava.test.requestservice.interfaces;

import com.murava.test.requestservice.requestdata.Request;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * The class that implements the service.
 *
 * It must delegate (directly or indirectly) on a {RequestExecutor}.
 */
@ThreadSafe
public interface RequestService extends AutoCloseable {
  /**
   * Executes the given service and returns a completable future that will be done once the service
   * is executed.
   *
   * @return a completable future that will be done once the service is executed. If the service is
   *         filtered (because {@link com.murava.test.requestservice.requestdata.Request#getRequestId() its UUID} is repeated), then the future
   *         associated with the original service is returned.
   */
  <T> CompletableFuture<T> execute(Request request);

  /**
   * {@inheritDoc }
   * @throws TimeoutException if it is impossible to release the resources on a
   *                          <em>acceptable</em> time
   */
  @Override
  public void close() throws TimeoutException;
}