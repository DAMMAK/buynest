package dev.dammak.apigateway.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter requestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("gateway.requests.total")
                .description("Total number of requests processed by the gateway")
                .register(meterRegistry);
    }

    @Bean
    public Timer requestTimer(MeterRegistry meterRegistry) {
        return Timer.builder("gateway.requests.duration")
                .description("Request processing time")
                .register(meterRegistry);
    }

    @Bean
    public Counter errorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("gateway.errors.total")
                .description("Total number of errors")
                .register(meterRegistry);
    }
}