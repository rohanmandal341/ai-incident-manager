package com.rohan.incidentmanager.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class GroqChatClient {

    @Value("${ai.groq.apiKey:}")
    private String apiKey;

    @Value("${ai.groq.baseUrl:https://api.groq.com/openai/v1}")
    private String baseUrl;

    @Value("${ai.model:llama-3.3-70b-versatile}")
    private String model;

    private final RestTemplate rt = new RestTemplate();

    public String chat(String system, String user) {
        String url = baseUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.2,
                "max_completion_tokens", 512,
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", system == null ? "" : system),
                        Map.of("role", "user", "content", user == null ? "" : user)
                )
        );

        ResponseEntity<Map> resp = rt.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            return "Sorry, the AI is unavailable right now.";
        }
        try {
            List choices = (List) resp.getBody().get("choices");
            if (choices == null || choices.isEmpty()) return "No answer.";
            Map first = (Map) choices.get(0);
            Map msg = (Map) first.get("message");
            Object content = msg.get("content");
            return content == null ? "No answer." : content.toString().trim();
        } catch (Exception e) {
            return "No answer.";
        }
    }
}
