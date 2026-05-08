package com.eliteresume.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String frontendOrigin,
        Jwt jwt,
        Gemini gemini,
        Google google,
        Storage storage
) {
    public record Jwt(String secret, long expirationMs) {}
    public record Gemini(String apiKey, String model) {}
    public record Google(String clientId) {}
    public record Storage(String resumeDir) {}
}
