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

import io.gravitee.reporter.tcp.formatter.Type;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class TcpReporterConfiguration {

    @Value("${reporters.tcp.enabled:true}")
    private boolean enabled;

    @Value("${reporters.tcp.output:json}")
    private String outputType;

    @Value("${reporters.tcp.host:localhost}")
    private String host;

    @Value("${reporters.tcp.port:8123}")
    private int port;

    @Value("${reporters.tcp.connectTimeout:10000}")
    private int connectTimeout;

    @Value("${reporters.tcp.retryTimeout:5000}")
    private long retryTimeout;

    public boolean isEnabled() {
        return enabled;
    }

    public Type getOutputType() {
        return outputType == null ? Type.JSON : Type.valueOf(outputType.toUpperCase());
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
}
