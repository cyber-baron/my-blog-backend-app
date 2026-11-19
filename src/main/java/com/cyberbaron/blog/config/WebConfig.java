package com.cyberbaron.blog.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.cyberbaron.blog")
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // The provided React app runs at localhost:80 and calls the API at localhost:8080.
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost", "http://localhost:80")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
