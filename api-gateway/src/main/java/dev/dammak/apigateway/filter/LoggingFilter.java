package dev.dammak.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class LoggingFilter implements GatewayFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = UUID.randomUUID().toString();

        // Log request
        logger.info("Request ID: {} | Method: {} | URI: {} | Headers: {} | Timestamp: {}",
                requestId,
                request.getMethod(),
                request.getURI(),
                request.getHeaders().toSingleValueMap(),
                LocalDateTime.now());

        // Add request ID to headers for tracing
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Request-ID", requestId)
                .build();

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .doFinally(signalType -> {
                    ServerHttpResponse response = exchange.getResponse();
                    long duration = System.currentTimeMillis() - startTime;

                    logger.info("Response ID: {} | Status: {} | Duration: {}ms | Timestamp: {}",
                            requestId,
                            response.getStatusCode(),
                            duration,
                            LocalDateTime.now());
                });
    }
}