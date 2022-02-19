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
package io.gravitee.reporter.tcp.formatter.json;

import io.gravitee.common.http.HttpMethod;
import io.gravitee.reporter.api.Reportable;
import io.gravitee.reporter.api.configuration.Rules;
import io.gravitee.reporter.api.http.Metrics;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
public class JsonFormatterBenchmark {

    JsonFormatter<Reportable> jsonFormatter;

    JsonFormatter<Reportable> jsonFormatterNoFilter;
    Metrics metrics;

    @Setup
    public void setup() {
        Rules rules = new Rules() {
            @Override
            public boolean containsRules() {
                // This will force the registration of the FieldFiltering jackson filter.
                return true;
            }
        };

        jsonFormatter = new JsonFormatter<>(rules);

        Rules noRules = new Rules();
        jsonFormatterNoFilter = new JsonFormatter<>(noRules);

        this.metrics = Metrics.on(System.currentTimeMillis()).build();
        this.metrics.setRequestId("id");
        this.metrics.setHttpMethod(HttpMethod.GET);
        this.metrics.setLocalAddress("192.68.0.1");
        this.metrics.setRemoteAddress("192.168.0.2");
        this.metrics.setHost("host");
        this.metrics.setUri("/my-uri");
        this.metrics.setUserAgent("user-agent");
        this.metrics.setApiResponseTimeMs(202);
        this.metrics.setMessage("message");
        this.metrics.setPath("/path");
        this.metrics.setMappedPath("/path");
        this.metrics.setApi(UUID.randomUUID().toString());
        this.metrics.setPlan(UUID.randomUUID().toString());
        this.metrics.setApplication(UUID.randomUUID().toString());
        this.metrics.setCustomMetrics(Map.of("keyA", "value1", "keyB", "value2"));
        this.metrics.setEndpoint("https://api.gravitee.io/echo");
        this.metrics.setProxyLatencyMs(2);
        this.metrics.setProxyResponseTimeMs(200);
        this.metrics.setStatus(200);
        this.metrics.setTransactionId(UUID.randomUUID().toString());
        this.metrics.setRequestId(UUID.randomUUID().toString());
        this.metrics.setRequestContentLength(0);
        this.metrics.setResponseContentLength(600);
        this.metrics.setSubscription(UUID.randomUUID().toString());
    }

    @Benchmark
    public void benchWithNoJacksonFilter() {
        jsonFormatterNoFilter.format(metrics);
    }

    @Benchmark
    public void benchWithFieldFilteringJacksonFilter() {
        jsonFormatter.format(metrics);
    }
}
