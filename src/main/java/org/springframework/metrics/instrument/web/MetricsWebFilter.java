/**
 * Copyright 2017 Pivotal Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.metrics.instrument.web;

import org.springframework.metrics.instrument.MeterRegistry;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

public class MetricsWebFilter implements WebFilter {
    private final MeterRegistry registry;
    private final WebMetricsTagProvider tagProvider;

    public MetricsWebFilter(MeterRegistry registry, WebMetricsTagProvider tagProvider) {
        this.registry = registry;
        this.tagProvider = tagProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        final long start = System.nanoTime();
        Mono<Void> filtered = chain.filter(exchange);
        return filtered
                .doOnSuccess(done ->
                        registry.timer("http-request", tagProvider.httpRequestTags(exchange, null, null))
                                .record(System.nanoTime() - start, TimeUnit.NANOSECONDS)
                )
                .doOnError(t ->
                        registry.timer("http-request", tagProvider.httpRequestTags(exchange, t, null))
                                .record(System.nanoTime() - start, TimeUnit.NANOSECONDS)
                );
    }
}