package org.elmorshedy.AI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class AiService {
    private final WebClient webClient;
    private final String apiKey;

    @Autowired
    public AiService(WebClient geminiWebClient, String geminiApiKey) {
        this.webClient = geminiWebClient;
        this.apiKey = geminiApiKey;
    }

    public String getAiReply(String content, boolean isFamiliar) {
        String style = isFamiliar
                ? "اكتب رد ودود وشخصي للرسالة التالية:"
                : "اكتب رد رسمي وبسيط للرسالة التالية:";

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", style + " " + content)
                        ))
                )
        );

        try {
            Map response = webClient.post()
                    .uri("/models/gemini-1.5-flash:generateContent?key=" + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // استخراج النص من response
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                return (String) parts.get(0).get("text");
            } else {
                return "No reply from Gemini.";
            }

        } catch (Exception e) {
            return "Gemini Error: " + e.getMessage();
        }
    }
}
