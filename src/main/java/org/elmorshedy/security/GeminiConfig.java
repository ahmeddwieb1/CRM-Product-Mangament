package org.elmorshedy.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Bean
    public WebClient geminiWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public String geminiApiKey() {
        return apiKey;
    }
}
