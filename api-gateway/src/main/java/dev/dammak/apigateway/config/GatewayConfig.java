package dev.dammak.apigateway.config;

import dev.dammak.apigateway.filter.AuthenticationFilter;
import dev.dammak.apigateway.filter.LoggingFilter;
import dev.dammak.apigateway.filter.RateLimitFilter;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.client.circuitbreaker.Customizer;

import java.time.Duration;


@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Autowired
    private LoggingFilter loggingFilter;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitFilter)
                                .filter(authenticationFilter)
                                .circuitBreaker(config -> config
                                        .setName("user-service-cb")
                                        .setFallbackUri("forward:/fallback/user-service"))
                                .rewritePath("/api/users/(?<segment>.*)", "/users/${segment}"))
                        .uri("lb://user-service"))

                // Product Catalog Service Routes
                .route("product-catalog-service", r -> r
                        .path("/api/products/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitFilter)
                                .circuitBreaker(config -> config
                                        .setName("product-catalog-cb")
                                        .setFallbackUri("forward:/fallback/product-catalog-service"))
                                .rewritePath("/api/products/(?<segment>.*)", "/products/${segment}"))
                        .uri("lb://product-catalog-service"))

                // Shopping Cart Service Routes
                .route("shopping-cart-service", r -> r
                        .path("/api/cart/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitFilter)
                                .filter(authenticationFilter)
                                .circuitBreaker(config -> config
                                        .setName("shopping-cart-cb")
                                        .setFallbackUri("forward:/fallback/shopping-cart-service"))
                                .rewritePath("/api/cart/(?<segment>.*)", "/cart/${segment}"))
                        .uri("lb://shopping-cart-service"))

                // Order Service Routes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitFilter)
                                .filter(authenticationFilter)
                                .circuitBreaker(config -> config
                                        .setName("order-service-cb")
                                        .setFallbackUri("forward:/fallback/order-service"))
                                .rewritePath("/api/orders/(?<segment>.*)", "/orders/${segment}"))
                        .uri("lb://order-service"))

                // Payment Service Routes
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitFilter)
                                .filter(authenticationFilter)
                                .circuitBreaker(config -> config
                                        .setName("payment-service-cb")
                                        .setFallbackUri("forward:/fallback/payment-service"))
                                .rewritePath("/api/payments/(?<segment>.*)", "/payments/${segment}"))
                        .uri("lb://payment-service"))

                // Notification Service Routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitFilter)
                                .filter(authenticationFilter)
                                .circuitBreaker(config -> config
                                        .setName("notification-service-cb")
                                        .setFallbackUri("forward:/fallback/notification-service"))
                                .rewritePath("/api/notifications/(?<segment>.*)", "/notifications/${segment}"))
                        .uri("lb://notification-service"))

                // Authentication Routes (no auth filter)
                // Authentication Routes (no auth filter)
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitFilter)
                                .circuitBreaker(config -> config
                                        .setName("auth-service-cb")
                                        .setFallbackUri("forward:/fallback/auth-service")))
                        // Remove the rewritePath for auth routes
                        .uri("lb://user-service"))

                .route("category-service", r -> r
                        .path("/api/categories/**")
                        .filters(f -> f
                                .filter(loggingFilter)
                                .filter(rateLimitFilter)
                                .circuitBreaker(config -> config
                                        .setName("category-service-cb")
                                        .setFallbackUri("forward:/fallback/category-service")))
                        // Remove the rewritePath for auth routes
                        .uri("lb://category-service"))

                .build();
    }

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .permittedNumberOfCallsInHalfOpenState(3)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofMillis(30000))
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(10))
                        .build())
                .build());
    }
}