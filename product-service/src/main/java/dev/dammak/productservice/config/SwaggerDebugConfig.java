package dev.dammak.productservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@Slf4j
public class SwaggerDebugConfig {

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        log.info("=== SpringDoc Configuration Debug ===");
        log.info("Swagger UI should be available at: http://localhost:8082/swagger-ui.html");
        log.info("OpenAPI docs should be available at: http://localhost:8082/api-docs");
        log.info("======================================");
    }
}