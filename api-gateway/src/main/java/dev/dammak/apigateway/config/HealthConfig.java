package dev.dammak.apigateway.config;


import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

@Configuration
public class HealthConfig {

    @Bean
    public HealthIndicator redisHealthIndicator(ReactiveRedisTemplate<String, String> redisTemplate) {
        return () -> {
            try {
                return redisTemplate.hasKey("health-check")
                        .map(result -> Health.up()
                                .withDetail("redis", "Available")
                                .build())
                        .onErrorReturn(Health.down()
                                .withDetail("redis", "Not Available")
                                .build())
                        .block();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("redis", "Connection failed: " + e.getMessage())
                        .build();
            }
        };
    }
}