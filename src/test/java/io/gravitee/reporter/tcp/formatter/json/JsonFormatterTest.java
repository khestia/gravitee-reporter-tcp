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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.gateway.api.http.HttpHeaders;
import io.gravitee.reporter.api.common.Request;
import io.gravitee.reporter.api.common.Response;
import io.gravitee.reporter.api.log.Log;
import io.vertx.core.buffer.Buffer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonFormatterTest {

    private final ObjectMapper mapper = new ObjectMapper();
    JsonFormatter<Log> formatter = new JsonFormatter<>(null);

    @Test
    public void shouldSerializeClientRequestHeaders() throws JsonProcessingException {
        Log log = new Log(System.currentTimeMillis());
        Request clientRequest = new Request();
        HttpHeaders httpHeaders = HttpHeaders.create();
        httpHeaders.add("header1", "value1");
        httpHeaders.add("header2", "value2");
        httpHeaders.add("header1", "value3");
        clientRequest.setHeaders(httpHeaders);
        log.setClientRequest(clientRequest);

        Buffer payload = formatter.format(log);

        JsonNode node = mapper.readTree(payload.toString());

        assertTrue(node.has("clientRequest"));
        assertTrue(node.get("clientRequest").has("headers"));
        assertEquals("{\"header1\":[\"value1\",\"value3\"],\"header2\":[\"value2\"]}", node.get("clientRequest").get("headers").toString());
    }

    @Test
    public void shouldSerializeProxyRequestHeaders() throws JsonProcessingException {
        Log log = new Log(System.currentTimeMillis());
        Request proxyRequest = new Request();
        HttpHeaders httpHeaders = HttpHeaders.create();
        httpHeaders.add("header1", "value1");
        httpHeaders.add("header2", "value2");
        httpHeaders.add("header1", "value3");
        proxyRequest.setHeaders(httpHeaders);
        log.setProxyRequest(proxyRequest);

        Buffer payload = formatter.format(log);

        JsonNode node = mapper.readTree(payload.toString());

        assertTrue(node.has("proxyRequest"));
        assertTrue(node.get("proxyRequest").has("headers"));
        assertEquals("{\"header1\":[\"value1\",\"value3\"],\"header2\":[\"value2\"]}", node.get("proxyRequest").get("headers").toString());
    }

    @Test
    public void shouldSerializeClientResponseHeaders() throws JsonProcessingException {
        Log log = new Log(System.currentTimeMillis());
        Response clientResponse = new Response();
        HttpHeaders httpHeaders = HttpHeaders.create();
        httpHeaders.add("header1", "value1");
        httpHeaders.add("header2", "value2");
        httpHeaders.add("header1", "value3");
        clientResponse.setHeaders(httpHeaders);
        log.setClientResponse(clientResponse);

        Buffer payload = formatter.format(log);

        JsonNode node = mapper.readTree(payload.toString());

        assertTrue(node.has("clientResponse"));
        assertTrue(node.get("clientResponse").has("headers"));
        assertEquals(
            "{\"header1\":[\"value1\",\"value3\"],\"header2\":[\"value2\"]}",
            node.get("clientResponse").get("headers").toString()
        );
    }

    @Test
    public void shouldSerializeProxyResponseHeaders() throws JsonProcessingException {
        Log log = new Log(System.currentTimeMillis());
        Response proxyResponse = new Response();
        HttpHeaders httpHeaders = HttpHeaders.create();
        httpHeaders.add("header1", "value1");
        httpHeaders.add("header2", "value2");
        httpHeaders.add("header1", "value3");
        proxyResponse.setHeaders(httpHeaders);
        log.setProxyResponse(proxyResponse);

        Buffer payload = formatter.format(log);

        JsonNode node = mapper.readTree(payload.toString());

        assertTrue(node.has("proxyResponse"));
        assertTrue(node.get("proxyResponse").has("headers"));
        assertEquals("{\"header1\":[\"value1\",\"value3\"],\"header2\":[\"value2\"]}", node.get("proxyResponse").get("headers").toString());
    }
}
