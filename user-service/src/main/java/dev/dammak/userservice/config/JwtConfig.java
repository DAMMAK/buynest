package dev.dammak.userservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    private String secret = "21ABC41C3481889DA91D8734EB517";
    private long expiration = 86400000; // 24 hours in milliseconds
    private String tokenPrefix = "Bearer ";
    private String headerString = "Authorization";
}