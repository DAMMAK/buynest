//package dev.dammak.userservice.config;
//
//
//import io.github.cdimascio.dotenv.Dotenv;
//import io.github.cdimascio.dotenv.DotenvEntry;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.core.env.Environment;
//import org.springframework.core.env.MapPropertySource;
//import org.springframework.core.env.MutablePropertySources;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Configuration
//public class EnvConfig {
//
//    @Bean
//    public static Dotenv dotenv() {
//        return Dotenv.load();
//    }
//
//    @Bean
//    public static MapPropertySource envPropertySource(Dotenv dotenv) {
//        Map<String, Object> map = dotenv.entries().stream()
//                .collect(Collectors.toMap(
//                        DotenvEntry::getKey,
//                        entry -> (Object) entry.getValue()
//                ));
//        return new MapPropertySource("dotenv", map);
//    }
//
//}
