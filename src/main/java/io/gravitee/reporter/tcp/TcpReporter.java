/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.reporter.tcp;

import io.gravitee.common.service.AbstractService;
import io.gravitee.reporter.api.Reportable;
import io.gravitee.reporter.api.Reporter;
import io.gravitee.reporter.tcp.configuration.TcpReporterConfiguration;
import io.gravitee.reporter.tcp.formatter.Formatter;
import io.gravitee.reporter.tcp.formatter.FormatterFactory;
import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public final class TcpReporter extends AbstractService implements Reporter {

  private final Logger logger = LoggerFactory.getLogger(TcpReporter.class);

  @Autowired
  private TcpReporterConfiguration configuration;

  @Autowired
  private Vertx vertx;

  private NetClient netClient;

  private NetSocket netSocket;

  private Map<Class<? extends Reportable>, Formatter> formatters = new HashMap<>(
    4
  );

  private CircuitBreaker circuitBreaker;

  /**
   * {@code \u000a} linefeed LF ('\n').
   *
   * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6">JLF: Escape Sequences
   *      for Character and String Literals</a>
   * @since 2.2
   */
  private static final char LF = '\n';

  /**
   * {@code \u000d} carriage return CR ('\r').
   *
   * @see <a href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6">JLF: Escape Sequences
   *      for Character and String Literals</a>
   * @since 2.2
   */
  private static final char CR = '\r';

  private static final byte[] END_OF_LINE = new byte[] { CR, LF };

  @Override
  public boolean canHandle(Reportable reportable) {
    return (
      configuration.isEnabled() && formatters.containsKey(reportable.getClass())
    );
  }

  @Override
  public void report(Reportable reportable) {
    if (configuration.isEnabled()) {
      if (netSocket != null) {
        final Buffer data = formatters
          .get(reportable.getClass())
          .format(reportable);

        if (data != null) {
          netSocket.write(data.appendBytes(END_OF_LINE));
        }
      } else {
        logger.warn("TCP reporter not connected, skipping data...");
      }
    }
  }

  @Override
  public void doStart() throws Exception {
    if (configuration.isEnabled()) {
      logger.info("Starting TCP reporter...");

      // Initialize reporters
      for (MetricsType type : MetricsType.values()) {
        Formatter formatter = FormatterFactory.getFormatter(
          configuration.getOutputType()
        );
        applicationContext
          .getAutowireCapableBeanFactory()
          .autowireBean(formatter);

        formatters.put(type.getClazz(), formatter);
      }

      circuitBreaker =
        CircuitBreaker
          .create(
            "tcp-reporter",
            vertx,
            new CircuitBreakerOptions()
              .setMaxRetries(Integer.MAX_VALUE)
              .setTimeout(configuration.getConnectTimeout())
          )
          .retryPolicy(integer -> configuration.getRetryTimeout());

      netClient =
        vertx.createNetClient(
          new NetClientOptions()
            .setConnectTimeout(configuration.getConnectTimeout())
            .setReconnectAttempts(configuration.getReconnectAttempts())
            .setReconnectInterval(configuration.getReconnectInterval())
        );

      connect();
    }
  }

  private void connect() {
    circuitBreaker
      .execute(this::doConnect)
      .onComplete(
        event -> {
          // The connection has been established
          if (event.succeeded()) {
            netSocket = event.result();
            netSocket
              .closeHandler(
                event1 -> {
                  netSocket = null;
                  logger.info(
                    "TCP reporter connection has been closed, trying to reconnect..."
                  );
                  // How to force to reconnect ?
                  connect();
                }
              )
              .exceptionHandler(
                throwable -> {
                  netSocket = null;
                  logger.error(
                    "An error occurs with the TCP reporter",
                    throwable
                  );
                }
              );
          } else {
            // Retry the connection
            connect();
          }
        }
      );
  }

  private void doConnect(Promise<NetSocket> netSocketPromise) {
    netClient.connect(
      configuration.getPort(),
      configuration.getHost(),
      event -> {
        if (event.failed()) {
          netSocketPromise.fail(event.cause());
          logger.error(
            "An error occurs while trying to connect TCP reporter to {}:{}",
            configuration.getHost(),
            configuration.getPort(),
            event.cause()
          );
        } else {
          netSocketPromise.complete(event.result());
          logger.info(
            "TCP reporter connected to {}:{}",
            configuration.getHost(),
            configuration.getPort()
          );
        }
      }
    );
  }

  @Override
  protected void doStop() throws Exception {
    if (configuration.isEnabled() && netClient != null) {
      logger.info("Stopping TCP reporter...");

      if (netSocket != null) {
        netSocket.close(
          event -> logger.info("TCP reporter socket closed successfully")
        );
      }

      netClient.close();

      logger.info("Stopping TCP reporter... DONE");
    }
  }
}
