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
package io.gravitee.reporter.tcp.configuration;

import io.gravitee.common.util.EnvironmentUtils;
import io.gravitee.reporter.api.configuration.Rules;
import io.gravitee.reporter.tcp.MetricsType;
import io.gravitee.reporter.tcp.formatter.Type;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class TcpReporterConfiguration {

  private static final String TCP_REPORTER_PREFIX = "reporters.tcp.";

  @Value("${reporters.tcp.output:json}")
  private String outputType;

  @Value("${reporters.tcp.enabled:false}")
  private boolean enabled;

  @Value("${reporters.tcp.host:localhost}")
  private String host;

  @Value("${reporters.tcp.port:8123}")
  private int port;

  @Value("${reporters.tcp.connectTimeout:10000}")
  private int connectTimeout;

  @Value("${reporters.tcp.retryTimeout:5000}")
  private long retryTimeout;

  @Value("${reporters.tcp.reconnectAttempts:10}")
  private int reconnectAttempts;

  @Value("${reporters.tcp.reconnectInterval:500}")
  private long reconnectInterval;

  @Autowired
  private ConfigurableEnvironment environment;

  public boolean isEnabled() {
    return enabled;
  }

  public Type getOutputType() {
    return outputType == null
      ? Type.JSON
      : Type.valueOf(outputType.toUpperCase());
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public long getRetryTimeout() {
    return retryTimeout;
  }

  public int getReconnectAttempts() {
    return reconnectAttempts;
  }

  public long getReconnectInterval() {
    return reconnectInterval;
  }

  public Rules getRules(MetricsType type) {
    Rules rules = new Rules();

    rules.setRenameFields(
      getMapProperties(TCP_REPORTER_PREFIX + type.getType() + ".rename")
    );
    rules.setExcludeFields(
      getArrayProperties(TCP_REPORTER_PREFIX + type.getType() + ".exclude")
    );
    rules.setIncludeFields(
      getArrayProperties(TCP_REPORTER_PREFIX + type.getType() + ".include")
    );

    return rules;
  }

  private Map<String, String> getMapProperties(String prefix) {
    Map<String, Object> properties = EnvironmentUtils.getPropertiesStartingWith(
      environment,
      prefix
    );
    if (!properties.isEmpty()) {
      return properties
        .entrySet()
        .stream()
        .collect(
          Collectors.toMap(
            entry ->
              entry
                .getKey()
                .substring(EnvironmentUtils.encodedKey(prefix).length() + 1),
            entry -> entry.getValue().toString()
          )
        );
    } else {
      return Collections.emptyMap();
    }
  }

  private Set<String> getArrayProperties(String prefix) {
    final Set<String> properties = new HashSet<>();

    boolean found = true;
    int idx = 0;

    while (found) {
      String property = environment.getProperty(prefix + '[' + idx++ + ']');
      found = (property != null && !property.isEmpty());

      if (found) {
        properties.add(property);
      }
    }

    return properties;
  }
}
