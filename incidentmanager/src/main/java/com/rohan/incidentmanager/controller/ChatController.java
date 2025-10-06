package com.rohan.incidentmanager.controller;

import com.rohan.incidentmanager.ai.GroqChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final GroqChatClient client;

    public ChatController(GroqChatClient client) {
        this.client = client;
    }

    // Simple endpoint: POST /api/chat/ask { "q": "your question", "system": "optional system msg" }
    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody Map<String, String> body) {
        String system = body.getOrDefault("system", "You are a helpful backend/devops assistant. Answer briefly with clear steps.");
        String user = body.getOrDefault("q", "");
        String answer = client.chat(system, user);
        return Map.of("answer", answer);
    }
}
