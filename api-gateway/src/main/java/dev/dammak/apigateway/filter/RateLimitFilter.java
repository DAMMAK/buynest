package dev.dammak.apigateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class RateLimitFilter implements GatewayFilter {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    private static final int RATE_LIMIT = 100; // requests per minute
    private static final Duration WINDOW_SIZE = Duration.ofMinutes(1);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String clientId = getClientId(request);
        String key = "rate_limit:" + clientId;

        return redisTemplate.opsForValue()
                .increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        // Set expiration for the first request
                        return redisTemplate.expire(key, WINDOW_SIZE)
                                .then(Mono.just(count));
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > RATE_LIMIT) {
                        return rateLimitExceeded(exchange);
                    }

                    // Add rate limit headers
                    ServerHttpResponse response = exchange.getResponse();
                    response.getHeaders().add("X-RateLimit-Limit", String.valueOf(RATE_LIMIT));
                    response.getHeaders().add("X-RateLimit-Remaining",
                            String.valueOf(Math.max(0, RATE_LIMIT - count)));

                    return chain.filter(exchange);
                })
                .onErrorResume(throwable -> {
                    // If Redis is down, allow the request to proceed
                    return chain.filter(exchange);
                });
    }

    private String getClientId(ServerHttpRequest request) {
        // Try to get user ID from JWT token first
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (userId != null) {
            return "user:" + userId;
        }

        // Fall back to IP address
        String clientIp = request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        return "ip:" + clientIp;
    }

    private Mono<Void> rateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");

        String body = "{\"error\":\"Rate limit exceeded\",\"status\":429}";

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}